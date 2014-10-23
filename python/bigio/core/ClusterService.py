#
# Copyright (c) 2014, Archarithms Inc.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this
# list of conditions and the following disclaimer. 
# 2. Redistributions in binary form must reproduce the above copyright notice,
# this list of conditions and the following disclaimer in the documentation
# and/or other materials provided with the distribution.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# The views and conclusions contained in the software and documentation are those
# of the authors and should not be interpreted as representing official policies, 
# either expressed or implied, of the FreeBSD Project.
#

import logging
import random
from bigio import DeliveryType
from bigio import Parameters
from bigio.core import Envelope
from bigio.core import Gossiper
from bigio.core import ListenerRegistry
from bigio.core import MCDiscovery
from bigio.core.codec import GenericEncoder
from bigio.core.member import MemberHolder
from bigio.core.member import MemberKey
from bigio.core.member import MeMember
from bigio.util import NetworkUtil
from bigio.util import TimeUtil

#
# This class handles communication between nodes in a cluster.
# 
# @author Andy Trimble
#
class ClusterService():

    PROTOCOL_PROPERTY = 'io.bigio.protocol'
    DEFAULT_PROTOCOL = 'tcp'
    GOSSIP_PORT_PROPERTY = 'io.bigio.port.gossip'
    DATA_PORT_PROPERTY = 'io.bigio.port.data'

    multicast = MCDiscovery

    memberHolder = MemberHolder

    registry = ListenerRegistry

    me = MeMember

    gossiper = Gossiper

    logger = logging.getLogger('ClusterService')

    deliveries = dict()
    roundRobinIndex = dict()

    shuttingDown = False

    #
    # Default constructor.
    #
    def __init__(self):
        return

    #
    # Set the multicast discovery object. Only used in bootstrapping.
    # 
    # @param multicast the multicast discovery object.
    #
    def setMulticastDiscovery(self, multicast):
        self.multicast = multicast
        return

    #
    # Set the member container. Only used in bootstrapping.
    # 
    # @param memberHolder the member container.
    #
    def setMemberHolder(self, memberHolder):
        self.memberHolder = memberHolder
        return

    #
    # Set the listener registry. Only used in bootstrapping.
    # 
    # @param registry the listener registry.
    #
    def setRegistry(self, registry):
        self.registry = registry
        return

    #
    # Get the listener registry.
    # 
    # @return the listener registry.
    #
    def getRegistry(self):
        return self.registry

    #
    # Set the delivery method.
    # 
    # @param topic a topic.
    # @param type the type of method delivery.
    #
    def setDeliveryType(self, topic, deliveryType):
        self.deliveries[topic] = deliveryType
        
        if deliveryType == DeliveryType.ROUND_ROBIN:
            self.roundRobinIndex[topic] = 0
            
        return

    #
    # Add an interceptor to a topic.
    # 
    # @param topic a topic.
    # @param interceptor an interceptor.
    #
    def addInterceptor(self, topic, interceptor):
        self.registry.addInterceptor(topic, interceptor)
        return

    #
    # Add a topic/partition listener.
    # 
    # @param <T> the message type.
    # @param topic a topic.
    # @param partition a partition.
    # @param consumer a listener.
    #
    def addListener(self, topic, partition, consumer):
        self.registry.registerMemberForTopic(topic, partition, self.me)
        self.registry.addLocalListener(topic, partition, consumer)
        return

    #
    # Remove all listeners on a topic.
    # 
    # @param topic a topic.
    #
    def removeAllListeners(self, topic):
        self.registry.removeAllLocalListeners(topic);
        return

    #
    # Send a message.
    # 
    # @param <T> a message type.
    # @param topic a topic.
    # @param partition a partition.
    # @param message a message.
    # @param offsetMilliseconds time offset of the message.
    # @throws IOException in case of error in delivery.
    #
    def sendMessage(self, topic, partition, message, offsetMilliseconds=0):
        envelope = Envelope()
        envelope.setDecoded(False)
        envelope.setExecuteTime(offsetMilliseconds)
        envelope.setMillisecondsSinceMidnight(TimeUtil.getMillisecondsSinceMidnight())
        envelope.setSenderKey(MemberKey.getMemberKey(self.me))
        envelope.setTopic(topic)
        envelope.setPartition(partition)
        envelope.setClassName(message.getClass().getName())

        if topic in self.deliveries:
            delivery = self.deliveries[topic]
        else:
            delivery = DeliveryType.BROADCAST
            self.deliveries[topic] = delivery

        if delivery == DeliveryType.ROUND_ROBIN:
            if len(self.registry.getRegisteredMembers(topic)) > 0:

                index = (self.roundRobinIndex[topic] + 1) % len(self.registry.getRegisteredMembers(topic))
                self.roundRobinIndex[topic] = index
                
                member = self.registry.getRegisteredMembers(topic)[index]

                if(self.me.equals(member)):
                    envelope.setMessage(message)
                    envelope.setDecoded(True)
                else:
                    envelope.setPayload(GenericEncoder.encode(message))
                    envelope.setDecoded(False)

                member.send(envelope)
                
        elif delivery == DeliveryType.RANDOM:
            if len(self.registry.getRegisteredMembers(topic)) > 0:
                index = (int)(random.random() * self.registry.getRegisteredMembers(topic).size())

                member = self.registry.getRegisteredMembers(topic)[index]

                if self.me.equals(member):
                    envelope.setMessage(message)
                    envelope.setDecoded(True)
                else:
                    envelope.setPayload(GenericEncoder.encode(message))
                    envelope.setDecoded(False)

                member.send(envelope);
                    
        else:
            for member in self.registry.getRegisteredMembers(topic):
                if self.me.equals(member):
                    envelope.setMessage(message);
                    envelope.setDecoded(True);
                else:
                    envelope.setPayload(GenericEncoder.encode(message));
                    envelope.setDecoded(False);

                member.send(envelope);
        return

    #
    # Get the list of known members. Members returned by this method may be
    # either active or dead.
    # 
    # @return the list of known members.
    #
    def getAllMembers(self):
        return self.memberHolder.getAllMembers()
    
    #
    # Get the list of active members.
    # 
    # @return the list of active members.
    #
    def getActiveMembers(self):
        return self.memberHolder.getActiveMembers()
    
    #
    # Get the list of dead members.
    # 
    # @return the list of dead members.
    #
    def getDeadMembers(self):
        return self.memberHolder.getDeadMembers()

    #
    # Get the member representing this BigIO instance.
    # 
    # @return the current member.
    #
    def getMe(self):
        return self.me

    #
    # Initialize communication.
    #
    def initialize(self):

        protocol = Parameters.getProperty(self.PROTOCOL_PROPERTY, self.DEFAULT_PROTOCOL);
        gossipPort = Parameters.getProperty(self.GOSSIP_PORT_PROPERTY);
        dataPort = Parameters.getProperty(self.DATA_PORT_PROPERTY);


        if gossipPort is None:
            self.logger.trace("Finding a random port for gossiping.")
            gossipPortInt = NetworkUtil.getFreePort()
        else:
            gossipPortInt = int(gossipPort)

        if dataPort is None:
            self.logger.trace("Finding a random port for data.");
            dataPortInt = NetworkUtil.getFreePort();
        else:
            dataPortInt = int(dataPort);

        myAddress = NetworkUtil.getIp();

        if(LOG.isDebugEnabled()) {
            StringBuilder greeting = new StringBuilder();
            LOG.trace(greeting
                    .append("Greetings. I am ")
                    .append(myAddress)
                    .append(":")
                    .append(gossipPortInt)
                    .append(":")
                    .append(dataPortInt)
                    .toString());
        }

        if(protocol.equalsIgnoreCase("udp")) {
            LOG.info("Running over UDP");
            me = new MeMemberUDP(myAddress, gossipPortInt, dataPortInt, memberHolder, registry);
        } else {
            LOG.info("Running over TCP");
            me = new MeMemberTCP(myAddress, gossipPortInt, dataPortInt, memberHolder, registry);
        }
        me.setStatus(MemberStatus.Alive);
        me.initialize();
        memberHolder.updateMemberStatus(me);

        me.addGossipConsumer(new GossipListener() {
            @Override
            public void accept(GossipMessage message) {
                handleGossipMessage(message);
            }
        });

        multicast.initialize(me);

        gossiper = new Gossiper(me, memberHolder, registry);

        registry.setMe(me);
    }

    /#*
     # TODO: Implement manually joining a cluster.
     # 
     # @param ip an initial peer to which to connect.
     #/
    public void join(String ip) {
        
    }

    /#*
     # TODO: Implement manually leaving a cluster.
     #/
    public void leave() {
        
    }

    /#*
     # Shutdown communication objects.
     #/
    public void shutdown() {
        shuttingDown = true;
        
        gossiper.shutdown();

        multicast.shutdown();

        for(Member member : memberHolder.getAllMembers()) {
            ((AbstractMember)member).shutdown();
        }

        memberHolder.clear();
    }

    private void handleGossipMessage(GossipMessage message) {
        if(shuttingDown) {
            return;
        }

        String senderKey = MemberKey.getKey(message);
        boolean updateTags = false;

        for(int i = 0; i < message.getMembers().size(); ++i) {

            String key = message.getMembers().get(i);

            Member m = memberHolder.getMember(key);
            if(m == null) {
                String protocol = Parameters.INSTANCE.getProperty(PROTOCOL_PROPERTY, DEFAULT_PROTOCOL);
                if(protocol.equalsIgnoreCase("udp")) {
                    if(LOG.isTraceEnabled()) {
                        LOG.trace(new StringBuilder()
                                .append(MemberKey.getKey(me))
                                .append(" Discovered new UDP member through gossip: ")
                                .append(message.getIp())
                                .append(":")
                                .append(message.getGossipPort())
                                .append(":").append(message.getDataPort()).toString());
                    }
                    m = new RemoteMemberUDP(memberHolder);
                } else {
                    if(LOG.isTraceEnabled()) {
                        LOG.trace(new StringBuilder()
                                .append(MemberKey.getKey(me))
                                .append(" Discovered new TCP member through gossip: ")
                                .append(message.getIp())
                                .append(":")
                                .append(message.getGossipPort())
                                .append(":").append(message.getDataPort()).toString());
                    }
                    m = new RemoteMemberTCP(memberHolder);
                }
                String[] values = key.split(":");
                m.setIp(values[0]);
                m.setGossipPort(Integer.parseInt(values[1]));
                m.setDataPort(Integer.parseInt(values[2]));
                ((AbstractMember)m).initialize();
            }

            memberHolder.updateMemberStatus(m);

            int memberClock = message.getClock().get(i);
            int knownMemberClock = m.getSequence().get();

            if(memberClock > knownMemberClock) {
                if(key.equals(senderKey)) {
                    updateTags = true;
                }

                m.getSequence().set(memberClock);
                List<String> topics = message.getListeners().get(key);
                if(topics == null) {
                    topics = Collections.emptyList();
                }

                List<Registration> toRemove = new ArrayList<>();
                for(Registration reg : registry.getAllRegistrations()) {
                    if(reg.getMember().equals(m)) {
                        if(!topics.contains(TopicUtils.getTopicString(reg.getTopic(), reg.getPartition()))) {
                            toRemove.add(reg);
                        }
                    }
                }
                registry.removeRegistrations(toRemove);
                for(String topicString : topics) {
                    String topic = TopicUtils.getTopic(topicString);
                    String partition = TopicUtils.getPartition(topicString);
                    if(!registry.getRegisteredMembers(topic).contains(m)) {
                        registry.registerMemberForTopic(topic, partition, m);
                    }
                }
            }
        }

        if(updateTags) {
            Member m = memberHolder.getMember(senderKey);
            m.getTags().clear();
            m.getTags().putAll(message.getTags());
        }
    }
}
