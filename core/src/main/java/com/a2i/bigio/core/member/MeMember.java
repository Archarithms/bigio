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
package com.a2i.bigio.core.member;

import com.a2i.bigio.core.Envelope;
import com.a2i.bigio.core.GossipListener;
import com.a2i.bigio.core.GossipMessage;
import com.a2i.bigio.core.ListenerRegistry;
import com.a2i.bigio.core.codec.EnvelopeDecoder;
import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;

/**
 *
 * @author atrimble
 */
public abstract class MeMember extends AbstractMember {

    private static final Logger LOG = LoggerFactory.getLogger(MeMember.class);

    protected static final String GOSSIP_TOPIC = "__gossiper";
    protected static final String DECODE_TOPIC = "__decoder";
                
    private final Environment env = new Environment();
    protected Reactor reactor;
    protected Reactor decoderReactor;

    protected ListenerRegistry registry;

    public MeMember(MemberHolder memberHolder, ListenerRegistry registry) {
        super(memberHolder);
        this.registry = registry;
    }

    public MeMember(String ip, int gossipPort, int dataPort, MemberHolder memberHolder, ListenerRegistry registry) {
        super(ip, gossipPort, dataPort, memberHolder);
        this.registry = registry;
    }

    protected abstract void initializeServers();

    public void addGossipConsumer(final GossipListener consumer) {
        reactor.on(Selectors.$(GOSSIP_TOPIC), new Consumer<Event<GossipMessage>>() {
            @Override
            public void accept(Event<GossipMessage> m) {
                consumer.accept(m.getData());
            }
        });
    }

    @Override
    public void initialize() {
        initializeReactor();
        initializeServers();
    }

    @Override
    public void send(Envelope message) throws IOException {
        registry.send(message);
    }

    private void initializeReactor() {
        reactor = Reactors.reactor()
                .env(env)
                .dispatcher(Environment.RING_BUFFER)
                .get();

        decoderReactor = Reactors.reactor()
                .env(env)
                .dispatcher(Environment.RING_BUFFER)
                .get();

        decoderReactor.on(Selectors.$(DECODE_TOPIC), new Consumer<Event<byte[]>>() {
            @Override
            public void accept(Event<byte[]> m) {
                try {
                    Envelope message = EnvelopeDecoder.decode(m.getData());
                    message.setDecoded(false);
                    send(message);
                } catch (IOException | MessageTypeException ex) {
                    LOG.error("Error decoding message.", ex);
                }
            }
        });
    }
}
