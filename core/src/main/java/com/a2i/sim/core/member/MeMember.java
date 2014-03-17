/*
 * Copyright 2014 Archarithms Inc.
 */
package com.a2i.sim.core.member;

import com.a2i.sim.core.Envelope;
import com.a2i.sim.core.GossipListener;
import com.a2i.sim.core.GossipMessage;
import com.a2i.sim.core.ListenerRegistry;
import com.a2i.sim.core.codec.EnvelopeDecoder;
import com.a2i.sim.util.RunningStatistics;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.function.Consumer;

/**
 *
 * @author atrimble
 */
public abstract class MeMember extends AbstractMember {

    private static final Logger LOG = LoggerFactory.getLogger(MeMember.class);
                
    private final Environment env = new Environment();
    protected Reactor reactor;
    protected Reactor decoderReactor;

//    private final RunningStatistics gossipSizeStat = new RunningStatistics();
//    private final RunningStatistics dataSizeStat = new RunningStatistics();

    public MeMember() {
        super();
    }

    public MeMember(String ip, int gossipPort, int dataPort) {
        super(ip, gossipPort, dataPort);
    }

    protected abstract void initializeServers();

    public void addGossipConsumer(final GossipListener consumer) {
        reactor.on(new Consumer<Event<GossipMessage>>() {
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
        ListenerRegistry.INSTANCE.send(message);
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

        decoderReactor.on(new Consumer<Event<byte[]>>() {
            @Override
            public void accept(Event<byte[]> m) {

//                if(LOG.isDebugEnabled()) {
//                    dataSizeStat.push(m.getData().length);
//                }

                try {
                    Envelope message = EnvelopeDecoder.decode(m.getData());
                    message.setDecoded(false);
                    send(message);
                } catch (IOException ex) {
                    LOG.error("Error decoding message.", ex);
                }
            }
        });
    }
}
