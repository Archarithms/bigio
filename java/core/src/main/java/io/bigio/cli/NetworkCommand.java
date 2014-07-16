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

package io.bigio.cli;

import io.bigio.CommandLine;
import io.bigio.Component;
import io.bigio.util.NetworkUtil;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the "net" CLI command. This command will print information about the
 * available network interfaces.
 * 
 * @author Andy Trimble
 */
@Component
public class NetworkCommand implements CommandLine {

    private static final Logger LOG = LoggerFactory.getLogger(NetworkCommand.class);

    /**
     * Get the command string.
     * 
     * @return the command.
     */
    @Override
    public String getCommand() {
        return "net";
    }

    /**
     * Execute the command.
     * 
     * @param args the arguments to the command (if any).
     */
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

    /**
     * Return the help/description string for display.
     * 
     * @return the help/description string
     */
    @Override
    public String help() {
        return "Prints the available network interfaces on this system (currently used interface marked with a *).";
    }
}
