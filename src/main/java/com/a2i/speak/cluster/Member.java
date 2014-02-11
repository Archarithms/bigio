/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.event.Event;

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

    private static final Logger LOG = LoggerFactory.getLogger(Member.class);

    private Status status;

    private final Map<String, String> tags = new HashMap<>();

    private final AtomicInteger sequence = new AtomicInteger(0);

    private String ip;
    private int dataPort;
    private int commandPort;

    public Member() {

    }

    public Member(String ip, int commandPort, int dataPort) {
        this.ip = ip;
        this.commandPort = commandPort;
        this.dataPort = dataPort;
    }

    public void sendCommand(final CommandMessage message) throws IOException {
        final byte[] bytes = message.encode();

        EventLoopGroup workerGroup = new NioEventLoopGroup();
            
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(final ChannelHandlerContext ctx) throws Exception {

                            ByteBuf buff = ctx.alloc().buffer(bytes.length);
                            buff.writeBytes(bytes);

                            final ChannelFuture f = ctx.writeAndFlush(buff);
                            f.addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    assert f == future;
                                    LOG.debug("Message sent");
                                    ctx.close();
                                }
                            });
                        }
                    });
                }

                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    LOG.error("Cannot initialize command client.", cause);
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(getIp(), getCommandPort()).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();

            LOG.debug("Finished sending message");
        } catch (InterruptedException ex) {
            LOG.error("Interrupted in command client.", ex);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void close() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Closing TCP connections to " + ip + ":" + commandPort + ":" + dataPort);
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

    public void initializeClients() {
        LOG.debug("Initializing command client");

//        new Thread() {
//            @Override
//            public void run() {
//                EventLoopGroup workerGroup = new NioEventLoopGroup();
//
//                try {
//                    Bootstrap b = new Bootstrap();
//                    b.group(workerGroup);
//                    b.channel(NioSocketChannel.class);
//                    b.option(ChannelOption.SO_KEEPALIVE, true);
//                    b.handler(new ChannelInitializer<SocketChannel>() {
//                        @Override
//                        public void initChannel(SocketChannel ch) throws Exception {
//                            ch.pipeline().addLast(new CommandMessageHandler());
//                            channel = ch;
//                        }
//
//                        @Override
//                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//                            LOG.error("Cannot initialize command client.", cause);
//                        }
//                    });
//
//                    // Start the client.
//                    ChannelFuture f = b.connect(getIp(), getCommandPort()).sync();
//
//                    // Wait until the connection is closed.
//                    f.channel().closeFuture().sync();
//                } catch (InterruptedException ex) {
//                    LOG.error("Interrupted in command client.", ex);
//                } finally {
//                    workerGroup.shutdownGracefully();
//                }
//            }
//        }.start();
    }

    private class CommandMessageHandler extends ChannelInboundHandlerAdapter {

        private ChannelHandlerContext ctx;

        public void writeMessage(byte[] bytes) {
            LOG.debug("Writing message");
            ctx.writeAndFlush(bytes);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            LOG.info("Command channel active");
            this.ctx = ctx;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ((ByteBuf) msg).release();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Caught an exception", cause);
            ctx.close();
        }
    }
}
