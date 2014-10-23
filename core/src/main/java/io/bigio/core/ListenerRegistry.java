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

package io.bigio.core;

import io.bigio.MessageListener;
import io.bigio.Component;
import io.bigio.Interceptor;
import io.bigio.core.codec.GenericDecoder;
import io.bigio.core.member.Member;
import io.bigio.core.member.MemberKey;
import io.bigio.util.TopicUtils;
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
 * A class for managing listener registrations.
 * 
 * @author Andy Trimble
 */
@Component
public class ListenerRegistry {

    private static final int THREAD_POOL_SIZE = 8;

    private static final Logger LOG = LoggerFactory.getLogger(ListenerRegistry.class);
    
    private final Environment environment = new Environment();
    private final Reactor reactor;

    private final ScheduledExecutorService futureExecutor = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    private Member me;

    private final Map<Member, Map<String, List<Registration>>> map = new HashMap<>();

    private final Map<String, List<Interceptor>> interceptors = new HashMap<>();

    /**
     * Constructor.
     */
    public ListenerRegistry() {
        reactor = Reactors.reactor()
                .env(environment)
                .dispatcher(Environment.RING_BUFFER)
                .get();
    }

    /**
     * Add a topic interceptor.
     * 
     * @param topic a topic.
     * @param interceptor an interceptor.
     */
    public void addInterceptor(String topic, Interceptor interceptor) {
        if(interceptors.get(topic) == null) {
            interceptors.put(topic, new ArrayList<Interceptor>());
        }
        interceptors.get(topic).add(interceptor);
    }

    /**
     * Set the current member.
     * 
     * @param me the current member.
     */
    public void setMe(Member me) {
        this.me = me;
    }

    /**
     * Get the current member.
     * @return the current member.
     */
    public Member getMe() {
        return me;
    }

    /**
     * Add a listener that is located in the same VM as the current member.
     * 
     * @param <T> a message type.
     * @param topic a topic.
     * @param partition a partition.
     * @param listener a listener.
     */
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

    /**
     * Remove all local listeners on a given topic.
     * 
     * @param topic a topic.
     */
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

    /**
     * Remove topic/partition registrations. 
     * 
     * @param regs a set of registrations.
     */
    public void removeRegistrations(List<Registration> regs) {
        for(Map<String, List<Registration>> allRegs : map.values()) {
            if(allRegs != null) {
                for(String key : allRegs.keySet()) {
                    allRegs.get(key).removeAll(regs);
                }
            }
        }
    }

    /**
     * Get all topic/partition registrations. 
     * 
     * @return the list of all registrations.
     */
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

    /**
     * Get all members that have at least one listener registered for a given 
     * topic.
     * 
     * @param topic a topic.
     * @return all members that have at least one registered listener.
     */
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

    /**
     * Register a member for a topic-partition.
     * 
     * @param topic a topic.
     * @param partition a partition.
     * @param member a member.
     */
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

    /**
     * Send a message.
     * 
     * @param envelope a message envelope.
     * @throws IOException in case of a sending error.
     */
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
        } else if(envelope.getExecuteTime() >= 0) {
            reactor.notify(TopicUtils.getNotifyTopicString(envelope.getTopic(), envelope.getPartition()), Event.wrap(envelope));
        }
    }
}
