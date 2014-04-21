/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.cli;

import com.a2i.dms.CommandLine;
import com.a2i.dms.Component;
import com.a2i.dms.util.NetworkUtil;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
@Component
public class NetworkCommand implements CommandLine {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkCommand.class);

    @Override
    public String getCommand() {
        return "net";
    }

    @Override
    public void execute(String... args) {
        try {
            Enumeration<NetworkInterface> enu = NetworkInterface.getNetworkInterfaces();
            while(enu.hasMoreElements()) {
                NetworkInterface ifc = enu.nextElement();
                System.out.println();
                if(NetworkUtil.getNetworkInterface() != null && 
                        NetworkUtil.getNetworkInterface().getName().equals(ifc.getName())) {
                    System.out.println(ifc.getName() + " *");
                } else {
                    System.out.println(ifc.getName());
                }
                System.out.println("    Display Name: " + ifc.getDisplayName());
                System.out.println("    Loopback: " + ifc.isLoopback());
                System.out.println("    Virtual: " + ifc.isVirtual());
                System.out.println("    Multicast: " + ifc.supportsMulticast());
                System.out.println("    Up: " + ifc.isUp());
                
                Enumeration<InetAddress> addresses = ifc.getInetAddresses();
                while(addresses.hasMoreElements()) {
                    InetAddress add = addresses.nextElement();
                    System.out.println("        " + add.getHostAddress());
                }
            }
        } catch (SocketException ex) {
            LOG.error("Socket error.", ex);
        }
    }

    @Override
    public String help() {
        return "Prints the available network interfaces on this system (currently used interface marked with a *).";
    }
}
