/*
 * Copyright 2014 Archarithms Inc.
 */
package com.a2i.dms.core.member;

import com.a2i.dms.Parameters;
import com.a2i.dms.core.Envelope;
import com.a2i.dms.core.GossipMessage;
import com.a2i.dms.core.codec.EnvelopeEncoder;
import com.a2i.dms.core.codec.GossipEncoder;
import com.a2i.dms.util.RunningStatistics;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class RemoteMemberTCP extends RemoteMember {

    private static final String MAX_RETRY_COUNT_PROPERTY = "com.a2i.remote.maxRetry";
    private static final String RETRY_INTERVAL_PROPERTY = "com.a2i.remote.retryInterval";
    private static final String CONNECTION_TIMEOUT_PROPERTY = "com.a2i.remote.connectionTimeout";
    private static final String DEFAULT_MAX_RETRY_COUNT = "3";
    private static final String DEFAULT_RETRY_INTERVAL = "2000";
    private static final String DEFAULT_CONNECTION_TIMEOUT = "5000";
    private static final int CLIENT_THREAD_POOL_SIZE = 2;
    private static final int GOSSIP_WORKER_THREADS = 2;
    private static final int DATA_WORKER_THREADS = 2;
    private static final Logger LOG = LoggerFactory.getLogger(RemoteMemberTCP.class);

    private int maxRetry;
    private long retryInterval;
    private int timeout;
    
    private final AtomicInteger gossipRetryCount = new AtomicInteger(0);
    private final AtomicInteger dataRetryCount = new AtomicInteger(0);
    private final ScheduledExecutorService retryExecutor = Executors.newScheduledThreadPool(CLIENT_THREAD_POOL_SIZE);
    private final ExecutorService serverExecutor = Executors.newFixedThreadPool(CLIENT_THREAD_POOL_SIZE);

    private Channel gossipChannel = null;
    private Channel dataChannel = null;
    private EventLoopGroup gossipWorkerGroup = null;
    private EventLoopGroup dataWorkerGroup = null;

    private final RunningStatistics gossipSizeStat = new RunningStatistics();
    private final RunningStatistics dataSizeStat = new RunningStatistics();

    public RemoteMemberTCP() {
        super();
    }

    public RemoteMemberTCP(String ip, int gossipPort, int dataPort) {
        super(ip, gossipPort, dataPort);
    }

    @Override
    public void initialize() {
        maxRetry = Integer.parseInt(Parameters.INSTANCE.getProperty(
                MAX_RETRY_COUNT_PROPERTY, DEFAULT_MAX_RETRY_COUNT));
        retryInterval = Long.parseLong(Parameters.INSTANCE.getProperty(
                RETRY_INTERVAL_PROPERTY, DEFAULT_RETRY_INTERVAL));
        timeout = Integer.parseInt(Parameters.INSTANCE.getProperty(
                CONNECTION_TIMEOUT_PROPERTY, DEFAULT_CONNECTION_TIMEOUT));

        serverExecutor.submit(new Runnable() {
            @Override
            public void run() {
                initializeGossipClient();
            }
        });
        serverExecutor.submit(new Runnable() {
            @Override
            public void run() {
                initializeDataClient();
            }
        });
    }

    @Override
    public void send(final Envelope message) throws IOException {
        byte[] bytes = EnvelopeEncoder.encode(message);

        if(LOG.isDebugEnabled()) {
            dataSizeStat.push(bytes.length);
        }
        
        if(dataChannel != null) {
            dataChannel.writeAndFlush(Unpooled.wrappedBuffer(bytes));
        }
    }

    @Override
    public void gossip(final GossipMessage message) throws IOException {
        byte[] bytes = GossipEncoder.encode(message);

        if(LOG.isDebugEnabled()) {
            gossipSizeStat.push(bytes.length);
        }

        if(gossipChannel != null) {
            gossipChannel.writeAndFlush(bytes);
        }
    }

    @Override
    public void shutdown() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Closing connections to " + getIp() + ":" + getGossipPort() + ":" + getDataPort());
        }

        if(gossipWorkerGroup != null) {
            gossipWorkerGroup.shutdownGracefully();
        }
        
        if(dataWorkerGroup != null) {
            dataWorkerGroup.shutdownGracefully();
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("Mean sent gossip message size: " + gossipSizeStat.mean() + " over " + gossipSizeStat.numSamples() + " samples");
            LOG.debug("Mean sent data message size: " + dataSizeStat.mean() + " over " + dataSizeStat.numSamples() + " samples");
        }
    }

    private void updateMember() {
        memberHolder.updateMemberStatus(this);
    }

    private void initializeGossipClient() {
        LOG.debug("Initializing gossip client");

        gossipWorkerGroup = new NioEventLoopGroup(GOSSIP_WORKER_THREADS);
            
        Bootstrap b = new Bootstrap();
        b.group(gossipWorkerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
                ch.pipeline().addLast("encoder", new ByteArrayEncoder());
                ch.pipeline().addLast("decoder", new ByteArrayDecoder());
                ch.pipeline().addLast(new GossipExceptionHandler());
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                LOG.error("Cannot initialize gossip client.", cause);
                ctx.close();
            }
        });

        // Start the client.
        ChannelFuture future = b.connect(getIp(), getGossipPort()).awaitUninterruptibly();

        if(future.isCancelled()) {
            LOG.warn("Connection cancelled by user");
            gossipChannel = null;
        } else if(!future.isSuccess()) {
            gossipChannel = null;
            retryGossipConnection();
        } else {
            gossipChannel = future.channel();
            setStatus(MemberStatus.Alive);
            updateMember();
        }
    }

    private void initializeDataClient() {
        LOG.debug("Initializing data client");

        dataWorkerGroup = new NioEventLoopGroup(DATA_WORKER_THREADS);
            
        Bootstrap b = new Bootstrap();
        b.group(dataWorkerGroup)
         .channel(NioSocketChannel.class)
         .option(ChannelOption.SO_SNDBUF, 262144)
         .option(ChannelOption.SO_RCVBUF, 262144)
         .option(ChannelOption.SO_KEEPALIVE, true)
         .option(ChannelOption.TCP_NODELAY, true)
         .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
         .handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.config().setAllocator(UnpooledByteBufAllocator.DEFAULT);
                ch.pipeline().addLast("encoder", new ByteArrayEncoder());
                ch.pipeline().addLast("decoder", new ByteArrayDecoder());
                ch.pipeline().addLast(new DataExceptionHandler());
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                LOG.error("Cannot initialize data client.", cause);
                ctx.close();
            }
        });

        // Start the client.
        ChannelFuture future = b.connect(getIp(), getDataPort()).awaitUninterruptibly();

        if(future.isCancelled()) {
            LOG.warn("Connection cancelled by user");
            dataChannel = null;
        } else if(!future.isSuccess()) {
            dataChannel = null;
            retryDataConnection();
        } else {
            dataChannel = future.channel();

            try {
                dataChannel.closeFuture().sync();
            } catch (InterruptedException ex) {
                LOG.warn("Interrupted waiting for client to shutdown.", ex);
            }
        }
    }

    private void retryGossipConnection() {
        if(gossipRetryCount.getAndIncrement() < maxRetry) {
            retryExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    initializeGossipClient();
                }
            }, retryInterval, TimeUnit.MILLISECONDS);
        } else {
            LOG.warn("Could not connect to gossip server after max retries.");
        }
    }

    private void retryDataConnection() {
        if(dataRetryCount.getAndIncrement() < maxRetry) {
            retryExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    initializeDataClient();
                }
            }, retryInterval, TimeUnit.MILLISECONDS);
        } else {
            LOG.warn("Could not connect to data server after max retries.");
        }
    }

    private class GossipExceptionHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LOG.debug("Member left");
            setStatus(MemberStatus.Left);
            updateMember();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.warn("Gossip connection failed");
            retryGossipConnection();
        }
    }

    private class DataExceptionHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.warn("Data connection failed");
            retryGossipConnection();
        }
    }
}
