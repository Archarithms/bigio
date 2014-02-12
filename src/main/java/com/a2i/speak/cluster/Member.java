/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class Member {

    public enum Status {

        Alive, Left, Failed, Unknown;

        public static Status fromString(String in) {
            switch (in) {
                case "alive":
                    return Alive;
                case "left":
                    return Left;
                case "failed":
                    return Failed;
                default:
                    return Unknown;
            }
        }
    };

    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_INTERVAL = 2000l;
    private static final int CONNECTION_TIMEOUT = 5000;
    private static final Logger LOG = LoggerFactory.getLogger(Member.class);
    
    private final AtomicInteger retryCount = new AtomicInteger(0);

    private Channel channel = null;
    private EventLoopGroup workerGroup = null;

    private final Map<String, String> tags = new HashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);
    private Status status = Status.Unknown;
    private String ip;
    private int dataPort;
    private int commandPort;

    public Member() {
        init();
    }

    public Member(String ip, int commandPort, int dataPort) {
        this.ip = ip;
        this.commandPort = commandPort;
        this.dataPort = dataPort;

        init();
    }

    private void init() {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                initializeClients();
            }
        });
    }

    public void sendCommand(final CommandMessage message) throws IOException {
        byte[] bytes = message.encode();
        if(channel != null) {
            channel.writeAndFlush(bytes);
        }
    }

    public void close() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Closing TCP connections to " + ip + ":" + commandPort + ":" + dataPort);
        }
        if(workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\nMember ").append(ip);
        builder.append(":");
        builder.append(commandPort);
        builder.append(":");
        builder.append(dataPort);
        if (status == Status.Alive || status == Status.Unknown) {
            builder.append("\n    is ");
        } else {
            builder.append("\n    has ");
        }
        builder.append(status);

        builder.append("\n    with properties");
        for (String key : tags.keySet()) {
            builder.append("\n        ");
            builder.append(key);
            builder.append(" -> ");
            builder.append(tags.get(key));
        }

        builder.append("\n");

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Member) {
            Member them = (Member) obj;
            return them.getIp().equals(getIp())
                    && them.getCommandPort() == getCommandPort()
                    && them.getDataPort() == getDataPort();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.ip);
        hash = 83 * hash + this.dataPort;
        hash = 83 * hash + this.commandPort;
        return hash;
    }

    /**
     * @return the sequence
     */
    public AtomicInteger getSequence() {
        return sequence;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the tags
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the data port
     */
    public int getDataPort() {
        return dataPort;
    }

    /**
     * @param dataPort the data port to set
     */
    public void setPort(int dataPort) {
        this.dataPort = dataPort;
    }

    /**
     * @return the commandPort
     */
    public int getCommandPort() {
        return commandPort;
    }

    /**
     * @param commandPort the commandPort to set
     */
    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
    }

    private void initializeClients() {
        LOG.debug("Initializing command client");

        workerGroup = new NioEventLoopGroup();
            
        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECTION_TIMEOUT);
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
            setStatus(Status.Alive);
            updateMember();
        }
    }

    private void retry() {
        if(retryCount.getAndIncrement() < MAX_RETRY_COUNT) {
            Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
                @Override
                public void run() {
                    initializeClients();
                }
            }, RETRY_INTERVAL, TimeUnit.MILLISECONDS);
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
            setStatus(Status.Left);
            updateMember();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.warn("Member failed");
            retry();
        }
    }
}
