/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;

/**
 *
 * @author atrimble
 */
public class MeMember extends Member {

    private static final Logger LOG = LoggerFactory.getLogger(MeMember.class);

    private final Environment env = new Environment();
    private Reactor reactor;

    public MeMember() {
        super();
    }

    public MeMember(String ip, int commandPort, int dataPort) {
        super(ip, commandPort, dataPort);
        initializeReactor();
        initializeServers();
    }

    public void addCommandConsumer(CommandMessageType type, final CommandListener consumer) {
        reactor.on(Selectors.object(type), new Consumer<Event<CommandMessage>>() {
            @Override
            public void accept(Event<CommandMessage> m) {
                consumer.accept(m.getData());
            }
        });
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void initializeClients() {
        
    }

    private void initializeReactor() {
        reactor = Reactors.reactor()
                .env(env)
                .dispatcher(Environment.EVENT_LOOP)
                .get();
    }

    private void initializeServers() {
        LOG.debug("Initializing command server on " + getIp() + ":" + getCommandPort());

        new Thread() {
            @Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(new CommandMessageHandler());
                                }

                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    LOG.error("Cannot initialize command server.", cause);
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    // Bind and start to accept incoming connections.
                    ChannelFuture f = b.bind(getIp(), getCommandPort()).sync();

                    // Wait until the server socket is closed.
                    // In this example, this does not happen, but you can do that to gracefully
                    // shut down your server.
                    f.channel().closeFuture().sync();
                } catch (InterruptedException ex) {
                    LOG.error("Command server interrupted.", ex);
                } finally {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        }.start();
    }

    public class CommandMessageHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            LOG.debug("Inbound server connection");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf m = (ByteBuf) msg;
            try {
                LOG.debug("Decoding message");
                CommandMessage message = new CommandMessage().decode(m.nioBuffer());
                reactor.notify(CommandMessageType.fromValue(message.getMessage()), Event.wrap(message));
                
            } catch (IOException ex) {
                LOG.error("Error decoding message.", ex);
            } finally {
                ReferenceCountUtil.release(m);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Error in TCP Client", cause);
            ctx.close();
        }
    }
}
