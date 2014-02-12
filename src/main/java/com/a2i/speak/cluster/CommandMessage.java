/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.cluster;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class CommandMessage {

    private static final Logger LOG = LoggerFactory.getLogger(CommandMessage.class);

    private final MessagePack msgPack = new MessagePack();
    private final Template<Map<String, String>> tagTemplate = Templates.tMap(Templates.TString, Templates.TString);

    private String message;
    private int sequence;
    private String ip;
    private int commandPort;
    private int dataPort;
    private Map<String, String> tags;

    public CommandMessage() {
        tags = new HashMap<>();
    }

    public CommandMessage(String message, int sequence, String ip, int commandPort, int dataPort) {
        this(message, sequence, ip, commandPort, dataPort, new HashMap<String, String>());
    }

    public CommandMessage(String message, int sequence, String ip, int commandPort, int dataPort, Map<String, String> tags) {
        this.message = message;
        this.sequence = sequence;
        this.ip = ip;
        this.commandPort = commandPort;
        this.dataPort = dataPort;
        this.tags = tags;
    }

    public CommandMessage decode(ByteBuffer bytes) throws IOException {

        bytes.get();
        bytes.get();

        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        setMessage(unpacker.readString());
        setSequence(unpacker.readInt());
        setIp(unpacker.readString());
        setCommandPort(unpacker.readInt());
        setDataPort(unpacker.readInt());
        this.tags = unpacker.read(tagTemplate);

        return this;
    }

    public CommandMessage decode(byte[] bytes) throws IOException {

        //Unpacker unpacker = msgPack.createBufferUnpacker(bytes, 2, bytes.length - 2);
        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);

        setMessage(unpacker.readString());
        setSequence(unpacker.readInt());
        setIp(unpacker.readString());
        setCommandPort(unpacker.readInt());
        setDataPort(unpacker.readInt());
        this.tags = unpacker.read(tagTemplate);

        return this;
    }

    public byte[] encode() throws IOException {
        ByteArrayOutputStream msgBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Packer packer = msgPack.createPacker(msgBuffer);
        packer.write(getMessage());
        packer.write(getSequence());
        packer.write(getIp());
        packer.write(getCommandPort());
        packer.write(getDataPort());
        packer.write(getTags());

        out.write((short)msgBuffer.size() >>> 8);
        out.write((short)msgBuffer.size());
        msgBuffer.writeTo(out);

        return out.toByteArray();
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("Command: ").append(getMessage()).append("\n")
                .append("Sequence: ").append(getSequence()).append("\n")
                .append("Address: ").append(getIp()).append("\n")
                .append("CommandPort: ").append(getCommandPort()).append("\n")
                .append("DataPort: ").append(getDataPort()).append("\n")
                .append("Tags: ").append("\n");
        for(String key : getTags().keySet()) {
            buff.append("    ").append(key).append(" -> ").append(getTags().get(key)).append("\n");
        }
        return buff.toString();
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
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
     * @return the commandPort
     */
    public int getCommandPort() {
        return commandPort;
    }

    /**
     * @param commandPort the command port to set
     */
    public void setCommandPort(int commandPort) {
        this.commandPort = commandPort;
    }

    /**
     * @return the tags
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @return the dataPort
     */
    public int getDataPort() {
        return dataPort;
    }

    /**
     * @param dataPort the dataPort to set
     */
    public void setDataPort(int dataPort) {
        this.dataPort = dataPort;
    }

    /**
     * @return the sequence
     */
    public int getSequence() {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
