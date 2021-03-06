/*
 * Copyright (c) 2015, Archarithms Inc.
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
package io.bigio.core.member;

import io.bigio.core.GossipMessage;
import io.bigio.core.ListenerRegistry;
import io.bigio.core.codec.GossipCodec;
import io.bigio.util.NetworkUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.msgpack.core.MessageTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.event.Event;

/**
 * A UDP implementation of the current BigIO cluster member.
 *
 * @author Andy Trimble
 */
public class MeMemberUDP extends MeMember {

    private static final int SERVER_THREAD_POOL_SIZE = 2;

    private static final int GOSSIP_BOSS_THREADS = 2;
    private static final int GOSSIP_WORKER_THREADS = 2;
    private static final int DATA_BOSS_THREADS = 2;
    private static final int DATA_WORKER_THREADS = 4;

    private static final Logger LOG = LoggerFactory.getLogger(MeMemberUDP.class);

    private EventLoopGroup gossipBossGroup = null;
    private EventLoopGroup gossipWorkerGroup = null;
    private EventLoopGroup dataBossGroup = null;
    private EventLoopGroup dataWorkerGroup = null;

    private final ExecutorService serverExecutor = Executors.newFixedThreadPool(SERVER_THREAD_POOL_SIZE);

    public MeMemberUDP(MemberHolder memberHolder, ListenerRegistry registry) {
        super(memberHolder, registry);
    }

    public MeMemberUDP(String ip, int gossipPort, int dataPort, MemberHolder memberHolder, ListenerRegistry registry) {
        super(ip, gossipPort, dataPort, memberHolder, registry);
    }

    @Override
    public void shutdown() {

        if (gossipBossGroup != null) {
            gossipBossGroup.shutdownGracefully();
        }

        if (gossipWorkerGroup != null) {
            gossipWorkerGroup.shutdownGracefully();
        }

        if (dataBossGroup != null) {
            dataBossGroup.shutdownGracefully();
        }

        if (dataWorkerGroup != null) {
            dataWorkerGroup.shutdownGracefully();
        }

    }

    @Override
    protected void initializeServers() {
        LOG.debug("Initializing gossip server on " + getIp() + ":" + getGossipPort());

        try {
            if (NetworkUtil.getNetworkInterface() == null || !NetworkUtil.getNetworkInterface().isUp()) {
                LOG.error("Cannot start networking. Interface is down.");
                return;
            }
        } catch (SocketException ex) {
            LOG.error("Cannot start networking.", ex);
            return;
        }

        serverExecutor.submit(new GossipServerThread());
        serverExecutor.submit(new DataServerThread());
    }

    private class GossipServerThread implements Runnable {

        private ChannelFuture f;

        public GossipServerThread() {
            gossipBossGroup = new NioEventLoopGroup(GOSSIP_BOSS_THREADS);
            gossipWorkerGroup = new NioEventLoopGroup(GOSSIP_WORKER_THREADS);
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(gossipBossGroup, gossipWorkerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
                                ch.pipeline().addLast(new GossipMessageDecoder());
                                ch.pipeline().addLast("encoder", new ByteArrayEncoder());
                                ch.pipeline().addLast("decoder", new ByteArrayDecoder());
                                ch.pipeline().addLast(new GossipMessageHandler());
                                if (LOG.isTraceEnabled()) {
                                    ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE));
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                LOG.error("Cannot initialize gossip server.", cause);
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

                // Bind and start to accept incoming connections.
                f = b.bind(getIp(), getGossipPort()).sync();
            } catch (InterruptedException ex) {
                LOG.error("Gossip server interrupted.", ex);
            }
        }

        @Override
        public void run() {
            try {
                // Wait until the server socket is closed.
                f.channel().closeFuture().sync();

                LOG.debug("Shutting down gossip server");
            } catch (InterruptedException ex) {
                LOG.error("Gossip server interrupted.", ex);
            } finally {
                gossipBossGroup.shutdownGracefully();
                gossipWorkerGroup.shutdownGracefully();
            }
        }
    }

    private class DataServerThread implements Runnable {

        private ChannelFuture f;

        public DataServerThread() {
            dataBossGroup = new NioEventLoopGroup(DATA_BOSS_THREADS);
            dataWorkerGroup = new NioEventLoopGroup(DATA_WORKER_THREADS);
            try {
                Bootstrap b = new Bootstrap();
                b.group(dataWorkerGroup)
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
                                ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
                                ch.pipeline().addLast(new DataMessageHandler());
                                if (LOG.isTraceEnabled()) {
                                    ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE));
                                }
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                LOG.error("Cannot initialize data server.", cause);
                            }
                        });

                // Bind and start to accept incoming connections.
                f = b.bind(getIp(), getDataPort()).sync();
            } catch (InterruptedException ex) {
                LOG.error("Message data interrupted.", ex);
            }
        }

        @Override
        public void run() {
            try {
                // Wait until the server socket is closed.
                f.channel().closeFuture().sync();

                LOG.debug("Shutting down data server");
            } catch (InterruptedException ex) {
                LOG.error("Message data interrupted.", ex);
            } finally {
                dataBossGroup.shutdownGracefully();
                dataWorkerGroup.shutdownGracefully();
            }
        }
    }

    private class GossipMessageDecoder extends ReplayingDecoder {

        @Override
        protected void decode(ChannelHandlerContext chc, ByteBuf bb, List<Object> list) throws Exception {
            int length = bb.readShort();
            list.add(bb.readBytes(length));
        }
    }

    private class GossipMessageHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof byte[]) {
                byte[] bytes = (byte[]) msg;
                try {
                    GossipMessage message = GossipCodec.decode(bytes);
                    reactor.notify(GOSSIP_TOPIC, Event.wrap(message));
                } catch (IOException | MessageTypeException ex) {
                    LOG.debug("Error decoding message.", ex);
                } finally {
                    ReferenceCountUtil.release(msg);
                }
            } else {
                LOG.trace(msg.getClass().getName());
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Error in UDP Client", cause);
            ctx.close();
        }
    }

    @Sharable
    private class DataMessageHandler extends MessageToMessageDecoder<DatagramPacket> {

        @Override
        public void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) {
            ByteBuf buff = packet.content();
            byte[] bytes = new byte[2];
            buff.readBytes(bytes, 0, 2);
            int size = (bytes[0] & 0xFF) << 8 | (bytes[1] & 0xFF);
            bytes = new byte[size];
            buff.readBytes(bytes, 0, size);
            decoderReactor.notify(DECODE_TOPIC, Event.wrap(bytes));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Error in UDP Client", cause);
            ctx.close();
        }
    }
}
