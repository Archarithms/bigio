package com.a2i.sim.core;

///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package com.a2i.speak.cluster.gossip;
//
//import com.a2i.speak.cluster.RPCMessage;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.bytes.ByteArrayDecoder;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//import java.io.IOException;
//import java.util.Map;
//import org.msgpack.MessagePack;
//import org.msgpack.type.Value;
//import org.msgpack.unpacker.Unpacker;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// *
// * @author atrimble
// */
//public class TCPDiscovery {
//    private final MessagePack msgPack = new MessagePack();
//    private EventLoopGroup workerGroup;
//
//    private static final Logger LOG = LoggerFactory.getLogger(TCPDiscovery.class);
//    
//    private final class RPCThread extends Thread {
//        @Override
//        public void run() {
//
//            workerGroup = new NioEventLoopGroup();
//
//            try {
//                Bootstrap b = new Bootstrap();
//                b.group(workerGroup)
//                        .channel(NioSocketChannel.class)
//                        .option(ChannelOption.TCP_NODELAY, true)
//                        .handler(new ChannelInitializer<SocketChannel>() {
//                    @Override
//                    public void initChannel(SocketChannel ch) throws Exception {
//                        if(LOG.isTraceEnabled()) {
//                            ch.pipeline().addLast(new LoggingHandler(LogLevel.TRACE)) ;
//                        }
//
//                        ch.pipeline().addLast(
//                                new ByteArrayDecoder(), 
//                                rpc);
//                    }
//                });
//
//                // Start the client.
//                ChannelFuture f = b.connect(rpcAddress, Integer.parseInt(rpcPort)).sync();
//
//                // Wait until the connection is closed.
//                f.channel().closeFuture().sync();
//            } catch(InterruptedException ex) {
//                LOG.error("Error in RPC call.", ex);
//            }
//
//            LOG.info("Connection to Clustering agent closed.");
//        }
//    }
//
//    private class RPCHandler extends ChannelInboundHandlerAdapter {
//
//        private ChannelHandlerContext ctxt;
//
//        public void writeMessage(RPCMessage message) throws IOException {
//            int seq = sequence.getAndIncrement();
//            history.put(seq, message);
//
//            byte[] bytes = message.encode(seq);
//            ByteBuf buff = Unpooled.buffer(bytes.length);
//            buff.writeBytes(bytes);
//            ctxt.writeAndFlush(buff);
//        }
//
//        @Override
//        public void channelActive(ChannelHandlerContext ctx) {
//            this.ctxt = ctx;
//            
//            LOG.info("Shaking hands");
//            try {
//                writeMessage(handshake);
//            } catch (IOException ex) {
//                LOG.error("Cannot serialize message.", ex);
//            }
//
//            LOG.debug("Registering for join events");
//            try {
//                writeMessage(joinStream);
//            } catch (IOException ex) {
//                LOG.error("Cannot serialize message.", ex);
//            }
//
//            LOG.debug("Registering for leave events");
//            try {
//                writeMessage(leaveStream);
//            } catch (IOException ex) {
//                LOG.error("Cannot serialize message.", ex);
//            }
//
//            LOG.debug("Registering for fail events");
//            try {
//                writeMessage(failStream);
//            } catch (IOException ex) {
//                LOG.error("Cannot serialize message.", ex);
//            }
//        }
//
//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            if (msg instanceof byte[]) {
//                
//                byte[] bytes = (byte[]) msg;
//
//                Unpacker unpacker = msgPack.createBufferUnpacker(bytes);
//                
//                Map<String, Value> header = AbstractRPCMessage.decodeHeader(bytes, unpacker);
//
//                int seq = header.get("Seq").asIntegerValue().getInt();
//                String error = header.get("Error").asRawValue().getString();
//
//                if(!"".equals(error)) {
//                    LOG.error("Error in RPC(" + seq + "): " + error);
//                } else {
//                    RPCMessage mess = history.get(seq);
//                    mess.decode(bytes);
//                }
//            }
//        }
//
//        @Override
//        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//           ctx.flush();
//        }
//
//        @Override
//        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//            LOG.error("Exception in connection to Clustering Agent.", cause);
//            ctx.close();
//        }
//    }
//}
