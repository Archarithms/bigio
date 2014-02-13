/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak;

import com.a2i.speak.cluster.ClusterService;
import com.a2i.speak.cluster.MessageListener;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class Speaker {

    private static final Logger LOG = LoggerFactory.getLogger(Speaker.class);

    @Autowired
    private ClusterService cluster;

    @PostConstruct
    public void init() {
        cluster.initialize();
    }

    public Speaker() {
        LOG.info("The speaker has arrived");
    }

    public <T> void send(String topic, T message) throws Exception {
        cluster.sendMessage(topic, message);
    }

    public <T> void addListener(String topic, MessageListener<T> listener) {
        cluster.addListener(topic, listener);
    }
}
