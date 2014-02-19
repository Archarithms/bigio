/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core;

import com.a2i.speak.Parameters;
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
 *
 * @author atrimble
 */
public class NetworkUtil {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkUtil.class);

    private static final String NETWORK_INTERFACE_PROPETY = "com.a2i.network";
    
    private static NetworkInterface nic = null;

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

    public static String getIp() {
        if(nic == null) {
            findNIC();
        }

        return ip;
    }

    public static NetworkInterface getNetworkInterface() {
        if(nic == null) {
            findNIC();
        }

        return nic;
    }

    public static int getFreePort() {
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

        throw new RuntimeException("unable to find a free port");
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
                        networkInterfaceName = "net0";
                        break;
                    case LINUX_64:
                    case LINUX_32:
                        networkInterfaceName = "eth0";
                        break;
                    case MAC_64:
                    case MAC_32:
                        networkInterfaceName = "eth0";
                        break;
                    default:
                        LOG.error("Cannot determine operating system. Cluster cannot form.");
                }
            }
            
            nic = NetworkInterface.getByName(networkInterfaceName);
            Enumeration e = nic.getInetAddresses();
            while(e.hasMoreElements()) {
                InetAddress i = (InetAddress) e.nextElement();
                String address = i.getHostAddress();

                if(!address.startsWith("fe")) {
                    ip = address;
                }
            }
        } catch(SocketException ex) {
            LOG.error("Unable to determine IP address", ex);
            nic = null;
        }
    }
}
