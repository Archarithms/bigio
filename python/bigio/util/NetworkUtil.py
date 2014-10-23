#
# Copyright (c) 2014, Archarithms Inc.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this
# list of conditions and the following disclaimer. 
# 2. Redistributions in binary form must reproduce the above copyright notice,
# this list of conditions and the following disclaimer in the documentation
# and/or other materials provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# The views and conclusions contained in the software and documentation are those
# of the authors and should not be interpreted as representing official policies, 
# either expressed or implied, of the FreeBSD Project.
#

import logging
import netifaces
from bigio import Parameters
from bigio.core import OperatingSystem
from random import shuffle

#
# A utility class providing network tools.
# 
# @author Andy Trimble
#

logger = logging.getLogger('NetworkUtil')

NETWORK_INTERFACE_PROPETY = "io.bigio.network"

nic = None

ip = 'localhost'

START_PORT = 32768;
END_PORT = 65536;
NUM_CANDIDATES = END_PORT - START_PORT;
PORTS = []

def init():
    for i in range(START_PORT, END_PORT):
        PORTS.append(i)
    
    shuffle(PORTS)
    return

def findNIC():
    
    networkInterfaceName = Parameters.getProperty(NETWORK_INTERFACE_PROPETY);

    if networkInterfaceName is None:
        if Parameters.currentOS() == OperatingSystem.WIN_64 or Parameters.currentOS() == OperatingSystem.WIN_32:
            networkInterfaceName = "lo"
        elif Parameters.currentOS() == OperatingSystem.LINUX_64 or Parameters.currentOS() == OperatingSystem.LINUX_32:
            networkInterfaceName = "eth0"
        elif Parameters.currentOS() == OperatingSystem.MAC_64 or Parameters.currentOS() == OperatingSystem.MAC_32:
            networkInterfaceName = "en0"
        else:
            logger.error("Cannot determine operating system. Cluster cannot form.")
    else:
        networkInterfaceName = networkInterfaceName.trim();
    
    nic = NetworkInterface.getByName(networkInterfaceName);

    if(nic != null && !nic.isUp()) {
        LOG.error("Selected network interface '" + networkInterfaceName + 
                "' is down. Please select an alternate network " + 
                "interface using the property 'io.bigio.network' in your " +
                "configuration file (ex. io.bigio.network=eth0). For " +
                "a list of available interfaces, type 'net' into the shell.");
    }

    if(nic != null) {
        Enumeration e = nic.getInetAddresses();
        while(e.hasMoreElements()) {
            InetAddress i = (InetAddress) e.nextElement();
            String address = i.getHostAddress();

            if(!address.contains(":")) {
                inetAddress = i;
                ip = address;
                break;
            }
        }
    } else {
        LOG.error("Selected network interface '" + networkInterfaceName + 
                "' cannot be found. Please select an alternate network " + 
                "interface using the property 'io.bigio.network' in your " +
                "configuration file (ex. io.bigio.network=eth0). For " +
                "a list of available interfaces, type 'net' into the shell.");
    }
    
#
# Get the IP address of this instance.
# 
# @return the IP address associated with the selected network interface
#
@staticmethod
def getIp():
    if nic == None:
        findNIC()

    return ip;

#
# Get the configured network interface.
# 
# @return the network interface this member is using
#
def getNetworkInterface():
    if nic is None:
        findNIC();

    return nic;

#
# Get a random unused port between START_PORT and END_PORT.
# 
# @return a random unused port
#
def getFreePort():
    for port in PORTS:
        
        try:
            comSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            comSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        except socket.error, msg:
            continue
            
        try:
            comSocket.bind(('', port))
            comSocket.connect()
            
            comSocket.close()
            
            return port
        
        except socket.error, msg:
            continue
        

    return None;

init()
