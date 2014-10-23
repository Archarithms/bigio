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
import sys
from bigio.core import OperatingSystem

#
# This singleton class manages all of the configurable parameters. Configuration
# files are loaded from the 'config' directory. Any property in the directory
# structure that ends with '.properties' will be loaded by this class and 
# their contents will be available through this API.
# 
# @author Andy Trimble
#
    
logger = logging.getLogger('Parameters')
MAX_DEPTH = 10
configDir = 'config'
os = OperatingSystem
properties = dict()


#
# Get a property.
# 
# @param name the name of the property.
# @return the property value, or null if the property does not exist.
#/
def getProperty(name, defaultValue=None):
    if name in properties:
        return properties[name]
    else:
        return defaultValue
    

#
# Get the operating system.
# 
# @return the operating system.
#
def currentOS():
    return os;

def loadProperties(file):
    f = open(file, 'r')
    for line in f:
        if '=' in line:
            param = line.split('=')
            properties[param[0]] = param[1]
    f.close()
    return

#
# Load the configuration.
#
def init():
    osName = sys.platform
    osArch = 'amd64'

    if 'win32' in osName:
        if 'amd64' in osArch:
            os = OperatingSystem.WIN_64;
        else:
            os = OperatingSystem.WIN_32;
    elif 'linux' in osName or 'cygwin' in osName:
        if 'amd64' in osArch:
            os = OperatingSystem.LINUX_64;
        else:
            os = OperatingSystem.LINUX_32;
    elif 'darwin' in osName:
        if 'amd64' in osArch:
            os = OperatingSystem.MAC_64;
        else:
            os = OperatingSystem.MAC_32;
            
    for dirpath, dnames, fnames in os.walk(configDir):
        for f in fnames:
            if f.endswith('.properties'):
                loadProperties(os.path.join(dirpath, f))
        
init()
