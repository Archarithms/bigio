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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
@Component
public class Producer {

    private static final Logger LOG = LoggerFactory.getLogger(Producer.class);
    
    @Inject
    private Speaker speaker;

    @Initialize
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
