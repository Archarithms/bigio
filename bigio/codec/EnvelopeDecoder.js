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
var bops = require('bops');
var msgpack = require('./msgpack');
var Envelope = require('../Envelope');

/**
 * This is a class for decoding gossip messages.
 *
 * @author Andy Trimble
 */
module.exports = {

    /**
     * Decode a message.
     *
     * @param bytes the raw message.
     * @return the decoded message.
     * @throws IOException in case of an error in decoding.
     */
    decode: function(bytes) {
        var message = {};

        var unpacked = msgpack.decode(bytes.slice(2, bytes.length), false);

        var index = 0;

        var ip = unpacked[index++] + '.' + unpacked[index++] + '.' + unpacked[index++] + '.' + unpacked[index++];
        var gossipPort = unpacked[index++];
        var dataPort = unpacked[index++];

        message.senderKey = ip + ':' + gossipPort + ':' + dataPort;

        var encrypted = unpacked[index++];
        message.isEncrypted = encrypted;

        if(encrypted === "true") {
            logger.info("Message encrypted");
            message.key = unpacked[index++];
        }

        message.executeTime = unpacked[index++];
        message.millisecondsSinceMidnight = unpacked[index++];
        message.topic = unpacked[index++];
        message.partition = unpacked[index++];
        message.className = unpacked[index++];
        message.payload = bops.from(unpacked[index], encoding="utf8");

        //logger.info('Decoding payload');
        //message.payload = msgpack.decode(payload, false);
        //message.payload = payload;

        /* logger.info("Sender key: " + message.senderKey);
        logger.info("Encrypted: " + message.isEncrypted);
        logger.info("Execution Time: " + message.executeTime);
        logger.info("MS Since Midnight: " + message.millisecondsSinceMidnight);
        logger.info("Topic: " + message.topic);
        logger.info("Partition: " + message.partition);
        */

        return message;
    }
};