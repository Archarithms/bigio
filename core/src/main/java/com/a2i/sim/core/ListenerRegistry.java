/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.core;

import com.a2i.sim.core.codec.GenericDecoder;
import com.a2i.sim.util.Relation;
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

    // Indexed by topic, values are member keys
    private final Map<String, List<String>> registeredListeners = new HashMap<>();
    // Keep track of the Reactor consumer objects
    private final Map<MessageListener, Consumer> consumers = new HashMap<>();
    // Indexed by topic
    private final Map<String, List<MessageListener>> topicListeners = new HashMap<>();
    // List of topics someone locally has subscribed to
    private final List<String> topicsOfInterest = new ArrayList<>();

    ListenerRegistry() {
        reactor = Reactors.reactor()
                .env(env)
                .dispatcher(Environment.RING_BUFFER)
                .get();
    }

    public <T> void addLocalListener(String topic, final MessageListener<T> consumer) {
        Consumer<Event<Envelope>> con = new Consumer<Event<Envelope>>() {
            @Override
            public void accept(Event<Envelope> m) {
                consumer.receive((T)m.getData().getMessage());
            }
        };

        if(topicListeners.get(topic) == null) {
            topicListeners.put(topic, new ArrayList<MessageListener>());
        }
        topicListeners.get(topic).add(consumer);
        consumers.put(consumer, con);
        reactor.on(Selectors.regex(topic), con);

        topicsOfInterest.add(topic);
    }

    public <T> void removeLocalListener(MessageListener<T> consumer) {
        reactor.getConsumerRegistry().unregister(consumers.get(consumer));
        consumers.remove(consumer);
    }
    
    public void removeAllLocalListeners(String topic) {
        LOG.debug("Removing all listeners from topic '" + topic + "'");

        if(topicListeners.get(topic) == null) {
            return;
        }
        
        for(MessageListener l : topicListeners.get(topic)) {
            reactor.getConsumerRegistry().unregister(consumers.get(l));
            consumers.remove(l);
        }

        topicListeners.remove(topic);

        registeredListeners.remove(topic);
    }

    public List<String> getLocalTopicsOfInterest() {
        return topicsOfInterest;
    }

    public List<Member> getRegisteredMembers(String topic) {
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
            envelope.setMessage(GenericDecoder.decode(envelope.getClassName(), envelope.getPayload()));
            envelope.setDecoded(true);
        }

        reactor.notify(envelope.getTopic(), Event.wrap(envelope));
    }

    private class Registration implements Relation {
        private final Member member;
        private final MessageListener listener;
        private final Consumer<Event> consumer;
        private final String topic;

        public Registration(Member member, MessageListener listener, Consumer<Event> consumer, String topic) {
            this.member = member;
            this.listener = listener;
            this.consumer = consumer;
            this.topic = topic;
        }

        @Override
        public int getLength() {
            return 4;
        }

        @Override
        public Class<?> getClass(int itemNum) {
            switch(itemNum) {
                case 1:
                    return Member.class;
                case 2:
                    return MessageListener.class;
                case 3:
                    return Consumer.class;
                case 4:
                    return String.class;
                default:
                    return null;
            }
        }

        @Override
        public Object getItem(int itemNum) {
            switch(itemNum) {
                case 1:
                    return getMember();
                case 2:
                    return getListener();
                case 3:
                    return getConsumer();
                case 4:
                    return getTopic();
                default:
                    return null;
            }
        }

        /**
         * @return the member
         */
        public Member getMember() {
            return member;
        }

        /**
         * @return the listener
         */
        public MessageListener getListener() {
            return listener;
        }

        /**
         * @return the consumer
         */
        public Consumer<Event> getConsumer() {
            return consumer;
        }

        /**
         * @return the topic
         */
        public String getTopic() {
            return topic;
        }
    }
}
