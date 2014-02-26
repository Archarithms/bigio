/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.core;

import com.a2i.sim.core.member.AbstractMember;
import com.a2i.sim.core.member.Member;
import com.a2i.sim.core.member.MemberKey;
import com.a2i.sim.core.member.MemberHolder;
import com.a2i.sim.core.member.RemoteMember;
import com.a2i.sim.util.NetworkUtil;
import com.a2i.sim.util.TimeUtil;
import com.a2i.sim.core.codec.GossipEncoder;
import com.a2i.sim.core.codec.GossipDecoder;
import com.a2i.sim.Parameters;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class MCDiscovery extends Thread {
    
    private static final Logger LOG = LoggerFactory.getLogger(MCDiscovery.class);
    private static final String MULTICAST_ENABLED_PROPERTY = "com.a2i.multicast.enabled";
    private static final String MULTICAST_GROUP_PROPERTY = "com.a2i.multicast.group";
    private static final String MULTICAST_PORT_PROPERTY = "com.a2i.multicast.port";
    private static final String DEFAULT_MULTICAST_GROUP = "239.0.0.1";
    private static final String DEFAULT_MULTICAST_PORT = "8989";
    private static final int THREADS = 2;

    private EventLoopGroup workerGroup;
    private final MessageHandler handler = new MessageHandler();

    private final boolean enabled;
    private final String multicastGroup;
    private final int multicastPort;

    private DatagramChannel channel;
    private InetSocketAddress group;
    private Member me;

    public MCDiscovery() {
        enabled = Boolean.parseBoolean(Parameters.INSTANCE.getProperty(MULTICAST_ENABLED_PROPERTY, "true"));
        multicastGroup = Parameters.INSTANCE.getProperty(MULTICAST_GROUP_PROPERTY, DEFAULT_MULTICAST_GROUP);
        multicastPort = Integer.parseInt(Parameters.INSTANCE.getProperty(MULTICAST_PORT_PROPERTY, DEFAULT_MULTICAST_PORT));
    }

    public void initialize(Member me) {
        this.me = me;

        if(isEnabled()) {
            start();
        }
    }

    public void shutdown() throws InterruptedException {
        if(isEnabled()) {
            workerGroup.shutdownGracefully();
            join();
        }
    }

    @Override
    public void run() {
        try {
            if(!NetworkUtil.getNetworkInterface().supportsMulticast()) {
                LOG.error("Network Interface doesn't support multicast.");
                return;
            }
        } catch (SocketException ex) {
            LOG.error("Error determining multicast support.", ex);
            return;
        }

        //InetSocketAddress addr = new InetSocketAddress(NetworkUtil.INSTANCE.getIp(), NetworkUtil.INSTANCE.getFreePort());
        InetSocketAddress addr = new InetSocketAddress(NetworkUtil.getIp(), multicastPort);
        group = new InetSocketAddress(multicastGroup, addr.getPort());

        workerGroup = new NioEventLoopGroup(THREADS, new DefaultThreadFactory("multicast-thread-pool", true));

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup)
                .channelFactory(new ChannelFactory<Channel>() {
                    @Override
                    public Channel newChannel() {
                        return new NioDatagramChannel(InternetProtocolFamily.IPv4);
                    }

                    @Override
                    public String toString() {
                        return NioDatagramChannel.class.getSimpleName() + ".class";
                    }
                }).handler(new ChannelInitializer<DatagramChannel>() {
                    @Override
                    public void initChannel(DatagramChannel ch) throws Exception {
                        if(LOG.isTraceEnabled()) {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE));
                        }

                        ch.pipeline().addLast(handler);
                    }
                }).localAddress(addr.getPort());

            b.option(ChannelOption.IP_MULTICAST_IF, NetworkUtil.getNetworkInterface());
            b.option(ChannelOption.SO_REUSEADDR, true);

            channel = (DatagramChannel)b.bind().sync().channel();
            channel.joinGroup(group, NetworkUtil.getNetworkInterface()).sync();

            LOG.info("Announcing");
            try {
                GossipMessage message = new GossipMessage(
                        me.getSequence().incrementAndGet(),
                        me.getIp(),
                        me.getGossipPort(),
                        me.getDataPort());
                message.setMillisecondsSinceMidnight(TimeUtil.getMillisecondsSinceMidnight());
                message.getTags().putAll(me.getTags());
                message.getMembers().add(MemberKey.getKey(me));

                sendMessage(message);
            } catch (IOException ex) {
                LOG.error("Cannot serialize message.", ex);
            }

            // Wait until the connection is closed.
            channel.closeFuture().sync();
        } catch(InterruptedException ex) {
            LOG.error("Error in RPC call.", ex);
        }

        LOG.info("Connection to Clustering agent closed.");
    }

    public void sendMessage(GossipMessage message) throws IOException {

        byte[] bytes = GossipEncoder.encode(message);
        ByteBuf buff = Unpooled.buffer(bytes.length);
        buff.writeBytes(bytes);
        
        try {
            channel.writeAndFlush(new DatagramPacket(buff, group)).sync();
        } catch (InterruptedException ex) {
            LOG.error("Interrupted waiting on sent message.", ex);
        }
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    private class MessageHandler extends SimpleChannelInboundHandler<DatagramPacket> {

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            
        }

        @Override
        protected void channelRead0(ChannelHandlerContext chc, DatagramPacket packet) throws Exception {
            ByteBuf buff = packet.content();
            GossipMessage message = GossipDecoder.decode(buff.nioBuffer(buff.readerIndex(), buff.readableBytes()));

            String key = MemberKey.getKey(message);

            Member member = MemberHolder.INSTANCE.getMember(key);

            if(member == null) {
                member = new RemoteMember(message.getIp(), message.getGossipPort(), message.getDataPort());
                ((AbstractMember)member).initialize();
            }

            for(String k : message.getTags().keySet()) {
                member.getTags().put(k, message.getTags().get(k));
            }

            MemberHolder.INSTANCE.updateMemberStatus(member);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Exception in connection to Clustering Agent.", cause);
            ctx.close();
        }
    }
}
