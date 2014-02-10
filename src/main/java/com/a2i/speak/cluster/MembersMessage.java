/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.msgpack.MessagePack;
import org.msgpack.io.EndOfBufferException;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.type.MapValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class MembersMessage extends AbstractRPCMessage {

    private final MessagePack msgPack = new MessagePack();

    private static final Logger LOG = LoggerFactory.getLogger(MembersMessage.class);

    private final String commandName = "Command";
    private final String commandValue = "members";
    private final String sequenceName = "Seq";

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
    public MembersMessage decode(byte[] bytes) throws IOException {

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        Map<String, Value> header = decodeHeader(bytes, unpacker);

        int seq = header.get("Seq").asIntegerValue().getInt();
        String error = header.get("Error").asRawValue().getString();

        if(!"".equals(error)) {
            LOG.error("Error in RPC seq " + seq + ": " + error);
            return this;
        }

        Template<Map<String, List<Map<String, Value>>>> template
                = Templates.tMap(Templates.TString, Templates.tList(Templates.tMap(Templates.TString, Templates.TValue)));

        try {
            Map<String, List<Map<String, Value>>> map = unpacker.read(template);

            for (String members : map.keySet()) {
                for (Map<String, Value> m : map.get(members)) {
                    processMember(m);
                }
            }
        } catch(IOException ex) {

        }

        return this;
    }

    protected void processMember(Map<String, Value> m) throws IOException {
        StringBuilder ipBuilder = new StringBuilder();

        byte[] arr = m.get("Addr").asRawValue().getByteArray();
        Unpacker ipUnpacker = msgPack.createUnpacker(new ByteArrayInputStream(arr));
        int length = arr.length;
        for (int i = 0; i < length; ++i) {
            Integer octet = ipUnpacker.read(Integer.class);
            if(octet == null) {
                break;
            }
            ipBuilder.append(octet);
            if((i + 1) % 4 == 0) {
                ipBuilder.append("; ");
            } else {
                ipBuilder.append(".");
            }
        }

        String name = m.get("Name").asRawValue().getString();

        Member member = MemberHolder.INSTANCE.getAllMembers().get(name);

        if(member == null) {
            member = new Member();
            member.setName(name);
            MapValue tagMap = m.get("Tags").asMapValue();
            for (Value tag : tagMap.keySet()) {
                member.getTags().put(tag.asRawValue().getString(), tagMap.get(tag).asRawValue().getString());
            }
            if(member.getTags().containsKey("ip")) {
                member.setIp(member.getTags().get("ip"));
            } else {
                LOG.warn("Discovered member without an IP address.");
            }
            if(member.getTags().containsKey("port")) {
                member.setPort(Integer.parseInt(member.getTags().get("port")));
            } else {
                LOG.warn("Discovered member without a port.");
            }
            if(member.getTags().containsKey("command_port")) {
                member.setCommandPort(Integer.parseInt(member.getTags().get("command_port")));
            } else {
                LOG.warn("Discovered member without a command port.");
            }

            MemberHolder.INSTANCE.getAllMembers().put(name, member);
        }
        
        member.setStatus(Member.Status.fromString(m.get("Status").asRawValue().getString()));

        if(LOG.isDebugEnabled()) {
            LOG.debug("Node name " + m.get("Name").asRawValue().getString());
            LOG.debug("Status " + m.get("Status").asRawValue().getString());
            LOG.debug("Tags ");
            MapValue tagMap = m.get("Tags").asMapValue();
            for (Value tag : tagMap.keySet()) {
                LOG.debug("    " + tag.asRawValue().getString() + " -> " + tagMap.get(tag).asRawValue().getString());
            }
        }
    }
}
