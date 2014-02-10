/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster;

import com.a2i.speak.OperatingSystem;
import com.a2i.speak.Parameters;
import com.a2i.speak.Starter;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
//@Component
public class ClusterService {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterService.class);
    private static final long TIMEOUT = 5000l;
    private static final String LOCALHOST = "127.0.0.1";

    private final AtomicInteger sequence = new AtomicInteger(0);
    private final MessagePack msgPack = new MessagePack();
    private EventLoopGroup workerGroup;

    private final RPCHandler rpc = new RPCHandler();
    private final RPCThread rpcThread = new RPCThread();
    private final HandshakeMessage handshake = new HandshakeMessage();
    private final MemberJoinMessage joinStream = new MemberJoinMessage();
    private final MemberLeaveMessage leaveStream = new MemberLeaveMessage();
    private final MemberFailMessage failStream = new MemberFailMessage();
    private final Map<Integer, RPCMessage> history = new HashMap<>();
    
    private Process serf;

    private String bindAddress = LOCALHOST;
    private String rpcAddress = LOCALHOST;
    private String bindPort = "7946";
    private String rpcPort = "7373";
    private String memberAddress = LOCALHOST;
    private String memberPort = "8989";
    private String memberCommandPort = "8990";
    private String nodeName = UUID.randomUUID().toString();
    private String clusterName = "SpeakEasy";
    
    public ClusterService() {
        initSerf();
        initRPC();
    }

    public Map<String, Member> getAllMembers() {
        return Collections.unmodifiableMap(MemberHolder.INSTANCE.getAllMembers());
    }
    
    public Map<String, Member> getActiveMembers() {
        return Collections.unmodifiableMap(MemberHolder.INSTANCE.getActiveMembers());
    }
    
    public Map<String, Member> getDeadMembers() {
        return Collections.unmodifiableMap(MemberHolder.INSTANCE.getDeadMembers());
    }

    public void members() {
        for(Member member : getAllMembers().values()) {
            LOG.info(member.toString());
        }
//        try {
//            rpc.writeMessage(new MembersMessage());
//        } catch(IOException ex) {
//            LOG.error("Error in listing members.", ex);
//        }
    }

    public void join(String ip) {
        try {
            rpc.writeMessage(new JoinMessage(ip));
        } catch(IOException ex) {
            LOG.error("Error in joining cluster.", ex);
        }
    }

    public void leave() {
        try {
            rpc.writeMessage(new LeaveMessage());
        } catch(IOException ex) {
            LOG.error("Error leaving cluster.", ex);
        }
    }

    public void shutdown() {
        try {
            int leaveSequence = sequence.get();
            rpc.writeMessage(new LeaveMessage());
            ((LeaveMessage)history.get(leaveSequence)).waitForResponse();
        } catch(IOException ex) {
            LOG.error("Error leaving cluster.", ex);
        }
        workerGroup.shutdownGracefully();
        try {
            rpcThread.join(TIMEOUT);
        } catch (InterruptedException ex) {
            LOG.error("Error shutting down RCP connection.", ex);
        }
        serf.destroy();
    }

    private void initSerf() {
        File logFile = new File("logs/serf.log");

        try {
            String networkInterfaceName = "eth0";

            switch(Parameters.INSTANCE.currentOS()) {
                case WIN_64:
                case WIN_32:
                    networkInterfaceName = "net0";
                    break;
                case LINUX_64:
                case LINUX_32:
                    networkInterfaceName = "eth0";
                    break;
                case MAC_64:
                case MAC_32:
                    networkInterfaceName = "eth0";
                    break;
                default:
                    LOG.error("Cannot determine operating system. Cluster cannot form.");
            }
            
            NetworkInterface n = NetworkInterface.getByName(networkInterfaceName);
            Enumeration e = n.getInetAddresses();
            while(e.hasMoreElements()) {
                InetAddress i = (InetAddress) e.nextElement();
                String address = i.getHostAddress();

                if(!address.startsWith("fe")) {
                    bindAddress = address;
                    memberAddress = address;
                    LOG.info("Binding to address " + address + ":" + memberPort);
                }
            }
        } catch(SocketException ex) {
            LOG.error("Unable to determine IP address", ex);
        }

        String executable;

        switch(Parameters.INSTANCE.currentOS()) {
            case WIN_64:
                executable = "bin/amd64/win/serf.exe";
                break;
            case WIN_32:
                executable = "bin/x86/win/serf.exe";
                break;
            case LINUX_64:
                executable = "bin/amd64/linux/serf";
                break;
            case LINUX_32:
                executable = "bin/x86/linux/serf";
                break;
            case MAC_64:
                executable = "bin/amd64/mac/serf";
                break;
            case MAC_32:
                executable = "bin/x86/mac/serf";
                break;
            default:
                LOG.error("Cannot determine operating system. Cluster cannot form.");
                executable = "";
        }

        List<String> serfCommand = new ArrayList<>();

        serfCommand.add(executable);
        serfCommand.add("agent");
        serfCommand.add("-bind");
        serfCommand.add(bindAddress + ":" + bindPort);
        serfCommand.add("-node");
        serfCommand.add(nodeName);
        serfCommand.add("-tag");
        serfCommand.add("ip=" + memberAddress);
        serfCommand.add("-tag");
        serfCommand.add("port=" + memberPort);
        serfCommand.add("-tag");
        serfCommand.add("command_port=" + memberCommandPort);
        serfCommand.add("-discover=" + clusterName);

        ProcessBuilder procBuilder = new ProcessBuilder();
        procBuilder.redirectError(logFile);
        procBuilder.redirectOutput(logFile);
        procBuilder.command(serfCommand.toArray(new String[0]));
        try {
            LOG.info("Starting clustering agent.");
            serf = procBuilder.start();
        } catch (IOException ex) {
            LOG.error("Error starting clustering agent.", ex);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    private void initRPC() {
        rpcThread.start();
    }

    private final class RPCThread extends Thread {
        @Override
        public void run() {

            workerGroup = new NioEventLoopGroup();

            try {
                Bootstrap b = new Bootstrap();
                b.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        if(LOG.isTraceEnabled()) {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE)) ;
                        }

                        ch.pipeline().addLast(
                                new ByteArrayDecoder(), 
                                rpc);
                    }
                });

                // Start the client.
                ChannelFuture f = b.connect(rpcAddress, Integer.parseInt(rpcPort)).sync();

                // Wait until the connection is closed.
                f.channel().closeFuture().sync();
            } catch(InterruptedException ex) {
                LOG.error("Error in RPC call.", ex);
            }

            LOG.info("Connection to Clustering agent closed.");
        }
    }

    private class RPCHandler extends ChannelInboundHandlerAdapter {

        private ChannelHandlerContext ctxt;

        public void writeMessage(RPCMessage message) throws IOException {
            int seq = sequence.getAndIncrement();
            history.put(seq, message);

            byte[] bytes = message.encode(seq);
            ByteBuf buff = Unpooled.buffer(bytes.length);
            buff.writeBytes(bytes);
            ctxt.writeAndFlush(buff);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            this.ctxt = ctx;
            
            LOG.info("Shaking hands");
            try {
                writeMessage(handshake);
            } catch (IOException ex) {
                LOG.error("Cannot serialize message.", ex);
            }

            LOG.debug("Registering for join events");
            try {
                writeMessage(joinStream);
            } catch (IOException ex) {
                LOG.error("Cannot serialize message.", ex);
            }

            LOG.debug("Registering for leave events");
            try {
                writeMessage(leaveStream);
            } catch (IOException ex) {
                LOG.error("Cannot serialize message.", ex);
            }

            LOG.debug("Registering for fail events");
            try {
                writeMessage(failStream);
            } catch (IOException ex) {
                LOG.error("Cannot serialize message.", ex);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof byte[]) {
                
                byte[] bytes = (byte[]) msg;

                Unpacker unpacker = msgPack.createBufferUnpacker(bytes);
                
                Map<String, Value> header = AbstractRPCMessage.decodeHeader(bytes, unpacker);

                int seq = header.get("Seq").asIntegerValue().getInt();
                String error = header.get("Error").asRawValue().getString();

                if(!"".equals(error)) {
                    LOG.error("Error in RPC(" + seq + "): " + error);
                } else {
                    RPCMessage mess = history.get(seq);
                    mess.decode(bytes);
                }
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
           ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOG.error("Exception in connection to Clustering Agent.", cause);
            ctx.close();
        }
    }
}
