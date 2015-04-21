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
var events = require('events');

var TopicUtils = require('../util/TopicUtils');

var me;
var map = {};
var interceptors = {};
var reactor = new events.EventEmitter();

/**
 * A class for managing listener registrations.
 * 
 * @author Andy Trimble
 */
module.exports = {

    /**
     * Add a topic interceptor.
     * 
     * @param topic a topic.
     * @param interceptor an interceptor.
     */
    addInterceptor: function(topic, interceptor) {
        if(interceptors[topic] == undefined) {
            interceptors[topic] = [];
        }
        interceptors[topic].push(interceptor);
    },

    /**
     * Set the current member.
     * 
     * @param me the current member.
     */
    initialize: function(me) {
        this.me = me;
    },

    /**
     * Get the current member.
     * @return the current member.
     */
    getMe: function() {
        return me;
    },

    /**
     * Add a listener that is located in the same VM as the current member.
     * 
     * @param <T> a message type.
     * @param topic a topic.
     * @param partition a partition.
     * @param listener a listener.
     */
    addLocalListener: function(topic, partition, listener) {
        reactor.addListener(TopicUtils.getTopicString(topic, partition), listener);
    },

    /**
     * Remove all local listeners on a given topic.
     * 
     * @param topic a topic.
     */
    removeAllLocalListeners: function(topic) {
        var allRegs = map[me];
        
        if(allRegs != undefined) {
            var regs = allRegs[topic];

            if(regs != undefined) {
                logger.debug("Removing " + regs.size() + " registration");
                regs.clear();
            } else {
                logger.debug("No listeners registered for topic " + topic);
            }
        }
    },

    /**
     * Remove topic/partition registrations. 
     * 
     * @param regs a set of registrations.
     */
    removeRegistrations: function(regs) {
        for(var memberKey in map) {
            for(var key in map[memberKey]) {
                delete map[memberKey][key][regs];
            }
        }
    },

    /**
     * Get all topic/partition registrations. 
     * 
     * @return the list of all registrations.
     */
    getAllRegistrations: function() {
        var ret = [];

        for(var memberKey in map) {
            for(var reg in map[memberKey]) {
                for(var indx in map[memberKey][reg]) {
                    ret.push(map[memberKey][reg][indx]);
                }
            }
        }

        return ret;
    },

    /**
     * Get all members that have at least one listener registered for a given 
     * topic.
     * 
     * @param topic a topic.
     * @return all members that have at least one registered listener.
     */
    getRegisteredMembers: function(topic) {
        var ret = [];

        for(var member in map) {
            for(var regs in map[member]) {
                for(var indx in map[member][regs]) {
                    var key = map[member][regs][indx].topic;
                    if(key == topic) {
                        ret.push(map[member][regs][indx]);
                    }
                }
            }
        }

        return ret;
    },

    /**
     * Register a member for a topic-partition.
     * 
     * @param topic a topic.
     * @param partition a partition.
     * @param member a member.
     */
    registerMemberForTopic: function(topic, partition, member) {

        var memberKey = member.ip + ':' + member.gossipPort + ':' + member.dataPort;

        if(map[memberKey] == undefined) {
            map[memberKey] = {};
        }

        if(map[memberKey][topic] == undefined) {
            map[memberKey][topic]  = [];
        }

        var found = false;
        for(var reg in map[memberKey][topic]) {
            var thatMember = map[memberKey][topic][reg].member;
            var thatMemberKey = thatMember.ip + ':' + thatMember.gossipPort + ':' + thatMember.dataPort;

            if(String(topic) === String(map[memberKey][topic][reg].topic) && String(partition) === String(map[memberKey][topic][reg].partition) && memberKey == thatMemberKey) {
                found = true;
                break;
            }
        }

        if(!found) {
            var reg = {};
            reg.member = member;
            reg.topic = String(topic);
            reg.partition = String(partition);
            map[memberKey][topic].push(reg);
        }
    },

    /**
     * Send a message.
     * 
     * @param envelope a message envelope.
     * @throws IOException in case of a sending error.
     */
    send:function(envelope) {
        if(envelope.topic in Object.keys(interceptors)) {
            for(var interceptor in interceptors[envelope.topic]) {
                envelope = interceptor.intercept(envelope);
            }
        }

        if(envelope.executeTime > 0) {
            reactor.setTimeout(function() {
                reactor.emit(TopicUtils.getTopicString(envelope.topic, envelope.partition), envelope.message);
            }, envelope.executeTime);
        } else if(envelope.executeTime >= 0) {
            reactor.emit(TopicUtils.getTopicString(envelope.topic, envelope.partition), envelope.message);
        }
    }
};
