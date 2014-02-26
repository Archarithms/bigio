/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.core;

import com.a2i.sim.core.member.Member;
import com.a2i.sim.core.member.MemberKey;
import com.a2i.sim.core.codec.GenericDecoder;
import com.a2i.sim.util.RelationalMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    private Member me;

    private final RelationalMap<Registration> map = new RelationalMap<>();

    ListenerRegistry() {
        reactor = Reactors.reactor()
                .env(env)
                .dispatcher(Environment.RING_BUFFER)
                .get();
    }

    public void setMe(Member me) {
        this.me = me;
    }

    public <T> void addLocalListener(String topic, final MessageListener<T> listener) {
        Consumer<Event<Envelope>> consumer = new Consumer<Event<Envelope>>() {
            @Override
            public void accept(Event<Envelope> m) {
                listener.receive((T)m.getData().getMessage());
            }
        };
        reactor.on(Selectors.regex(topic), consumer);

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
            }
        }
    }
    
    public void removeAllLocalListeners(String topic) {
        List<Registration> regs = map.query(me, topic);
        
        if(!regs.isEmpty()) {
            LOG.info("Removing " + regs.size() + " registration");
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

    protected void registerMemberForTopic(String topic, Member member) {
        List<Registration> regs = map.query(member, topic);

        if(regs.isEmpty()) {
            Registration reg = new Registration(member, topic);
            map.add(reg);

            LOG.info("Registering member '" + MemberKey.getKey(member) + "' for topic '" + topic + "'");
        }
    }

    public void send(Envelope envelope) throws IOException {
        if(!envelope.isDecoded()) {
            // decode actual message
            envelope.setMessage(GenericDecoder.decode(envelope.getClassName(), envelope.getPayload()));
            envelope.setDecoded(true);
        }

        reactor.notify(envelope.getTopic(), Event.wrap(envelope));
    }
}
