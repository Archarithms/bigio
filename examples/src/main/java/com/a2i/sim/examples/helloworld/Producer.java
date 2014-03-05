/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.examples.helloworld;

import com.a2i.sim.Speaker;
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
public class Producer {

    private static final Logger LOG = LoggerFactory.getLogger(Producer.class);
    
    @Autowired
    private Speaker speaker;

    @PostConstruct
    public void init() {
        new Thread() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(1000l);
                        
                        HelloWorldMessage message = new HelloWorldMessage();
                        message.setMessage("Hello World!");
                        speaker.send("HelloWorld", message);
                    } catch (Exception ex) {
                        LOG.error("Caught exception", ex);
                    }
                }
            }
        }.start();
    }
}
