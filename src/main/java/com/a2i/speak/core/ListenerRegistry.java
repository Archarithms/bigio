/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public enum ListenerRegistry {
    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(ListenerRegistry.class);
    
    private final Environment env = new Environment();
    private final Reactor reactor;

    private final Map<String, List<String>> registeredListeners = new HashMap<>();
    private final List<String> localTopics = new ArrayList<>();

    ListenerRegistry() {
        reactor = Reactors.reactor()
                .env(env)
                .dispatcher(Environment.RING_BUFFER)
                .get();
    }

    public <T> void addListener(String topic, final MessageListener<T> consumer) {
        reactor.on(Selectors.regex(topic), new Consumer<Event<Envelope>>() {
            @Override
            public void accept(Event<Envelope> m) {
                consumer.receive((T)m.getData().getMessage());
            }
        });
        localTopics.add(topic);
    }

    protected List<String> getAllLocalTopics() {
        return localTopics;
    }

    protected List<Member> getRegisteredMembers(String topic) {
        List<String> memberKeys = registeredListeners.get(topic);
        if(memberKeys == null) {
            return Collections.emptyList();
        } else {
            List<Member> members = new ArrayList<>();
            for(String key : memberKeys) {
                members.add(MemberHolder.INSTANCE.getMember(key));
            }
            return members;
        }
    }

    protected void registerMemberForTopic(String topic, Member member) {
        if(registeredListeners.get(topic) == null) {
            registeredListeners.put(topic, new ArrayList<String>());
        }

        String key = MemberKey.getKey(member);
        if(!registeredListeners.get(topic).contains(key)) {
            registeredListeners.get(topic).add(key);
        }
    }

    protected void send(Envelope envelope) throws IOException {
        if(!envelope.isDecoded()) {
            // decode actual message
            envelope.setMessage(GenericDecoder.decode(envelope.getPayload()));
            envelope.setDecoded(true);
        }

        reactor.notify(envelope.getTopic(), Event.wrap(envelope));
    }
}
