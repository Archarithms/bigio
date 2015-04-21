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

var winston = require('winston')
var logger = new (winston.Logger)({
    transports: [
        new (winston.transports.Console)({ level: 'debug' })
        //new (winston.transports.File)({ filename: 'somefile.log' })
    ]
});
var parameters = require('./parameters');
var GossipMessage = require('./gossip-message');
var GossipEncoder = require('./codec/gossip-encoder');
var GossipDecoder = require('./codec/gossip-decoder');
var MemberHolder = require('./member/member-holder');
var MemberStatus = require('./member/member-status');
var RemoteMember = require('./member/remote-member');
var TimeUtil = require('./util/time-util');
var dgram = require('dgram');

var MULTICAST_ENABLED_PROPERTY = "io.bigio.multicast.enabled";
var MULTICAST_GROUP_PROPERTY = "io.bigio.multicast.group";
var MULTICAST_PORT_PROPERTY = "io.bigio.multicast.port";
var DEFAULT_MULTICAST_GROUP = "239.0.0.1";
var DEFAULT_MULTICAST_PORT = 8989;
var PROTOCOL_PROPERTY = "io.bigio.protocol";
var DEFAULT_PROTOCOL = "tcp";
var NETWORK_INTERFACE_PROPERTY = "io.bigio.network";

var enabled = parameters.getInstance().getProperty(MULTICAST_ENABLED_PROPERTY, "true");
var multicastGroup = parameters.getInstance().getProperty(MULTICAST_GROUP_PROPERTY, DEFAULT_MULTICAST_GROUP);
var multicastPort = parameters.getInstance().getProperty(MULTICAST_PORT_PROPERTY, DEFAULT_MULTICAST_PORT);
var protocol = parameters.getInstance().getProperty(PROTOCOL_PROPERTY, DEFAULT_PROTOCOL);
var nic = parameters.getInstance().getProperty(NETWORK_INTERFACE_PROPERTY);

var me;
var server;
var client;

module.exports = {

    shutdown: function(cb) {
        client.close();
        cb();
    },

    setupNetworking: function(cb) {

        server = dgram.createSocket('udp4');
        client = dgram.createSocket('udp4');

        client.on('message', function (data, rinfo) {

            var message = GossipDecoder.decode(data);

            var key = message.ip + ":" + message.gossipPort + ":" + message.dataPort;

            var member = MemberHolder.getMember(key);

            if (member == undefined) {
                if ("udp" == protocol) {
                    logger.debug("Discovered new UDP member: " + message.ip + ":" + message.gossipPort + ":" + message.dataPort);
                    member = new RemoteMember(message.ip, message.gossipPort, message.dataPort, false);
                    member.status = MemberStatus.Alive;
                } else {
                    logger.debug("Discovered new TCP member: " + message.ip + ":" + message.gossipPort + ":" + message.dataPort);
                    member = new RemoteMember(message.ip, message.gossipPort, message.dataPort, true);
                    member.status = MemberStatus.Alive;
                }

                if (message.publicKey != undefined) {
                    member.publicKey = message.publicKey;
                }

                member.initialize();
            } else {
                logger.debug("Found known member: " + message.ip + ":" + message.gossipPort + ":" + message.dataPort);
            }

            for (var k in message.tags) {
                member.tags[k] = message.tags[k];
            }

            MemberHolder.updateMemberStatus(member);
        });


        client.on('listening', function () {
            logger.info('Listening on MC port ' + multicastPort + ' and group ' + multicastGroup);
        });

        client.on('error', function (err) {
            logger.error('MC Discovery Error:\n' + err.stack);
        });

        client.bind(multicastPort, function () {
            client.addMembership(multicastGroup, nic);
            client.setBroadcast(true);
            client.setMulticastTTL(4);

            announce();
            cb();
            /* server.bind(multicastPort, function () {
                server.addMembership(multicastGroup, nic);
                server.setBroadcast(true);
                server.setMulticastTTL(4);

                announce();
                cb();
            });*/
        });
    },

    initialize: function(_me, cb) {
        me = _me;
        this.setupNetworking(cb);
    }
};

var announce = function() {
    logger.info("Announcing");
    var message = new GossipMessage();
    message.ip = me.ip;
    message.gossipPort = me.gossipPort;
    message.dataPort = me.dataPort;
    message.millisecondsSinceMidnight = TimeUtil.getMillisecondsSinceMidnight();
    for (var key in me.tags) {
        message.tags[key] = me.tags[key];
    }
    message.members = [me.ip + ":" + me.gossipPort + ":" + me.dataPort];
    me.sequence += 1;
    message.clock.push(me.sequence);
    message.publicKey = me.publicKey;
    message.eventListeners = {};

    var bytes = new Buffer(GossipEncoder.encode(message));
    server.send(bytes, 0, bytes.length, multicastPort, multicastGroup, function() {
        server.close();
    });
};