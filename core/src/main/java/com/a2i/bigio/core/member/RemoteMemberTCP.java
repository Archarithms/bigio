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
package com.a2i.bigio.core.member;

import com.a2i.bigio.Parameters;
import com.a2i.bigio.core.Envelope;
import com.a2i.bigio.core.GossipMessage;
import com.a2i.bigio.core.codec.EnvelopeEncoder;
import com.a2i.bigio.core.codec.GossipEncoder;
import com.a2i.bigio.util.NetworkUtil;
import com.a2i.bigio.util.RunningStatistics;
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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TCP implementation of a remote BigIO cluster member.
 * 
 * @author Andy Trimble
 */
public class RemoteMemberTCP extends RemoteMember {

    private static final String MAX_RETRY_COUNT_PROPERTY = "com.a2i.bigio.remote.maxRetry";
    private static final String RETRY_INTERVAL_PROPERTY = "com.a2i.bigio.remote.retryInterval";
    private static final String CONNECTION_TIMEOUT_PROPERTY = "com.a2i.bigio.remote.connectionTimeout";
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

    public RemoteMemberTCP(MemberHolder memberHolder) {
        super(memberHolder);
    }

    public RemoteMemberTCP(String ip, int gossipPort, int dataPort, MemberHolder memberHolder) {
        super(ip, gossipPort, dataPort, memberHolder);
    }

    @Override
    public void initialize() {
        maxRetry = Integer.parseInt(Parameters.INSTANCE.getProperty(
                MAX_RETRY_COUNT_PROPERTY, DEFAULT_MAX_RETRY_COUNT));
        retryInterval = Long.parseLong(Parameters.INSTANCE.getProperty(
                RETRY_INTERVAL_PROPERTY, DEFAULT_RETRY_INTERVAL));
        timeout = Integer.parseInt(Parameters.INSTANCE.getProperty(
                CONNECTION_TIMEOUT_PROPERTY, DEFAULT_CONNECTION_TIMEOUT));

        try {
            if(NetworkUtil.getNetworkInterface() == null || !NetworkUtil.getNetworkInterface().isUp()) {
                LOG.error("Cannot start networking. Interface is down.");
                return;
            }
        } catch(SocketException ex) {
            LOG.error("Cannot start networking.", ex);
            return;
        }

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

        if(LOG.isTraceEnabled()) {
            dataSizeStat.push(bytes.length);
        }
        
        if(dataChannel != null) {
            dataChannel.writeAndFlush(Unpooled.wrappedBuffer(bytes));
        }
    }

    @Override
    public void gossip(final GossipMessage message) throws IOException {
        byte[] bytes = GossipEncoder.encode(message);

        if(LOG.isTraceEnabled()) {
            gossipSizeStat.push(bytes.length);
        }

        if(gossipChannel != null) {
            gossipChannel.writeAndFlush(bytes);
        }
    }

    @Override
    public void shutdown() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Closing connections to " + getIp() + ":" + getGossipPort() + ":" + getDataPort());
        }

        if(gossipWorkerGroup != null) {
            gossipWorkerGroup.shutdownGracefully();
        }
        
        if(dataWorkerGroup != null) {
            dataWorkerGroup.shutdownGracefully();
        }

        if(LOG.isTraceEnabled()) {
            LOG.trace("Mean sent gossip message size: " + gossipSizeStat.mean() + " over " + gossipSizeStat.numSamples() + " samples");
            LOG.trace("Mean sent data message size: " + dataSizeStat.mean() + " over " + dataSizeStat.numSamples() + " samples");
        }
    }

    private void updateMember() {
        memberHolder.updateMemberStatus(this);
    }

    private void initializeGossipClient() {
        LOG.trace("Initializing gossip client");

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
                if(LOG.isTraceEnabled()) {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE));
                }
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
        LOG.trace("Initializing data client");

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
                if(LOG.isTraceEnabled()) {
                    ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE));
                }
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
            LOG.trace("Member left");
            setStatus(MemberStatus.Left);
            updateMember();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            retryGossipConnection();
        }
    }

    private class DataExceptionHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            retryGossipConnection();
        }
    }
}
