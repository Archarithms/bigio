/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.core;

import com.a2i.dms.Component;
import com.a2i.dms.Inject;
import com.a2i.dms.Parameters;
import com.a2i.dms.core.codec.GossipDecoder;
import com.a2i.dms.core.codec.GossipEncoder;
import com.a2i.dms.core.member.AbstractMember;
import com.a2i.dms.core.member.Member;
import com.a2i.dms.core.member.MemberHolder;
import com.a2i.dms.core.member.MemberKey;
import com.a2i.dms.core.member.RemoteMemberTCP;
import com.a2i.dms.core.member.RemoteMemberUDP;
import com.a2i.dms.util.NetworkUtil;
import com.a2i.dms.util.TimeUtil;
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

/**
 *
 * @author atrimble
 */
@Component
public class MCDiscovery extends Thread {
    
    private static final Logger LOG = LoggerFactory.getLogger(MCDiscovery.class);

    @Inject
    private MemberHolder memberHolder;

    private static final String PROTOCOL_PROPERTY = "com.a2i.protocol";
    private static final String DEFAULT_PROTOCOL = "tcp";
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
    private String protocol;

    public MCDiscovery() {
        enabled = Boolean.parseBoolean(Parameters.INSTANCE.getProperty(MULTICAST_ENABLED_PROPERTY, "true"));
        multicastGroup = Parameters.INSTANCE.getProperty(MULTICAST_GROUP_PROPERTY, DEFAULT_MULTICAST_GROUP);
        multicastPort = Integer.parseInt(Parameters.INSTANCE.getProperty(MULTICAST_PORT_PROPERTY, DEFAULT_MULTICAST_PORT));
    }

    public void setMemberHolder(MemberHolder memberHolder) {
        this.memberHolder = memberHolder;
    }

    public void initialize(Member me) {
        this.me = me;

        protocol = Parameters.INSTANCE.getProperty(PROTOCOL_PROPERTY, DEFAULT_PROTOCOL);

        setupNetworking();

        if(isEnabled()) {
            start();
        }
    }

    public void shutdown() throws InterruptedException {
        if(isEnabled() && workerGroup != null) {
            workerGroup.shutdownGracefully();
            join();
        }
    }

    public void setupNetworking() {
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

        try {
            if(!NetworkUtil.getNetworkInterface().isUp()) {
                LOG.error("Cannot form cluster. Network interface is down.");
                return;
            }
        } catch(SocketException ex) {
            LOG.error("Cannot form cluster.", ex);
            return;
        }
        
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
                        me.getIp(),
                        me.getGossipPort(),
                        me.getDataPort());
                message.setMillisecondsSinceMidnight(TimeUtil.getMillisecondsSinceMidnight());
                message.getTags().putAll(me.getTags());
                message.getMembers().add(MemberKey.getKey(me));
                message.getClock().add(me.getSequence().incrementAndGet());

                sendMessage(message);
            } catch (IOException ex) {
                LOG.error("Cannot serialize message.", ex);
            }

        } catch(InterruptedException ex) {

        }
    }

    @Override
    public void run() {
        try {
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

            Member member = memberHolder.getMember(key);

            if(member == null) {
                if(protocol.equalsIgnoreCase("udp")) {
                    member = new RemoteMemberUDP(message.getIp(), message.getGossipPort(), message.getDataPort());
                } else {
                    member = new RemoteMemberTCP(message.getIp(), message.getGossipPort(), message.getDataPort());
                }
                ((AbstractMember)member).initialize();
            }

            for(String k : message.getTags().keySet()) {
                member.getTags().put(k, message.getTags().get(k));
            }

            memberHolder.updateMemberStatus(member);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Exception in connection to Clustering Agent.", cause);
            ctx.close();
        }
    }
}
