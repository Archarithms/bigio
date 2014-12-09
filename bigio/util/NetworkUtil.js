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
var net = require('net');
var os = require('os');
var sync = require('synchronize');
var parameters = require('./../Parameters')
var OperatingSystem = require('./OperatingSystem')

var NETWORK_INTERFACE_PROPERTY = "io.bigio.network";
    
var nic = null;
var inetAddress = null;

var ip = undefined;

var START_PORT = 32768;
var END_PORT = 65536;
var NUM_CANDIDATES = END_PORT - START_PORT + 1;
var port;

module.exports = {
    getIp: function(cb) {
        if(ip == undefined) {
            var nic = parameters.getInstance().getProperty(NETWORK_INTERFACE_PROPERTY);
            if(nic == undefined) {
                var interfaces = os.networkInterfaces();
                var match;

                switch(parameters.getInstance().currentOS()) {
                    case OperatingSystem.WIN_64:
                    case OperatingSystem.WIN_32:
                        match = "Loopback";
                        break;
                    case OperatingSystem.LINUX_64:
                    case OperatingSystem.LINUX_32:
                        match = "lo";
                        break;
                    case OperatingSystem.MAC_64:
                    case OperatingSystem.MAC_32:
                        match = "lo0";
                        break;
                    default:
                        logger.error("Cannot determine operating system. Cluster cannot form.");
                }

                for(var intfc in interfaces) {
                    if(intfc.indexOf("Loopback") > -1) {
                        for(var i in interfaces[intfc]) {
                            if(interfaces[intfc][i]['family'] == 'IPv4') {
                                ip = interfaces[intfc][i]['address'];
                            }
                        }
                    }
                }
            }
        }

        cb(null, ip);
    },

    getFreePort: function(cb) {
        port = Math.floor(Math.random() * NUM_CANDIDATES + START_PORT);
        nextPort(cb);
    }
}

var nextPort = function(cb) {
    var server = net.createServer();

    server.listen(port, function(err) {
        server.once('close', function() {
            cb(null, port);
        })
        server.close();
    });
    server.on('error', function(err) {
        port = Math.floor(Math.random() * NUM_CANDIDATES + START_PORT);
        nextPort(cb);
    })
}

/*
            if(networkInterfaceName == null || "".equals(networkInterfaceName)) {
                switch(Parameters.INSTANCE.currentOS()) {
                    case WIN_64:
                    case WIN_32:
                        networkInterfaceName = "lo";
                        break;
                    case LINUX_64:
                    case LINUX_32:
                        networkInterfaceName = "eth0";
                        break;
                    case MAC_64:
                    case MAC_32:
                        networkInterfaceName = "en0";
                        break;
                    default:
                        LOG.error("Cannot determine operating system. Cluster cannot form.");
                }
            } else {
                networkInterfaceName = networkInterfaceName.trim();
            }
*/
