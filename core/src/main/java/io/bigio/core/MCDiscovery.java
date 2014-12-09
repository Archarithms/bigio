/*
 * Copyright (c) 2014, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package io.bigio.core;

import io.bigio.Component;
import io.bigio.Inject;
import io.bigio.Parameters;
import io.bigio.core.codec.GossipDecoder;
import io.bigio.core.codec.GossipEncoder;
import io.bigio.core.member.AbstractMember;
import io.bigio.core.member.Member;
import io.bigio.core.member.MemberHolder;
import io.bigio.core.member.MemberKey;
import io.bigio.core.member.RemoteMemberTCP;
import io.bigio.core.member.RemoteMemberUDP;
import io.bigio.util.NetworkUtil;
import io.bigio.util.TimeUtil;
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
 * A discovery protocol implemented using multicast.
 * 
 * @author Andy Trimble
 */
@Component
public class MCDiscovery extends Thread {
    
    private static final Logger LOG = LoggerFactory.getLogger(MCDiscovery.class);

    @Inject
    private MemberHolder memberHolder;

    private static final String MULTICAST_ENABLED_PROPERTY = "io.bigio.multicast.enabled";
    private static final String MULTICAST_GROUP_PROPERTY = "io.bigio.multicast.group";
    private static final String MULTICAST_PORT_PROPERTY = "io.bigio.multicast.port";
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

    /**
     * Constructor.
     */
    public MCDiscovery() {
        enabled = Boolean.parseBoolean(Parameters.INSTANCE.getProperty(MULTICAST_ENABLED_PROPERTY, "true"));
        multicastGroup = Parameters.INSTANCE.getProperty(MULTICAST_GROUP_PROPERTY, DEFAULT_MULTICAST_GROUP);
        multicastPort = Integer.parseInt(Parameters.INSTANCE.getProperty(MULTICAST_PORT_PROPERTY, DEFAULT_MULTICAST_PORT));
    }

    /**
     * Set the member container.
     * 
     * @param memberHolder the member container.
     */
    public void setMemberHolder(MemberHolder memberHolder) {
        this.memberHolder = memberHolder;
    }

    /**
     * Initialize the discovery service.
     * 
     * @param me the current member.
     */
    public void initialize(Member me) {
        this.me = me;

        protocol = Parameters.INSTANCE.getProperty(ClusterService.PROTOCOL_PROPERTY, ClusterService.DEFAULT_PROTOCOL);

        setupNetworking();

        if(isEnabled()) {
            start();
        }
    }

    /**
     * Shutdown the discovery service.
     */
    public void shutdown() {
        if(isEnabled() && workerGroup != null) {
            workerGroup.shutdownGracefully();
            try {
                join();
            } catch (InterruptedException ex) {
                LOG.warn("Interrupted while shutting down multicast agent.", ex);
            }
        }
    }

    /**
     * Setup the multicast facilities.
     */
    public void setupNetworking() {
        if(NetworkUtil.getNetworkInterface() == null) {
            LOG.error("Cannot form cluster. No Network interface can be found.");
            return;
        }
        
        try {
            if(!NetworkUtil.getNetworkInterface().supportsMulticast()) {
                LOG.error("Network Interface doesn't support multicast.");
                return;
            }
        } catch (SocketException ex) {
            LOG.error("Error determining multicast support.", ex);
            return;
        }

        try {
            if(NetworkUtil.getNetworkInterface() == null || !NetworkUtil.getNetworkInterface().isUp()) {
                LOG.error("Cannot form cluster. Network interface is down.");
                return;
            }
        } catch(SocketException ex) {
            LOG.error("Cannot form cluster.", ex);
            return;
        }
        
        group = new InetSocketAddress(multicastGroup, multicastPort);

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
                }).localAddress(multicastPort);

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
                message.setPublicKey(me.getPublicKey());

                sendMessage(message);
            } catch (IOException ex) {
                LOG.error("Cannot serialize message.", ex);
            }

        } catch(InterruptedException ex) {

        }
    }

    /**
     * Run the multicast listener.
     */
    @Override
    public void run() {
        try {
            // Wait until the connection is closed.
            if(channel != null) {
                channel.closeFuture().sync();
            }
        } catch(InterruptedException ex) {
            LOG.error("Error in RPC call.", ex);
        }

        LOG.info("Connection to Clustering agent closed.");
    }

    /**
     * Send a gossip message over multicast.
     * 
     * @param message a message.
     * @throws IOException in case of a sending error.
     */
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
        protected void channelRead0(ChannelHandlerContext chc, DatagramPacket packet) {
            ByteBuf buff = packet.content();

            try {
                GossipMessage message = GossipDecoder.decode(buff.nioBuffer(buff.readerIndex(), buff.readableBytes()));

                String key = MemberKey.getKey(message);

                Member member = memberHolder.getMember(key);

                if(member == null) {
                    if("udp".equalsIgnoreCase(protocol)) {
                        if(LOG.isTraceEnabled()) {
                            LOG.trace(new StringBuilder()
                                    .append("Discovered new UDP member: ")
                                    .append(message.getIp())
                                    .append(":")
                                    .append(message.getGossipPort())
                                    .append(":").append(message.getDataPort()).toString());
                        }
                        member = new RemoteMemberUDP(message.getIp(), message.getGossipPort(), message.getDataPort(), memberHolder);
                    } else {
                        if(LOG.isTraceEnabled()) {
                            LOG.trace(new StringBuilder()
                                    .append("Discovered new TCP member: ")
                                    .append(message.getIp())
                                    .append(":")
                                    .append(message.getGossipPort())
                                    .append(":").append(message.getDataPort()).toString());
                        }
                        member = new RemoteMemberTCP(message.getIp(), message.getGossipPort(), message.getDataPort(), memberHolder);
                    }

                    if(message.getPublicKey() != null) {
                        member.setPublicKey(message.getPublicKey());
                    }

                    ((AbstractMember)member).initialize();
                } else {
                    if(LOG.isTraceEnabled()) {
                            LOG.trace(new StringBuilder()
                                    .append("Received known member: ")
                                    .append(message.getIp())
                                    .append(":")
                                    .append(message.getGossipPort())
                                    .append(":").append(message.getDataPort()).toString());
                        }
                }

                for(String k : message.getTags().keySet()) {
                    member.getTags().put(k, message.getTags().get(k));
                }

                memberHolder.updateMemberStatus(member);
            } catch(IOException ex) {
                LOG.error("Error receiving multicast discovery message.", ex);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Exception in connection to Clustering Agent.", cause);
        }
    }
}
