/*
 * Copyright (c) 2015, Archarithms Inc.
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

package io.bigio.util;

import io.bigio.Parameters;
import io.netty.util.NetUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class providing network tools.
 * 
 * @author Andy Trimble
 */
public class NetworkUtil {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkUtil.class);

    private static final String NETWORK_INTERFACE_PROPETY = "io.bigio.network";
    
    private static NetworkInterface nic = null;
    private static InetAddress inetAddress = null;

    private static String ip;

    private static final int START_PORT = 32768;
    private static final int END_PORT = 65536;
    private static final int NUM_CANDIDATES = END_PORT - START_PORT;

    private static final List<Integer> PORTS = new ArrayList<>();
    private static Iterator<Integer> portIterator;

    static {
        for (int i = START_PORT; i < END_PORT; i ++) {
            PORTS.add(i);
        }
        Collections.shuffle(PORTS);
    }

    private NetworkUtil() {

    }

    /**
     * Get the IP address of this instance.
     * 
     * @return the IP address associated with the selected network interface
     */
    public static String getIp() {
        if(nic == null) {
            findNIC();
        }

        return ip;
    }

    /**
     * Get the configured network interface.
     * 
     * @return the network interface this member is using
     */
    public static NetworkInterface getNetworkInterface() {
        if(nic == null) {
            findNIC();
        }

        return nic;
    }

    /**
     * Get the InetAddress object.
     * 
     * @return the InetAddress object
     */
    public static InetAddress getInetAddress() {
        if(nic == null) {
            findNIC();
        }

        return inetAddress;
    }

    /**
     * Get a random unused port between START_PORT and END_PORT.
     * 
     * @return a random unused port
     * @throws java.net.SocketException in the case where a free port cannot be located.
     */
    public static int getFreePort() throws SocketException {
        for (int i = 0; i < NUM_CANDIDATES; i ++) {
            int port = nextCandidatePort();
            try {
                // Ensure it is possible to bind on both wildcard and loopback.
                ServerSocket ss;
                ss = new ServerSocket();
                ss.setReuseAddress(false);
                ss.bind(new InetSocketAddress(port));
                ss.close();

                ss = new ServerSocket();
                ss.setReuseAddress(false);
                ss.bind(new InetSocketAddress(NetUtil.LOCALHOST, port));
                ss.close();

                return port;
            } catch (IOException e) {
                // ignore
            }
        }

        throw new SocketException("unable to find a free port");
    }

    private static int nextCandidatePort() {
        if (portIterator == null || !portIterator.hasNext()) {
            portIterator = PORTS.iterator();
        }
        return portIterator.next();
    }

    private static void findNIC() {
        try {
            String networkInterfaceName = Parameters.INSTANCE.getProperty(NETWORK_INTERFACE_PROPETY);

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
        } catch(SocketException ex) {
            LOG.error("Unable to determine IP address", ex);
            nic = null;
        }
    }
}
