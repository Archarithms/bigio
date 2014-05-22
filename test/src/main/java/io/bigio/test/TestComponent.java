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

package io.bigio.test;

import io.bigio.Component;
import io.bigio.Inject;
import io.bigio.Speaker;
import io.bigio.core.Envelope;
import io.bigio.core.MessageListener;
import io.bigio.core.codec.EnvelopeEncoder;
import io.bigio.core.codec.GenericEncoder;
import io.bigio.util.TimeUtil;
import java.io.IOException;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
@Component
public class TestComponent {

    private static final Logger LOG = LoggerFactory.getLogger(TestComponent.class);
    
    @Inject
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
                    Thread.sleep(1000l);
                    speaker.send("HelloWorld", new SimpleMessage("This message should be en/decoded"));
                } catch(Exception ex) {
                    LOG.debug("Error", ex);
                }
            }
        }
    };

    public TestComponent() {
        SimpleMessage m = new SimpleMessage("This message should be en/decoded");
        try {
            byte[] payload = GenericEncoder.encode(m);
            Envelope envelope = new Envelope();
            envelope.setDecoded(false);
            envelope.setExecuteTime(0);
            envelope.setMillisecondsSinceMidnight(TimeUtil.getMillisecondsSinceMidnight());
            envelope.setSenderKey("192.168.1.1:55200:55200");
            envelope.setTopic("HelloWorld");
            envelope.setClassName(SimpleMessage.class.getName());
            envelope.setPayload(payload);
            envelope.setDecoded(false);

            byte[] bytes = EnvelopeEncoder.encode(envelope);
            LOG.info("Typical message size: " + bytes.length);
            LOG.info("Typical payload size: " + payload.length);
            LOG.info("Typical header size: " + (bytes.length - payload.length));
        } catch (IOException ex) {
            LOG.error("IOException", ex);
        }

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
        LOG.info("Adding listener");

        speaker.addListener("HelloWorld", new MessageListener<SimpleMessage>() {
            @Override
            public void receive(SimpleMessage message) {
                ++messageCount;
                LOG.debug("Woo Hoo!!! Got a message. '" + message.getString() + "'");
            }
        });

        senderThread.start();
    }
}
