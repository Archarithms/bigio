/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim;

import com.a2i.sim.core.ClusterService;
import com.a2i.sim.core.member.Member;
import com.a2i.sim.core.MessageListener;
import java.util.Collection;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This is the main entry point into A2I Sim.
 * 
 * @author Andy Trimble
 */
@Component
public class Speaker {

    private static final Logger LOG = LoggerFactory.getLogger(Speaker.class);

    @Autowired
    private ClusterService cluster;

    /**
     * Initialize the framework.
     */
    @PostConstruct
    public void init() {
        cluster.initialize();
    }

    /**
     * Construct a speaker.
     */
    public Speaker() {
        LOG.info("The speaker has arrived");
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
        cluster.sendMessage(topic, message);
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
        cluster.sendMessage(topic, message, offsetMilliseconds);
    }

    /**
     * Add a listener on a topic.
     * 
     * @param <T> the type of message expected
     * @param topic the name of the topic
     * @param listener the listener to add
     */
    public <T> void addListener(String topic, MessageListener<T> listener) {
        cluster.addListener(topic, listener);
    }

    /**
     * Remove a listener from all registered topics.
     * 
     * @param <T> the type of message the listener expects
     * @param listener a listener to remove
     */
    public <T> void removeListener(MessageListener<T> listener) {
        cluster.removeListener(listener);
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
}
