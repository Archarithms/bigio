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


var MAX_DEPTH;
var properties;
var fileSystem;
var configDir;
var os;
var operatingSystem = require('os');
var OperatingSystem = require('./util/operating-system')

var properties = {};

var Singleton = (function () {
    var instance;

    function createInstance() {

        MAX_DEPTH = 10;

        var osName = operatingSystem.platform();
        var osArch = operatingSystem.arch();

        if (osName == "win32") {
            if (osArch == "x64") {
                os = OperatingSystem.WIN_64;
            } else {
                os = OperatingSystem.WIN_32;
            }
        } else if (osName == "linux") {
            if (osArch.contains("x64")) {
                os = OperatingSystem.LINUX_64;
            } else {
                os = OperatingSystem.LINUX_32;
            }
        } else {
            if (osArch.contains("x64")) {
                os = OperatingSystem.MAC_64;
            } else {
                os = OperatingSystem.MAC_32;
            }
        }

        // TODO: Load configuration files here

        return {

            getProperty: function (name) {
                return properties[name];
            },

            getProperty: function (name, defaultValue) {
                if(!(name in properties)) {
                    return defaultValue;
                }
                return properties[name];
            },

            setProperty: function (name, value) {
                properties[name] = value;
            },

            currentOS: function () {
                return os;
            }
        };
    };

    return {
        getInstance: function () {
            if (!instance) {
                instance = createInstance();
            }
            return instance;
        }
    };
})();

module.exports = Singleton;
