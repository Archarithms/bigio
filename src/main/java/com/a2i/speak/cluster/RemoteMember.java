/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

import com.a2i.speak.Parameters;
import io.netty.bootstrap.Bootstrap;
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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class RemoteMember extends AbstractMember {

    private static final String MAX_RETRY_COUNT_PROPERTY = "com.a2i.remote.maxRetry";
    private static final String RETRY_INTERVAL_PROPERTY = "com.a2i.remote.retryInterval";
    private static final String CONNECTION_TIMEOUT_PROPERTY = "com.a2i.remote.connectionTimeout";
    private static final String DEFAULT_MAX_RETRY_COUNT = "3";
    private static final String DEFAULT_RETRY_INTERVAL = "2000";
    private static final String DEFAULT_CONNECTION_TIMEOUT = "5000";
    private static final Logger LOG = LoggerFactory.getLogger(Member.class);

    private int maxRetry;
    private long retryInterval;
    private int timeout;
    
    private final AtomicInteger retryCount = new AtomicInteger(0);

    private Channel channel = null;
    private EventLoopGroup workerGroup = null;

    public RemoteMember() {
        super();
    }

    public RemoteMember(String ip, int commandPort, int dataPort) {
        super(ip, commandPort, dataPort);
    }

    @Override
    protected void initialize() {
        maxRetry = Integer.parseInt(Parameters.INSTANCE.getProperty(
                MAX_RETRY_COUNT_PROPERTY, DEFAULT_MAX_RETRY_COUNT));
        retryInterval = Long.parseLong(Parameters.INSTANCE.getProperty(
                RETRY_INTERVAL_PROPERTY, DEFAULT_RETRY_INTERVAL));
        timeout = Integer.parseInt(Parameters.INSTANCE.getProperty(
                CONNECTION_TIMEOUT_PROPERTY, DEFAULT_CONNECTION_TIMEOUT));

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                initializeClients();
            }
        });
    }

    public void sendCommand(final GossipMessage message) throws IOException {
        byte[] bytes = GossipEncoder.encode(message);
        if(channel != null) {
            channel.writeAndFlush(bytes);
        }
    }

    @Override
    protected void shutdown() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Closing TCP connections to " + getIp() + ":" + getCommandPort() + ":" + getDataPort());
        }

        if(workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    private void initializeClients() {
        LOG.debug("Initializing command client");

        workerGroup = new NioEventLoopGroup();
            
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("encoder", new ByteArrayEncoder());
                ch.pipeline().addLast("decoder", new ByteArrayDecoder());
                ch.pipeline().addLast(new ExceptionHandler());
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                LOG.error("Cannot initialize command client.", cause);
                ctx.close();
            }
        });

        // Start the client.
        ChannelFuture future = b.connect(getIp(), getCommandPort()).awaitUninterruptibly();

        if(future.isCancelled()) {
            LOG.warn("Connection cancelled by user");
            channel = null;
        } else if(!future.isSuccess()) {
            channel = null;
            retry();
        } else {
            channel = future.channel();
            setStatus(MemberStatus.Alive);
            updateMember();
        }
    }

    private void retry() {
        if(retryCount.getAndIncrement() < maxRetry) {
            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    initializeClients();
                }
            }, retryInterval, TimeUnit.MILLISECONDS);
        } else {
            LOG.warn("Could not connect to member after max retries.");
        }
    }

    private void updateMember() {
        MemberHolder.INSTANCE.updateMemberStatus(this);
    }

    private class ExceptionHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LOG.debug("Member left");
            setStatus(MemberStatus.Left);
            updateMember();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.warn("Member failed");
            retry();
        }
    }
}
