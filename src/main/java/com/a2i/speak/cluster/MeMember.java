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
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.util.List;
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
public class MeMember extends AbstractMember {

    private static final Logger LOG = LoggerFactory.getLogger(MeMember.class);
                
    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;

    private final Environment env = new Environment();
    private Reactor reactor;

    public MeMember() {
        super();
    }

    public MeMember(String ip, int commandPort, int dataPort) {
        super(ip, commandPort, dataPort);
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
    protected void initialize() {
        initializeReactor();
        initializeServers();
    }

    @Override
    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
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
                bossGroup = new NioEventLoopGroup();
                workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap();
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
//                                    ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE));
                                    ch.pipeline().addLast(new CommandMessageDecoder());
                                    ch.pipeline().addLast("encoder", new ByteArrayEncoder());
                                    ch.pipeline().addLast("decoder", new ByteArrayDecoder());
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

                    LOG.debug("Shutting down server");
                } catch (InterruptedException ex) {
                    LOG.error("Command server interrupted.", ex);
                } finally {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        }.start();
    }

    private class CommandMessageDecoder extends ReplayingDecoder {

        @Override
        protected void decode(ChannelHandlerContext chc, ByteBuf bb, List<Object> list) throws Exception {
            int length = bb.readShort();
//            list.add(length);
            list.add(bb.readBytes(length));
        }
    }

    private class CommandMessageHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if(msg instanceof byte[]) {
                byte[] bytes = (byte[]) msg;
                try {
                    CommandMessage message = new CommandMessage().decode(bytes);
                    reactor.notify(CommandMessageType.fromValue(message.getMessage()), Event.wrap(message));
                    
                } catch (IOException ex) {
                    LOG.error("Error decoding message.", ex);
                } finally {
                    ReferenceCountUtil.release(msg);
                }
            } else if (msg instanceof Integer) {
                // length field
            } else {
                LOG.debug(msg.getClass().getName());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Error in TCP Client", cause);
            ctx.close();
        }
    }
}
