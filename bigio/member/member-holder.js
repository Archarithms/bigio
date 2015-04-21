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

var MemberStatus = require('./member-status');

module.exports = {

    members: {},
    activeMembers: {},
    deadMembers: {},

    clear: function() {
        this.members.clear();
        this.activeMembers.clear();
        this.deadMembers.clear();
    },

    getMember: function(key) {
        return this.members[key];
    },

    getAllMembers: function() {
        var ret = [];
        ret.concat(this.members);
        return ret;
    },

    getActiveMembers: function() {
        var ret = [];
        for(var m in this.activeMembers) {
            ret.push(this.activeMembers[m]);
        }
        return ret;
    },

    getDeadMembers: function() {
        var ret = [];
        for(var m in this.deadMembers) {
            ret.push(this.deadMembers[m]);
        }
        return ret;
    },

    updateMemberStatus: function(member) {
        var key = member.ip + ":" + member.gossipPort + ":" + member.dataPort;

        if(key in this.members) {
            if(key in this.activeMembers
                    && (member.status == MemberStatus.Failed
                    || member.status == MemberStatus.Left
                    || member.status == MemberStatus.Unknown)) {
                delete this.activeMembers[key];
                this.deadMembers[key] = member;
            } else if(key in this.deadMembers && member.status == MemberStatus.Alive) {
                delete this.deadMembers[key];
                this.activeMembers[key] = member;
            }
        } else {
            //logger.info('Adding new member at key ' + key);
            this.members[key] = member;
            if(MemberStatus.Alive == member.status) {
                this.activeMembers[key] = member;
            } else {
                this.deadMembers[key] = member;
            }
        }
    }
};
