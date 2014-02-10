/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.msgpack.MessagePack;
import org.msgpack.io.EndOfBufferException;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class JoinMessage extends AbstractRPCMessage {

    private static final Logger LOG = LoggerFactory.getLogger(JoinMessage.class);

    private final MessagePack msgPack = new MessagePack();

    private final String commandName = "Command";
    private final String commandValue = "join";
    private final String sequenceName = "Seq";
    private final String existingName = "Existing";
    private final String replayName = "Replay";
    private final String ip;

    public JoinMessage(String ip) {
        this.ip = ip;
    }

    @Override
    public byte[] encode(int seq) throws IOException {
        Map<String, Value> headerMap = new HashMap<>();
        Map<String, Object> bodyMap = new HashMap<>();

        headerMap.put(commandName, ValueFactory.createRawValue(commandValue));
        headerMap.put(sequenceName, ValueFactory.createIntegerValue(seq));

        List<String> ips = new ArrayList<>();
        ips.add(ip);
//        ips.add("127.0.0.1:7946");
        
        bodyMap.put(existingName, ips);
        bodyMap.put(replayName, false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgPack.createPacker(out);
        packer.write(headerMap);
        packer.write(bodyMap);
        return out.toByteArray();
    }

    @Override
    public JoinMessage decode(byte[] bytes) throws IOException {

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        Map<String, Value> header = decodeHeader(bytes, unpacker);

        int seq = header.get("Seq").asIntegerValue().getInt();
        String error = header.get("Error").asRawValue().getString();

        if(!"".equals(error)) {
            LOG.error("Error in RPC seq " + seq + ": " + error);
        }

        Template<Map<String, Integer>> template
                = Templates.tMap(Templates.TString, Templates.TInteger);

        try {
            Map<String, Integer> map = unpacker.read(template);

            if(map.containsKey("Num")) {
                LOG.info(map.get("Num") + " members in cluster");
            }
        } catch(IOException ex) {
            
        }
        
        return this;
    }
}
