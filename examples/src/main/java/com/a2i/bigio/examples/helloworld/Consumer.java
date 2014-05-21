/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.bigio.examples.helloworld;

import com.a2i.bigio.Component;
import com.a2i.bigio.Initialize;
import com.a2i.bigio.Inject;
import com.a2i.bigio.Speaker;
import com.a2i.bigio.core.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
@Component
public class Consumer {

    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);
    
    @Inject
    private Speaker speaker;

    @Initialize
    public void init() {
        speaker.addListener("HelloWorld", new MessageListener<HelloWorldMessage>() {
            @Override
            public void receive(HelloWorldMessage message) {
                LOG.info(message.getMessage());
            }
        });
    }
}
