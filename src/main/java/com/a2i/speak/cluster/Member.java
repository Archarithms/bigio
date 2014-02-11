/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.composable.Stream;
import reactor.function.Consumer;
import reactor.tcp.Reconnect;
import reactor.tcp.TcpClient;
import reactor.tcp.TcpConnection;
import reactor.tcp.encoding.StandardCodecs;
import reactor.tcp.netty.NettyTcpClient;
import reactor.tcp.spec.TcpClientSpec;
import reactor.tuple.Tuple;
import reactor.tuple.Tuple2;

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

    private static final int QUEUE_CAPACITY = 20;

    private final BlockingQueue<CommandMessage> dataQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    private TcpClient<byte[], byte[]> commandClient;
    private TcpClient<byte[], byte[]> dataClient;

    private Status status;

    private final Map<String, String> tags = new HashMap<>();

    private final AtomicInteger sequence = new AtomicInteger(0);

    private static final Environment env = new Environment();

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

    public void sendCommand(final CommandMessage message) {
        commandClient.open().consume(new Consumer<TcpConnection<byte[], byte[]>>() {
            @Override
            public void accept(TcpConnection<byte[], byte[]> conn) {
                try {
                    conn.send(message.encode());
                } catch (IOException ex) {
                    LOG.error("Exception sending command.", ex);
                }

                conn.close();
            }
        });
    }

    public void close() {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Closing TCP connections to " + ip + ":" + commandPort + ":" + dataPort);
        }

        commandClient.close();
//        dataClient.close();
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
        if(obj instanceof Member) {
            Member them = (Member)obj;
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

    public void initClients() {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Initiating command channel to " + ip + ":" + commandPort + ":" + dataPort);
        }

        commandClient = new TcpClientSpec<byte[], byte[]>(NettyTcpClient.class)
                .env(env)
                .codec(StandardCodecs.BYTE_ARRAY_CODEC)
                .connect(ip, commandPort)
                .get();

//        new Thread() {
//            @Override
//            public void run() {
//                while(running) {
//                    try {
//                        final CommandMessage message = commandQueue.take();
//
//                        LOG.info("Got a command");
//                        
//                        commandClient.open().consume(new Consumer<TcpConnection<byte[], byte[]>>() {
//                            @Override
//                            public void accept(TcpConnection<byte[], byte[]> conn) {
//
//                                LOG.info("Connection established");
//
//                                if(LOG.isDebugEnabled()) {
//                                    LOG.debug("Sending command to " + ip + ":" + commandPort + ":" + dataPort);
//                                }
//                                
//                                try {
//                                    conn.send(message.encode());
//                                } catch (IOException ex) {
//                                    LOG.error("Exception sending command.", ex);
//                                }
//                            }
//                        });
//                    } catch (InterruptedException ex) {
//                        LOG.error("Interrupted waiting for a command.", ex);
//                    }
//                }
//            }
//        }.start();

//        dataClient = new TcpClientSpec<byte[], byte[]>(NettyTcpClient.class)
//                .env(env)
//                .codec(StandardCodecs.BYTE_ARRAY_CODEC)
//                .connect(ip, dataPort)
//                .get();
//
//        Stream<TcpConnection<byte[], byte[]>> dataConnections = dataClient.open(new Reconnect() {
//            @Override
//            public Tuple2<InetSocketAddress, Long> reconnect(InetSocketAddress currentAddress, int attempt) {
//
//                if(LOG.isTraceEnabled()) {
//                    LOG.trace("Connection to " + ip + ":" + commandPort + ":" + dataPort + " failed.");
//                }
//
//                if(getStatus() != Status.Unknown) {
//                    setStatus(Status.Failed);
//                }
//
//                return Tuple.of(currentAddress, 500l);
//            }
//        });
//
//        dataConnections.consume(new Consumer<TcpConnection<byte[], byte[]>>() {
//            @Override
//            public void accept(final TcpConnection<byte[], byte[]> conn) {
//
//                if(LOG.isDebugEnabled()) {
//                    LOG.debug("Established data channel with " + ip + ":" + commandPort + ":" + dataPort);
//                }
//
//                setStatus(Status.Alive);
//
//                while(running) {
//                    try {
//                        conn.send(dataQueue.take().encode());
//                    } catch (InterruptedException ex) {
//                        LOG.error("Interrupted while waiting for messages.", ex);
//                    } catch (IOException ex) {
//                        LOG.error("Exception while serializng message.", ex);
//                    }
//                }
//
////                conn.in().consume(new Consumer<byte[]>() {
////                    @Override
////                    public void accept(byte[] bytes) {
////                        try {
////                            conn.send(dataQueue.take().encode());
////                        } catch (InterruptedException ex) {
////                            LOG.error("Interrupted while waiting for messages.", ex);
////                        } catch (IOException ex) {
////                            LOG.error("Exception while serializng message.", ex);
////                        }
////                    }
////                });
//            }
//        });
    }
}
