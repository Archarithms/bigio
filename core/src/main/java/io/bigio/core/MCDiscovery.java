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

package io.bigio.core;

import io.bigio.Component;
import io.bigio.Inject;
import io.bigio.Parameters;
import io.bigio.core.codec.GossipCodec;
import io.bigio.core.member.AbstractMember;
import io.bigio.core.member.Member;
import io.bigio.core.member.MemberHolder;
import io.bigio.core.member.MemberKey;
import io.bigio.core.member.RemoteMemberTCP;
import io.bigio.core.member.RemoteMemberUDP;
import io.bigio.util.NetworkUtil;
import io.bigio.util.TimeUtil;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A discovery protocol implemented using multicast.
 * 
 * @author Andy Trimble
 */
@Component
public class MCDiscovery extends Thread {
    
    private static final Logger LOG = LoggerFactory.getLogger(MCDiscovery.class);

    @Inject
    private MemberHolder memberHolder;

    private static final String MULTICAST_ENABLED_PROPERTY = "io.bigio.multicast.enabled";
    private static final String MULTICAST_GROUP_PROPERTY = "io.bigio.multicast.group";
    private static final String MULTICAST_PORT_PROPERTY = "io.bigio.multicast.port";
    private static final String MULTICAST_TTL_PROPERTY = "io.bigio.multicast.ttl";
    private static final String DEFAULT_MULTICAST_GROUP = "239.0.0.1";
    private static final String DEFAULT_MULTICAST_PORT = "8989";
    private static final String DEFAULT_MULTICAST_TTL = "1";

    private final boolean enabled;
    private final String multicastGroup;
    private final int multicastPort;
    private final int multicastTtl;

    private Member me;
    private String protocol;
    private DatagramChannel listener;
    private DatagramChannel sender;
    private InetAddress group;

    private final ExecutorService threadPool = Executors.newSingleThreadExecutor();

    private boolean running = true;

    /**
     * Constructor.
     */
    public MCDiscovery() {
        enabled = Boolean.parseBoolean(Parameters.INSTANCE.getProperty(MULTICAST_ENABLED_PROPERTY, "true"));
        multicastGroup = Parameters.INSTANCE.getProperty(MULTICAST_GROUP_PROPERTY, DEFAULT_MULTICAST_GROUP);
        multicastPort = Integer.parseInt(Parameters.INSTANCE.getProperty(MULTICAST_PORT_PROPERTY, DEFAULT_MULTICAST_PORT));
        multicastTtl = Integer.parseInt(Parameters.INSTANCE.getProperty(MULTICAST_TTL_PROPERTY, DEFAULT_MULTICAST_TTL));
    }

    /**
     * Set the member container.
     * 
     * @param memberHolder the member container.
     */
    public void setMemberHolder(MemberHolder memberHolder) {
        this.memberHolder = memberHolder;
    }

    /**
     * Initialize the discovery service.
     * 
     * @param me the current member.
     */
    public void initialize(Member me) {
        this.me = me;

        protocol = Parameters.INSTANCE.getProperty(ClusterService.PROTOCOL_PROPERTY, ClusterService.DEFAULT_PROTOCOL);

        try {
            setupNetworking();
        } catch(IOException ex) {
            LOG.error("IOException.", ex);
        }

        if(isEnabled()) {
            start();
        }
    }

    /**
     * Shutdown the discovery service.
     */
    public void shutdown() {
        this.running = false;
        try {
            listener.close();
            sender.close();
        } catch(IOException ex) {
            LOG.error("IOException.", ex);
        }
    }

    /**
     * Setup the multicast facilities.
     * @throws java.io.IOException
     */
    public void setupNetworking() throws IOException {
        if(NetworkUtil.getNetworkInterface() == null) {
            LOG.error("Cannot form cluster. No Network interface can be found.");
            return;
        }
        
        try {
            if(!NetworkUtil.getNetworkInterface().supportsMulticast()) {
                LOG.error("Network Interface doesn't support multicast.");
                return;
            }
        } catch (SocketException ex) {
            LOG.error("Error determining multicast support.", ex);
            return;
        }

        try {
            if(NetworkUtil.getNetworkInterface() == null || !NetworkUtil.getNetworkInterface().isUp()) {
                LOG.error("Cannot form cluster. Network interface is down.");
                return;
            }
        } catch(SocketException ex) {
            LOG.error("Cannot form cluster.", ex);
            return;
        }
        
        listener = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .setOption(StandardSocketOptions.IP_MULTICAST_TTL, multicastTtl)
                .bind(new InetSocketAddress(multicastPort))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, NetworkUtil.getNetworkInterface());
        sender = DatagramChannel.open(StandardProtocolFamily.INET)
                .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                .setOption(StandardSocketOptions.IP_MULTICAST_TTL, multicastTtl)
                .bind(new InetSocketAddress(multicastPort))
                .setOption(StandardSocketOptions.IP_MULTICAST_IF, NetworkUtil.getNetworkInterface());
        group = InetAddress.getByName(multicastGroup);
        switch(Parameters.INSTANCE.currentOS()) {
            case MAC_64:
            case MAC_32:
            case WIN_64:
            case WIN_32:
            case LINUX_64:
            case LINUX_32:
            default:
                listener.join(group, NetworkUtil.getNetworkInterface());
                break;
        }

        try {
            GossipMessage message = new GossipMessage(
                    me.getIp(),
                    me.getGossipPort(),
                    me.getDataPort());
            message.setMillisecondsSinceMidnight(TimeUtil.getMillisecondsSinceMidnight());
            message.getTags().putAll(me.getTags());
            message.getMembers().add(MemberKey.getKey(me));
            message.getClock().add(me.getSequence().incrementAndGet());
            message.setPublicKey(me.getPublicKey());

            sendMessage(message);
        } catch (IOException ex) {
            LOG.error("Cannot serialize message.", ex);
        }
    }

    /**
     * Run the multicast listener.
     */
    @Override
    public void run() {
        ByteBuffer packet = ByteBuffer.allocate(512);

        while(running) {
            try {
                listener.receive(packet);
                packet.rewind();
                processMessage(packet);
                packet.clear();
            } catch(AsynchronousCloseException ex) {
                running = false;
                return;
            } catch(IOException ex) {
                LOG.error("IOException.", ex);
            }
        }
    }

    private void processMessage(ByteBuffer buff) throws IOException {
        int length = (buff.get() & 0xFF) << 8 | (buff.get() & 0xFF);
        byte[] arr = new byte[length];
        buff.get(arr, 0, length);

        GossipMessage message = GossipCodec.decode(arr);

        String key = MemberKey.getKey(message);

        Member member = memberHolder.getMember(key);
        
        if(member == null) {
            if("udp".equalsIgnoreCase(protocol)) {
                if(LOG.isTraceEnabled()) {
                    LOG.trace(new StringBuilder()
                            .append("Discovered new UDP member: ")
                            .append(message.getIp())
                            .append(":")
                            .append(message.getGossipPort())
                            .append(":").append(message.getDataPort()).toString());
                }
                member = new RemoteMemberUDP(message.getIp(), message.getGossipPort(), message.getDataPort(), memberHolder);
            } else {
                if(LOG.isTraceEnabled()) {
                    LOG.trace(new StringBuilder()
                    .append("Discovered new TCP member: ")
                    .append(message.getIp())
                    .append(":")
                    .append(message.getGossipPort())
                    .append(":").append(message.getDataPort()).toString());
                }
                member = new RemoteMemberTCP(message.getIp(), message.getGossipPort(), message.getDataPort(), memberHolder);
            }

            if(message.getPublicKey() != null) {
                member.setPublicKey(message.getPublicKey());
            }

            ((AbstractMember)member).initialize();
        } else {
            if(LOG.isTraceEnabled()) {
                            LOG.trace(new StringBuilder()
                            .append("Received known member: ")
                            .append(message.getIp())
                            .append(":")
                            .append(message.getGossipPort())
                            .append(":").append(message.getDataPort()).toString());
                }
        }

        for(String k : message.getTags().keySet()) {
            member.getTags().put(k, message.getTags().get(k));
        }

        memberHolder.updateMemberStatus(member);
    }

    /**
     * Send a gossip message over multicast.
     * 
     * @param message a message.
     * @throws IOException in case of a sending error.
     */
    public void sendMessage(GossipMessage message) throws IOException {

        byte[] bytes = GossipCodec.encode(message);

        ByteBuffer buff = ByteBuffer.allocate(bytes.length);
        buff.clear();
        buff.put(bytes);
        buff.rewind();

        StringBuilder builder = new StringBuilder();
        while(buff.hasRemaining()) {
            builder.append(String.format("%02X ", buff.get()));
        }
        buff.rewind();

        sender.send(buff, new InetSocketAddress(group, multicastPort));
    }
        
    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
}
