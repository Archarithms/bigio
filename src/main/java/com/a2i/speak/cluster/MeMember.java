/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

import java.io.IOException;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;
import reactor.tcp.TcpConnection;
import reactor.tcp.TcpServer;
import reactor.tcp.encoding.StandardCodecs;
import reactor.tcp.netty.NettyTcpServer;
import reactor.tcp.spec.TcpServerSpec;

/**
 *
 * @author atrimble
 */
public class MeMember extends Member {

    private static final Logger LOG = LoggerFactory.getLogger(MeMember.class);

    private TcpServer<byte[], byte[]> commandServer;
    private TcpServer<byte[], byte[]> dataServer;
        
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
        commandServer.shutdown();
        dataServer.shutdown();
    }

    private void initializeReactor() {
        reactor = Reactors.reactor()
                .env(env)
                .dispatcher(Environment.EVENT_LOOP)
                .get();
    }

    private void initializeServers() {

        commandServer = new TcpServerSpec<byte[], byte[]>(NettyTcpServer.class)
                .env(env)
                .listen(getIp(), getCommandPort())
                .codec(StandardCodecs.BYTE_ARRAY_CODEC)
                .consume(new Consumer<TcpConnection<byte[], byte[]>>() {
                    @Override
                    public void accept(final TcpConnection<byte[], byte[]> conn) {

                        LOG.info("Accepted a command connection.");
                        
                        conn.in().consume(new Consumer<byte[]>() {
                            @Override
                            public void accept(byte[] bytes) {
                                try {
                                    CommandMessage message = new CommandMessage().decode(bytes);
                                    LOG.info(message.getMessage());
                                    reactor.notify(CommandMessageType.fromValue(message.getMessage()), Event.wrap(message));

                                    conn.close();
                                } catch (IOException ex) {
                                    LOG.error("Error decoding command message.", ex);
                                }
                            }
                        });
                    }
                })
                .get();

        commandServer.start();

        dataServer = new TcpServerSpec<byte[], byte[]>(NettyTcpServer.class)
                .env(env)
                .listen(getIp(), getDataPort())
                .codec(StandardCodecs.BYTE_ARRAY_CODEC)
                .consume(new Consumer<TcpConnection<byte[], byte[]>>() {
                    @Override
                    public void accept(TcpConnection<byte[], byte[]> conn) {

//                        LOG.info("Accepted a data connection");

                        // for each connection, process incoming data
                        conn.in().consume(new Consumer<byte[]>() {
                            @Override
                            public void accept(byte[] bytes) {
                                
                            }
                        });
                    }
                })
                .get();

        dataServer.start();
    }
}
