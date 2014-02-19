/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak;

import com.a2i.speak.core.MessageListener;
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
public class TestComponent {

    private static final Logger LOG = LoggerFactory.getLogger(TestComponent.class);
    
    @Autowired
    private Speaker speaker;

    private boolean running = true;
    private long time;
    private long messageCount = 0;

    Thread senderThread = new Thread() {
        @Override
        public void run() {
            time = System.currentTimeMillis();
            while(running) {
                try {
                    Thread.sleep(100l);
                    speaker.send("HelloWorld", "Hello there World!");
                } catch(Exception ex) {
                    LOG.debug("Error", ex);
                }
            }
        }
    };

    public TestComponent() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                running = false;
                
                try {
                    senderThread.join();
                } catch(InterruptedException ex) {
                    ex.printStackTrace();
                }

                time = System.currentTimeMillis() - time;

                long count = messageCount;

                long seconds = time / 1000;
                long bandwidth = count / seconds;

                LOG.info("Received " + count + " messages in " + seconds + 
                        " seconds for a bandwidth of " + bandwidth + " m/s");
            }
        });
    }

    @PostConstruct
    public void go() {
        speaker.addListener("HelloWorld", new MessageListener<String>() {
            @Override
            public void receive(String message) {
                ++messageCount;
                LOG.debug("Woo Hoo!!! Got a message. '" + message + "'");
            }
        });

        senderThread.start();
    }
}
