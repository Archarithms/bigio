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

var logger = require('winston');
var msgpack = require('./msgpack');
var GenericEncoder = require('./GenericEncoder');
var bops = require('bops');

/**
 * This is a class for encoding envelope messages.
 * 
 * @author Andy Trimble
 */
module.exports = {
    
    /**
     * Encode a message envelope.
     * 
     * @param message a message to encode.
     * @return the encoded message.
     * @throws IOException in case of an encode error.
     */
    encode: function(message) {

        var keys = message.senderKey.split(':');
        var ip = keys[0].split('.');

        if(message.isEncrypted) {
            var toPack = [
                parseInt(ip[0]),
                parseInt(ip[1]),
                parseInt(ip[2]),
                parseInt(ip[3]),
                parseInt(keys[1]),
                parseInt(keys[2]),
                true,
                message.key,
                message.executeTime,
                message.millisecondsSinceMidnight,
                message.topic,
                message.partition,
                message.className,
                message.payload
            ];
        } else {
            var toPack = [
                parseInt(ip[0]),
                parseInt(ip[1]),
                parseInt(ip[2]),
                parseInt(ip[3]),
                parseInt(keys[1]),
                parseInt(keys[2]),
                false,
                message.executeTime,
                message.millisecondsSinceMidnight,
                message.topic,
                message.partition,
                message.className,
                message.payload
            ];
        }

        var buff = msgpack.encode(toPack, false);
        var newBuff = bops.create(buff.length + 2);
        bops.copy(buff, newBuff, 2, 0, buff.length);
        bops.writeUInt16BE(newBuff, buff.length, 0);

        return newBuff;
    }
};
