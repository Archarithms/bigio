/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.core;

import com.a2i.bigio.Component;
import com.a2i.bigio.Interceptor;
import com.a2i.bigio.core.codec.GenericDecoder;
import com.a2i.bigio.core.member.Member;
import com.a2i.bigio.core.member.MemberKey;
import com.a2i.bigio.util.TopicUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
@Component
public class ListenerRegistry {

    private static final int THREAD_POOL_SIZE = 8;

    private static final Logger LOG = LoggerFactory.getLogger(ListenerRegistry.class);
    
    private final Environment environment = new Environment();
    private final Reactor reactor;

    private final ScheduledExecutorService futureExecutor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    private Member me;

//    private final RelationalMap<Registration> map = new RelationalMap<>();
    private final Map<Member, Map<String, List<Registration>>> map = new HashMap<>();

    private final Map<String, List<Interceptor>> interceptors = new HashMap<>();

    public ListenerRegistry() {
        reactor = Reactors.reactor()
                .env(environment)
                .dispatcher(Environment.RING_BUFFER)
                .get();
    }

    public void addInterceptor(String topic, Interceptor interceptor) {
        if(interceptors.get(topic) == null) {
            interceptors.put(topic, new ArrayList<Interceptor>());
        }
        interceptors.get(topic).add(interceptor);
    }

    public void setMe(Member me) {
        this.me = me;
    }

    public Member getMe() {
        return me;
    }

    public <T> void addLocalListener(final String topic, final String partition, final MessageListener<T> listener) {
        Consumer<Event<Envelope>> consumer = new Consumer<Event<Envelope>>() {
            @Override
            public void accept(Event<Envelope> m) {
                try {
                    listener.receive((T)m.getData().getMessage());
                } catch(ClassCastException ex) {
                    LOG.error("Topic '" + topic + "' received incorrect message type : " + m.getData().getMessage().getClass().getName(), ex);
                } catch(Throwable ex) {
                    LOG.error("Exception in Reactor.", ex);
                }
            }
        };

        reactor.on(Selectors.regex(TopicUtils.getTopicString(topic, partition)), consumer);
    }

    public void removeAllLocalListeners(String topic) {
        Map<String, List<Registration>> allRegs = map.get(me);
        
        if(allRegs != null) {
            List<Registration> regs = allRegs.get(topic);

            if(regs != null) {
                LOG.trace("Removing " + regs.size() + " registration");
                reactor.getConsumerRegistry().unregister(topic);
                regs.clear();
            } else {
                LOG.trace("No listeners registered for topic " + topic);
            }
        }
    }

    public void removeRegistrations(List<Registration> regs) {
        for(Map<String, List<Registration>> allRegs : map.values()) {
            if(allRegs != null) {
                for(String key : allRegs.keySet()) {
                    allRegs.get(key).removeAll(regs);
                }
            }
        }
    }

    public List<Registration> getAllRegistrations() {
        List<Registration> ret = new ArrayList<>();
        
        for(Map<String, List<Registration>> allRegs : map.values()) {
            if(allRegs != null) {
                for(String key : allRegs.keySet()) {
                    ret.addAll(allRegs.get(key));
                }
            }
        }

        return ret;
    }

    public List<Member> getRegisteredMembers(String topic) {
        List<Member> ret = new ArrayList<>();

        for(Member member : map.keySet()) {
            Map<String, List<Registration>> allRegs = map.get(member);
            if(allRegs != null) {
                for(String key : allRegs.keySet()) {
                    if(key.equals(topic)) {
                        ret.add(member);
                    }
                }
            }
        }

        return ret;
    }

    protected synchronized void registerMemberForTopic(String topic, String partition, Member member) {

        if(map.get(member) == null) {
            map.put(member, new HashMap<String, List<Registration>>());
        }

        if(map.get(member).get(topic) == null) {
            map.get(member).put(topic, new ArrayList<Registration>());
        }

        boolean found = false;
        for(Registration reg : map.get(member).get(topic)) {
            if(topic.equals(reg.getTopic()) && partition.equals(reg.getPartition()) && member.equals(reg.getMember())) {
                found = true;
                break;
            }
        }

        if(!found) {
            Registration reg = new Registration(member, topic, partition);
            map.get(member).get(topic).add(reg);

            if(LOG.isTraceEnabled()) {
                LOG.trace(new StringBuilder()
                        .append(MemberKey.getKey(me))
                        .append(": Registering member '")
                        .append(MemberKey.getKey(member))
                        .append("' for topic '")
                        .append(topic)
                        .append("' on partition '")
                        .append(partition)
                        .append("'").toString());
            }
        }
    }

    public void send(Envelope envelope) throws IOException {
        if(!envelope.isDecoded()) {
            // decode actual message
            envelope.setMessage(GenericDecoder.decode(envelope.getClassName(), envelope.getPayload()));
            envelope.setDecoded(true);
        }

        if(interceptors.containsKey(envelope.getTopic())) {
            for(Interceptor interceptor : interceptors.get(envelope.getTopic())) {
                envelope = interceptor.intercept(envelope);
            }
        }

        if(envelope.getExecuteTime() > 0) {
            final Envelope env = envelope;
            futureExecutor.schedule(new Runnable() {
               @Override
               public void run() {
                   reactor.notify(TopicUtils.getNotifyTopicString(env.getTopic(), env.getPartition()), Event.wrap(env));
               }
            }, envelope.getExecuteTime(), TimeUnit.MILLISECONDS);
        } else {
            reactor.notify(TopicUtils.getNotifyTopicString(envelope.getTopic(), envelope.getPartition()), Event.wrap(envelope));
        }
    }
}
