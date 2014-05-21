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

package com.a2i.bigio;

import com.a2i.bigio.cli.CommandLineInterface;
import com.a2i.bigio.core.ClusterService;
import com.a2i.bigio.core.MessageListener;
import com.a2i.bigio.core.member.Member;
import com.a2i.bigio.util.TopicUtils;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main entry point into A2I Sim.
 * 
 * @author Andy Trimble
 */
@Component
public class Speaker {

    private static final Logger LOG = LoggerFactory.getLogger(Speaker.class);

    @Inject
    private ClusterService cluster;

    @Inject
    private CommandLineInterface cli = null;

    /**
     * Initialize the framework.
     */
    @Initialize
    public void init() {
        cluster.initialize();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });

        LOG.info("The speaker has arrived");
    }

    /**
     * Construct a speaker.
     */
    public Speaker() {
        
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
     * @throws Exception in case of an error in the message pipeline
     */
    public <T> void send(String topic, T message) throws Exception {
        send(topic, TopicUtils.ALL_PARTITIONS, message);
    }

    /**
     * Send a message across a topic with the supplied execution offset.
     * 
     * @param <T> the type of message
     * @param topic the name of the topic
     * @param message the message to send
     * @param offsetMilliseconds the offset in milliseconds of this message
     * @throws Exception in case of an error in the message pipeline
     */
    public <T> void send(String topic, T message, int offsetMilliseconds) throws Exception {
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
     * @throws Exception in case of an error in the message pipeline
     */
    public <T> void send(String topic, String partition, T message) throws Exception {
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
     * @throws Exception in case of an error in the message pipeline
     */
    public <T> void send(String topic, String partition, T message, int offsetMilliseconds) throws Exception {
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
        cluster.removeAllListeners(topic);
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
}
