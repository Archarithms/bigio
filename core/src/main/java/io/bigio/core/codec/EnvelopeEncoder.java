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

import io.bigio.core.Envelope;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

/**
 * This is a class for encoding envelope messages.
 * 
 * @author Andy Trimble
 */
public class EnvelopeEncoder {

    private static final MessagePack msgPack = new MessagePack();
    
    /**
     * Encode a message envelope.
     * 
     * @param message a message to encode.
     * @return the encoded message.
     * @throws IOException in case of an encode error.
     */
    public static byte[] encode(Envelope message) throws IOException {
        ByteArrayOutputStream msgBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        Packer packer = msgPack.createPacker(msgBuffer);

        String[] keys = message.getSenderKey().split(":");
        String[] ip = keys[0].split("\\.");
        packer.write(Integer.parseInt(ip[0]));
        packer.write(Integer.parseInt(ip[1]));
        packer.write(Integer.parseInt(ip[2]));
        packer.write(Integer.parseInt(ip[3]));
        packer.write(Integer.parseInt(keys[1]));
        packer.write(Integer.parseInt(keys[2]));

        packer.write(message.isEncrypted());
        if(message.isEncrypted()) {
            packer.write(message.getKey());
        }
        packer.write(message.getExecuteTime());
        packer.write(message.getMillisecondsSinceMidnight());
        packer.write(message.getTopic());
        packer.write(message.getPartition());
        packer.write(message.getClassName());
        packer.write(message.getPayload());

        out.write((short)msgBuffer.size() >>> 8);
        out.write((short)msgBuffer.size());
        msgBuffer.writeTo(out);

        return out.toByteArray();
    }
}
