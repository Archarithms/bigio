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

/**
 * A message that contains the gossiped data.
 * 
 * @author Andy Trimble
 */
var GossipMessage = function() {

};

GossipMessage.prototype.ip = '';
GossipMessage.prototype.gossipPort = -1;
GossipMessage.prototype.dataPort = -1;
GossipMessage.prototype.millisecondsSinceMidnight = -1;
GossipMessage.prototype.publicKey = undefined;
GossipMessage.prototype.tags = {};
GossipMessage.prototype.members = [];
GossipMessage.prototype.clock = [];
GossipMessage.prototype.eventListeners = {};

/**
 * Produce a nice textual representation of the message.
 *
 * @return the message as a string.
 */
GossipMessage.prototype.toString = function() {
    /* StringBuilder buff = new StringBuilder();
    buff.append("GossipMessage: ").append("\n")
            .append("Address: ").append(getIp()).append("\n")
            .append("GossipPort: ").append(getGossipPort()).append("\n")
            .append("DataPort: ").append(getDataPort()).append("\n")
            .append("Time: ").append(getMillisecondsSinceMidnight()).append("\n")
            .append("Tags: ").append("\n");
    for(String key : getTags().keySet()) {
        buff.append("    ").append(key).append(" -> ").append(getTags().get(key)).append("\n");
    }
    buff.append("Members: ").append("\n");
    for(int i = 0; i < getMembers().size(); ++i) {
        buff.append("    ").append(getMembers().get(i)).append(" -- ").append(getClock().get(i)).append("\n");
    }
    buff.append("Listeners: ").append("\n");
    for(String key : getListeners().keySet()) {
        buff.append("    ").append(key).append("\n");
        for(String topic : getListeners().get(key)) {
            buff.append("        ").append(topic).append("\n");
        }
    }
    return buff.toString();*/
};

module.exports = GossipMessage;