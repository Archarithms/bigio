/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.examples.helloworld;

import com.a2i.sim.Speaker;
import com.a2i.sim.core.MessageListener;
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
public class Consumer {

    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);
    
    @Autowired
    private Speaker speaker;

    @PostConstruct
    public void init() {
        speaker.addListener("HelloWorld", new MessageListener<HelloWorldMessage>() {
            @Override
            public void receive(HelloWorldMessage message) {
                LOG.info(message.getMessage());
            }
        });
    }
}
