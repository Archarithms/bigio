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

var MemberHolder = require('./member/MemberHolder');
var ListenerRegistry = require('./member/ListenerRegistry');
var Parameters = require('./Parameters');
var GossipMessage = require('./GossipMessage');
var TimeUtil = require('./util/TimeUtil');

var GOSSIP_INTERVAL_PROPERTY = "io.bigio.gossip.interval";
var CLEANUP_INTERVAL_PROPERTY = "io.bigio.gossip.cleanup";
var DEFAULT_GOSSIP_INTERVAL = 250;
var DEFAULT_CLEANUP_INTERVAL = 10000;

var gossipInterval = Parameters.getInstance().getProperty(GOSSIP_INTERVAL_PROPERTY, DEFAULT_GOSSIP_INTERVAL);
var cleanupInterval = Parameters.getInstance().getProperty(CLEANUP_INTERVAL_PROPERTY, DEFAULT_CLEANUP_INTERVAL);

var me;

/**
 * This is the gossip protocol implementation.
 * 
 * @author Andy Trimble
 */
module.exports = {

    /**
     * Star the gossiping task.
     */
    initialize: function(me) {
        this.me = me;

        // start the periodic task
        setInterval(function() {
            var member = undefined;

            var chosenMember = undefined;

            var activeKeys = Object.keys(MemberHolder.activeMembers);
            var activeMemberNum = activeKeys.length;

            if (activeMemberNum > 1) {
                var tries = 10;
                do {
                    var randomNeighborIndex = Math.floor(Math.random() * activeMemberNum);
                    var chosenKey = activeKeys[randomNeighborIndex];
                    chosenMember = MemberHolder.activeMembers[chosenKey];

                    if (--tries <= 0) {
                        chosenMember = undefined;
                        break;
                    }
                } while (me.equals(chosenMember));
            }

            if(!me.equals(chosenMember)) {
                member = chosenMember;
            }


            if (member != undefined) {
                var memberList = new GossipMessage();
                memberList.ip = me.ip;
                memberList.gossipPort = me.gossipPort;
                memberList.dataPort = me.dataPort;
                memberList.millisecondsSinceMidnight = TimeUtil.getMillisecondsSinceMidnight();
                memberList.publicKey = me.publicKey;
                memberList.tags = me.tags;
                memberList.members = [];
                memberList.eventListeners = {};

                for(var i = 0; i < activeMemberNum; ++i) {
                    var k = activeKeys[i];
                    var m = MemberHolder.activeMembers[k];
                    memberList.members.push(m.ip + ":" + m.gossipPort + ":" + m.dataPort);

                    if(m == me) {
                        m.sequence += 1;
                    }
                    memberList.clock[i] = m.sequence;
                }

                var regs = ListenerRegistry.getAllRegistrations();
                for(var indx in regs) {
                    var key = regs[indx].member.ip + ":" + regs[indx].member.gossipPort + ":" + regs[indx].member.dataPort;
                    if(memberList.eventListeners[key] == undefined) {
                        memberList.eventListeners[key] = [];
                    }
                    memberList.eventListeners[key].push(regs[indx].topic);
                }

                logger.info(memberList.eventListeners);

                member.gossip(memberList);
            }
        }, gossipInterval);
    },

    /**
     * Shutdown the gossiping task.
     */
    shutdown: function(cb) {
        // TODO: Gracefully shut down the gossip task
        cb();
    }
};
