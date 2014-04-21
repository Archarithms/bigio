/*
 * Copyright 2014 Archarithms Inc.
 */
package com.a2i.dms.core.member;

import com.a2i.dms.core.GossipMessage;
import com.a2i.dms.core.codec.GossipDecoder;
import com.a2i.dms.util.NetworkUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.event.Event;

/**
 *
 * @author atrimble
 */
public class MeMemberTCP extends MeMember {

    private static final int SERVER_THREAD_POOL_SIZE = 2;

    private static final int GOSSIP_BOSS_THREADS = 2;
    private static final int GOSSIP_WORKER_THREADS = 2;
    private static final int DATA_BOSS_THREADS = 2;
    private static final int DATA_WORKER_THREADS = 4;

    private static final Logger LOG = LoggerFactory.getLogger(MeMemberTCP.class);
                
    private EventLoopGroup gossipBossGroup = null;
    private EventLoopGroup gossipWorkerGroup = null;
    private EventLoopGroup dataBossGroup = null;
    private EventLoopGroup dataWorkerGroup = null;

    private final ExecutorService serverExecutor = Executors.newFixedThreadPool(SERVER_THREAD_POOL_SIZE);

    public MeMemberTCP() {
        super();
    }

    public MeMemberTCP(String ip, int gossipPort, int dataPort) {
        super(ip, gossipPort, dataPort);
    }

    @Override
    public void shutdown() {
        
        gossipBossGroup.shutdownGracefully();
        gossipWorkerGroup.shutdownGracefully();
        dataBossGroup.shutdownGracefully();
        dataWorkerGroup.shutdownGracefully();

    }

    @Override
    protected void initializeServers() {
        LOG.debug("Initializing gossip server on " + getIp() + ":" + getGossipPort());

        try {
            if(NetworkUtil.getNetworkInterface() == null || !NetworkUtil.getNetworkInterface().isUp()) {
                LOG.error("Cannot start networking. Interface is down.");
                return;
            }
        } catch(SocketException ex) {
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
                ServerBootstrap b = new ServerBootstrap();
                b.group(dataBossGroup, dataWorkerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel ch) throws Exception {
                                ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
                                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(32768, 0, 2, 0, 2));
                                ch.pipeline().addLast("decoder", new ByteArrayDecoder());
                                ch.pipeline().addLast(new DataMessageHandler());
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                LOG.error("Cannot initialize data server.", cause);
                            }
                        })
                        .option(ChannelOption.SO_SNDBUF, 262144)
                        .option(ChannelOption.SO_RCVBUF, 262144)
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);

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
            if(msg instanceof byte[]) {
                byte[] bytes = (byte[]) msg;
                try {
                    GossipMessage message = GossipDecoder.decode(bytes);
                    reactor.notify(GOSSIP_TOPIC, Event.wrap(message));
                } catch (IOException ex) {
                    LOG.error("Error decoding message.", ex);
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
            LOG.error("Error in TCP Client", cause);
            ctx.close();
        }
    }

    @Sharable
    private class DataMessageHandler extends SimpleChannelInboundHandler<byte[]> {
        
        @Override
        public void channelRead0(ChannelHandlerContext ctx, byte[] bytes) {
            decoderReactor.notify(DECODE_TOPIC, Event.wrap(bytes));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Error in TCP Client", cause);
            ctx.close();
        }
    }
}
