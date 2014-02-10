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
import org.msgpack.io.EndOfBufferException;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Converter;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class MemberLeaveMessage extends AbstractRPCMessage {

    private final MessagePack msgPack = new MessagePack();

    private static final Logger LOG = LoggerFactory.getLogger(MemberLeaveMessage.class);

    private final String commandName = "Command";
    private final String commandValue = "stream";
    private final String sequenceName = "Seq";
    private final String bodyName = "Type";
    private final String filter = "member-leave";

    @Override
    public byte[] encode(int seq) throws IOException {
        Map<String, Value> headerMap = new HashMap<>();
        Map<String, Value> bodyMap = new HashMap<>();

        headerMap.put(commandName, ValueFactory.createRawValue(commandValue));
        headerMap.put(sequenceName, ValueFactory.createIntegerValue(seq));

        bodyMap.put(bodyName, ValueFactory.createRawValue(filter));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgPack.createPacker(out);

        packer.write(headerMap);
        packer.write(bodyMap);

        return out.toByteArray();
    }

    @Override
    public MemberLeaveMessage decode(byte[] bytes) throws IOException {

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);
        
        Map<String, Value> header = decodeHeader(bytes, unpacker);

        int seq = header.get("Seq").asIntegerValue().getInt();
        String error = header.get("Error").asRawValue().getString();

        if(!"".equals(error)) {
            LOG.error("Error in RPC seq " + seq + ": " + error);
            return this;
        }

        Template<Map<String, Value>> template
                = Templates.tMap(Templates.TString, Templates.TValue);

        try {
            Map<String, Value> map = unpacker.read(template);

            if(map.containsKey("Event")) {
                LOG.info(map.get("Event").asRawValue().getString());
                ArrayValue members = map.get("Members").asArrayValue();

                for(Value member : members) {
                    Map<String, Value> m = new Converter(member).read(Templates.tMap(Templates.TString, Templates.TValue));
                    new MembersMessage().processMember(m);
                }
            }
        } catch(IOException ex) {
            
        }

        return this;
    }
}
