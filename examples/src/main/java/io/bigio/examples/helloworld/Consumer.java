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

package io.bigio.examples.helloworld;

import io.bigio.BigIO;
import io.bigio.Component;
import io.bigio.Initialize;
import io.bigio.Inject;
import io.bigio.Interceptor;
import io.bigio.MessageListener;
import io.bigio.core.Envelope;
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
    private BigIO speaker;

    @Initialize
    public void init() {
        speaker.addListener("HelloWorld", (HelloWorldMessage message) -> {
            LOG.info("As a lambda expression: " + message.getMessage());
        });
        
        speaker.addListener("HelloWorld", new MessageListener<HelloWorldMessage>() {
            @Override
            public void receive(HelloWorldMessage message) {
                LOG.info("As an anonymous inner class: " + message.getMessage());
            }
        });

        speaker.addInterceptor("HelloWorld", (Envelope envelope) -> {
            LOG.info("Interceptor as a lambda expression: " + envelope.getSenderKey() + " : " + envelope.getTopic());
            return envelope;
        });

        speaker.addInterceptor("HelloWorld", new Interceptor() {
            @Override
            public Envelope intercept(Envelope envelope) {
                LOG.info("Interceptor as an anonymous inner class: " + envelope.getSenderKey() + " : " + envelope.getTopic());
                return envelope;
            }
        });
    }
}
