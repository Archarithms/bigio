/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class HandshakeMessage extends AbstractRPCMessage {

    private static final Logger LOG = LoggerFactory.getLogger(HandshakeMessage.class);

    private final MessagePack msgPack = new MessagePack();

    private final String commandName = "Command";
    private final String commandValue = "handshake";
    private final String sequenceName = "Seq";
    private final String versionName = "Version";
    private final int versionValue = 1;

    @Override
    public byte[] encode(int seq) throws IOException {
        Map<String, Value> headerMap = new HashMap<>();
        Map<String, Integer> bodyMap = new HashMap<>();

        headerMap.put(commandName, ValueFactory.createRawValue(commandValue));
        headerMap.put(sequenceName, ValueFactory.createIntegerValue(seq));
        bodyMap.put(versionName, versionValue);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgPack.createPacker(out);
        packer.write(headerMap);
        packer.write(bodyMap);
        return out.toByteArray();
    }

    @Override
    public HandshakeMessage decode(byte[] bytes) throws IOException {

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        Map<String, Value> header = decodeHeader(bytes, unpacker);

        int seq = header.get("Seq").asIntegerValue().getInt();
        String error = header.get("Error").asRawValue().getString();

        if(!"".equals(error)) {
            LOG.error("Error in RPC seq " + seq + ": " + error);
        }

        return this;
    }
}
