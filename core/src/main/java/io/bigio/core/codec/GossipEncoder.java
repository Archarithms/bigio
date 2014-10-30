/*
 * Copyright (c) 2014, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package io.bigio.core.codec;

import io.bigio.core.GossipMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for encoding gossip messages.
 * 
 * @author Andy Trimble
 */
public class GossipEncoder {

    private static final Logger LOG = LoggerFactory.getLogger(GossipEncoder.class);

    private static final MessagePack msgPack = new MessagePack();

    private GossipEncoder() {

    }
    
    /**
     * Encode a gossip message.
     * 
     * @param message a message.
     * @return the encoded form of the message.
     * @throws IOException in case of an encoding error.
     */
    public static byte[] encode(GossipMessage message) throws IOException {
        ByteArrayOutputStream msgBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        String[] splitIp = message.getIp().split("\\.");
        List<List<Integer>> members = new ArrayList<>();
        for(String m : message.getMembers()) {
            List<Integer> memberList = new ArrayList<>();
            String[] keys = m.split(":");
            String[] memIp = keys[0].split("\\.");
            if(memIp.length < 4) {
                LOG.warn(keys[0] + " is not a valid IP address.");
                continue;
            }
            memberList.add(Integer.parseInt(memIp[0]));
            memberList.add(Integer.parseInt(memIp[1]));
            memberList.add(Integer.parseInt(memIp[2]));
            memberList.add(Integer.parseInt(memIp[3]));
            memberList.add(Integer.parseInt(keys[1]));
            memberList.add(Integer.parseInt(keys[2]));
            members.add(memberList);
        }

        Packer packer = msgPack.createPacker(msgBuffer);
        packer.write(Integer.parseInt(splitIp[0]));
        packer.write(Integer.parseInt(splitIp[1]));
        packer.write(Integer.parseInt(splitIp[2]));
        packer.write(Integer.parseInt(splitIp[3]));
        packer.write(message.getGossipPort());
        packer.write(message.getDataPort());
        packer.write(message.getMillisecondsSinceMidnight());
        if(message.getPublicKey() != null) {
            packer.write(true);
            packer.write(message.getPublicKey());
        } else {
            packer.write(false);
        }
        packer.write(message.getTags());
        packer.write(members);
        packer.write(message.getClock());
        packer.write(message.getListeners());

        out.write((short)msgBuffer.size() >>> 8);
        out.write((short)msgBuffer.size());
        msgBuffer.writeTo(out);

        return out.toByteArray();
    }
}
