/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms.core;

import com.a2i.dms.Component;
import com.a2i.dms.Interceptor;
import com.a2i.dms.core.codec.GenericDecoder;
import com.a2i.dms.core.member.Member;
import com.a2i.dms.core.member.MemberKey;
import com.a2i.dms.util.RelationalMap;
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

    private final RelationalMap<Registration> map = new RelationalMap<>();

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

    public <T> void addLocalListener(String topic, final MessageListener<T> listener) {
        Consumer<Event<Envelope>> consumer = new Consumer<Event<Envelope>>() {
            @Override
            public void accept(Event<Envelope> m) {
                try {
                    listener.receive((T)m.getData().getMessage());
                } catch(Throwable ex) {
                    LOG.error("Exception in Reactor.", ex);
                }
            }
        };
        reactor.on(Selectors.object(topic), consumer);

        List<Registration> regs = map.query(me, topic);
        if(!regs.isEmpty()) {
            regs.get(0).setConsumer(consumer);
            regs.get(0).setListener(listener);
        } else {
            LOG.warn("Could not find myself in the registrations");
        }
    }

    public <T> void removeLocalListener(MessageListener<T> listener) {
        List<Registration> regs = map.query(me);

        for(Registration reg : regs) {
            if(reg.getListener().equals(listener)) {
                reactor.getConsumerRegistry().unregister(reg.getConsumer());
                map.remove(reg);
                LOG.info("Removing listener from topic " + reg.getTopic());
            }
        }
    }
    
    public void removeAllLocalListeners(String topic) {
        List<Registration> regs = map.query(me, topic);
        
        if(!regs.isEmpty()) {
            LOG.info("Removing " + regs.size() + " registration");
            for(Registration reg : regs) {
                reactor.getConsumerRegistry().unregister(reg.getConsumer());
            }
            map.remove(regs);
        } else {
            LOG.info("No listeners registered for topic " + topic);
        }
    }

    public void removeRegistrations(List<Registration> regs) {
        map.remove(regs);
    }

    public List<Registration> getAllRegistrations() {
        return map.query();
    }

    public List<Member> getRegisteredMembers(String topic) {
        List<Member> ret = new ArrayList<>();

        List<Registration> members = map.query(topic);
        for(Registration reg : members) {
            ret.add(reg.getMember());
        }

        return ret;
    }

    protected synchronized void registerMemberForTopic(String topic, Member member) {
        List<Registration> regs = map.query(member, topic);

        if(regs.isEmpty()) {
            Registration reg = new Registration(member, topic);
            map.add(reg);

            if(LOG.isTraceEnabled()) {
                LOG.trace(new StringBuilder()
                        .append("Registering member '")
                        .append(MemberKey.getKey(member))
                        .append("' for topic '")
                        .append(topic)
//                        .append("' on partition '")
//                        .append(partition)
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
                   reactor.notify(env.getTopic(), Event.wrap(env));
               }
            }, envelope.getExecuteTime(), TimeUnit.MILLISECONDS);
        } else {
            reactor.notify(envelope.getTopic(), Event.wrap(envelope));
        }
    }
}
