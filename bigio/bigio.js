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

var logger = require('winston')
var cluster = require('./cluster')

module.exports = {

    initialize: function (cb) {
        cluster.initialize(function() {
            logger.info("The speaker has arrived");
            cb();
        });
        process.on('SIGINT', function() {
            logger.info("Goodbye");
            cluster.shutdown(function() {
                process.exit();
            });
        });
    },

    shutdown: function () {
        cluster.shutdown();
    },

    send: function (obj) {
        cluster.sendMessage(
            obj['topic'],
            'partition' in obj ? obj['partition'] : '',
            obj['message'],
            'javaclass' in obj ? obj['javaclass'] : '',
            'offset' in obj ? obj['offset'] : '');
    },

    addListener: function (obj) {
        cluster.addListener(
            obj['topic'],
            'partition' in obj ? obj['partition'] : '',
            obj['listener']);
    },

    removeAllListeners: function (topic) {
        cluster.removeAllListeners(topic);
    },

    listMembers: function () {
        return cluster.getActiveMembers();
    },

    addInterceptor: function (topic, interceptor) {
        cluster.addInterceptor(topic, interceptor);
    },

    getMe: function () {
        return cluster.getMe();
    },

    getTags: function () {
        return cluster.getMe().getTags();
    },

    setDeliveryType: function (topic, type) {
        cluster.setDeliveryType(topic, type);
    }
};
