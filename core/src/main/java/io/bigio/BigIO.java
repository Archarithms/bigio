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

package io.bigio;

import io.bigio.cli.CommandLineInterface;
import io.bigio.core.ClusterService;
import io.bigio.core.Container;
import io.bigio.core.ListenerRegistry;
import io.bigio.core.MCDiscovery;
import io.bigio.core.member.Member;
import io.bigio.core.member.MemberHolder;
import io.bigio.util.TopicUtils;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.SocketException;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main entry point into BigIO.
 * 
 * @author Andy Trimble
 */
@Component
public class BigIO {

    private static final Logger LOG = LoggerFactory.getLogger(BigIO.class);

    private static final boolean MONITOR_THREAD_CONTENTION = true;

    private static BigIO bigio;

    @Inject
    private ClusterService cluster;

    @Inject
    private CommandLineInterface cli;

    /**
     * Do not use this constructor to create a BigIO instance. Rather, use the
     * {@link #bootstrap() bootstrap} method.
     */
    public BigIO() {
        if(MONITOR_THREAD_CONTENTION) {
            ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(MONITOR_THREAD_CONTENTION);
        }
    }

    /**
     * Initialize the framework.
     */
    @Initialize
    public void init() {
        try {
            cluster.initialize();
        } catch(SocketException ex) {
            LOG.error("Cannot find a free port.", ex);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    /**
     * Bootstrap the system and return a BigIO object.
     * 
     * @return an initialized speaker object.
     */
    public static BigIO bootstrap() {
        BigIO speaker = new BigIO();
        ClusterService cluster = new ClusterService();
        MemberHolder memberHolder = new MemberHolder();
        ListenerRegistry registry = new ListenerRegistry();
        MCDiscovery mc = new MCDiscovery();
        mc.setMemberHolder(memberHolder);
        cluster.setMulticastDiscovery(mc);
        cluster.setMemberHolder(memberHolder);
        cluster.setRegistry(registry);
        speaker.setCluster(cluster);
        speaker.init();
        return speaker;
    }

    /**
     * Shut down the system.
     */
    public void shutdown() {
        cluster.shutdown();
    }

    /**
     * Set the cluster service. This method exists to support bootstrapping.
     * 
     * @param cluster the cluster service.
     */
    public void setCluster(ClusterService cluster) {
        this.cluster = cluster;
    }

    /**
     * Send a message across a topic.
     * 
     * @param <T> the type of message
     * @param topic the name of the topic
     * @param message the message to send
     * @throws IOException in case of an error in the message pipeline
     */
    public <T> void send(String topic, T message) throws IOException {
        send(topic, TopicUtils.ALL_PARTITIONS, message);
    }

    /**
     * Send a message across a topic with the supplied execution offset.
     * 
     * @param <T> the type of message
     * @param topic the name of the topic
     * @param message the message to send
     * @param offsetMilliseconds the offset in milliseconds of this message
     * @throws IOException in case of an error in the message pipeline
     */
    public <T> void send(String topic, T message, int offsetMilliseconds) throws IOException {
        send(topic, TopicUtils.ALL_PARTITIONS, message, offsetMilliseconds);
    }

    /**
     * Send a message across a topic and partition. The partition matches on
     * a regular expression.
     * 
     * @param <T> the type of message
     * @param topic the name of the topic
     * @param partition a partition
     * @param message the message to send
     * @throws IOException in case of an error in the message pipeline
     */
    public <T> void send(String topic, String partition, T message) throws IOException {
        cluster.sendMessage(topic, partition, message);
    }

    /**
     * Send a message across a topic and partition with the supplied execution 
     * offset. The partition matches on a regular expression.
     * 
     * @param <T> the type of message
     * @param topic the name of the topic
     * @param partition a partition
     * @param message the message to send
     * @param offsetMilliseconds the offset in milliseconds of this message
     * @throws IOException in case of an error in the message pipeline
     */
    public <T> void send(String topic, String partition, T message, int offsetMilliseconds) throws IOException {
        cluster.sendMessage(topic, partition, message, offsetMilliseconds);
    }

    /**
     * Add a listener on a topic across all partitions.
     * 
     * @param <T> the type of message expected
     * @param topic the name of the topic
     * @param listener the listener to add
     */
    public <T> void addListener(String topic, MessageListener<T> listener) {
        addListener(topic, TopicUtils.ALL_PARTITIONS, listener);
    }

    /**
     * Add a listener on a topic and partition.
     * 
     * @param <T> the type of message expected
     * @param topic the name of the topic
     * @param partition a partition
     * @param listener the listener to add
     */
    public <T> void addListener(String topic, String partition, MessageListener<T> listener) {
        cluster.addListener(topic, partition, listener);
    }

    /**
     * Remove all registered listeners from the topic.
     * 
     * @param topic the topic
     */
    public void removeAllListeners(String topic) {
        cluster.removeAllListeners(topic, TopicUtils.ALL_PARTITIONS);
    }

    /**
     * Remove all registered listeners from the topic and partition.
     * 
     * @param topic the topic
     * @param partition
     */
    public void removeAllListeners(String topic, String partition) {
        cluster.removeAllListeners(topic, partition);
    }

    /**
     * List all of the members in the current simulation cluster.
     * 
     * @return the list of known members
     */
    public Collection<Member> listMembers() {
        return cluster.getActiveMembers();
    }

    /**
     * Add a message interceptor on a topic.
     * 
     * @param topic a topic
     * @param interceptor an interceptor
     */
    public void addInterceptor(String topic, Interceptor interceptor) {
        cluster.addInterceptor(topic, interceptor);
    }

    /**
     * Get the cluster member that represents this instance.
     * 
     * @return me
     */
    public Member getMe() {
        return cluster.getMe();
    }

    /**
     * Get the set of tags associated with this member. If the set of tags
     * returned is modified, the changes will be broadcast to the rest of
     * the simulation cluster.
     * 
     * @return the set of tags associated with this cluster member
     */
    public Map<String, String> getTags() {
        return cluster.getMe().getTags();
    }

    /**
     * Set the delivery type for a topic.
     * 
     * @param topic a topic.
     * @param type the type of delivery to perform.
     */
    public void setDeliveryType(String topic, DeliveryType type) {
        cluster.setDeliveryType(topic, type);
    }

    /**
     * Get the cluster service for unit testing purposes.
     * 
     * @return the cluster service
     */
    protected ClusterService getClusterService() {
        return cluster;
    }

    /**
     * Exit from the CLI.
     */
    public static void exit() {
        System.exit(0);
    }

    /**
     * The main method for BigIO.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Parameters.INSTANCE.currentOS(); // Just to load the properties
        Container.INSTANCE.scan();
        bigio = new BigIO();
    }
}
