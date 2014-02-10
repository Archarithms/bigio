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
import java.util.logging.Level;
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
public class LeaveMessage extends AbstractRPCMessage {

    private static final Logger LOG = LoggerFactory.getLogger(LeaveMessage.class);

    private static final long TIMEOUT = 5000l;

    private final MessagePack msgPack = new MessagePack();

    private final String commandName = "Command";
    private final String commandValue = "leave";
    private final String sequenceName = "Seq";

    private final Object sync = new Object();

    public void waitForResponse() {
        synchronized(sync) {
            try {
                sync.wait(TIMEOUT);
            } catch (InterruptedException ex) {
                LOG.error("Interrupted waiting for response.", ex);
            }
        }
    }

    @Override
    public byte[] encode(int seq) throws IOException {
        Map<String, Value> headerMap = new HashMap<>();

        headerMap.put(commandName, ValueFactory.createRawValue(commandValue));
        headerMap.put(sequenceName, ValueFactory.createIntegerValue(seq));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgPack.createPacker(out);
        packer.write(headerMap);
        return out.toByteArray();
    }

    @Override
    public LeaveMessage decode(byte[] bytes) throws IOException {

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        Map<String, Value> header = decodeHeader(bytes, unpacker);

        int seq = header.get("Seq").asIntegerValue().getInt();
        String error = header.get("Error").asRawValue().getString();

        if(!"".equals(error)) {
            LOG.error("Error in RPC seq " + seq + ": " + error);
        }

        synchronized(sync) {
            sync.notifyAll();
        }

        return this;
    }
}
