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
from bigio.core import ClusterService
from bigio.util import TopicUtils

# This is the main entry point into BigIO.
# 
# @author Andy Trimble
#
class Speaker():

    logger = logging.getLogger('Speaker')
    
    cluster = ClusterService

    # Construct a speaker.
    #
    def __init__(self):
        return
    
    #
    # Initialize the framework.
    #
    def init(self):
        self.cluster.initialize();
        self.logger.info('The speaker has arrived')
        return

    

    #
    # Shut down the system.
    #
    def shutdown(self):
        self.cluster.shutdown();
        return

    #
    # Set the cluster service. This method exists to support bootstrapping.
    # 
    # @param cluster the cluster service.
    #
    def setCluster(self, cl):
        self.cluster = cl;
        return

    #
    # Send a message across a topic with the supplied execution offset.
    # 
    # @param <T> the type of message
    # @param topic the name of the topic
    # @param message the message to send
    # @param offsetMilliseconds the offset in milliseconds of this message
    # @throws Exception in case of an error in the message pipeline
    #
    def send(self, topic, message, offsetMilliseconds=0):
        self.send(topic, TopicUtils.ALL_PARTITIONS, message, offsetMilliseconds);
        return

    #
    # Send a message across a topic and partition. The partition matches on
    # a regular expression.
    # 
    # @param <T> the type of message
    # @param topic the name of the topic
    # @param partition a partition
    # @param message the message to send
    # @throws Exception in case of an error in the message pipeline
    #
    def send(self, topic, partition, message):
        self.cluster.sendMessage(topic, partition, message);
        return

    #
    # Send a message across a topic and partition with the supplied execution 
    # offset. The partition matches on a regular expression.
    # 
    # @param <T> the type of message
    # @param topic the name of the topic
    # @param partition a partition
    # @param message the message to send
    # @param offsetMilliseconds the offset in milliseconds of this message
    # @throws Exception in case of an error in the message pipeline
    #
    def send(self, topic, partition, message, offsetMilliseconds):
        self.cluster.sendMessage(topic, partition, message, offsetMilliseconds)
        return

    #
    # Add a listener on a topic and partition.
    # 
    # @param <T> the type of message expected
    # @param topic the name of the topic
    # @param partition a partition
    # @param listener the listener to add
    #
    def addListener(self, topic, partition=TopicUtils.ALL_PARTITIONS, listener):
        self.cluster.addListener(topic, partition, listener);
        return

    #
    # Remove all registered listeners from the topic.
    # 
    # @param topic the topic
    #
    def removeAllListeners(self, topic):
        self.cluster.removeAllListeners(topic);
        return

    #
    # List all of the members in the current simulation cluster.
    # 
    # @return the list of known members
    #
    def listMembers(self):
        return self.cluster.getActiveMembers()

    #
    # Add a message interceptor on a topic.
    # 
    # @param topic a topic
    # @param interceptor an interceptor
    #
    def addInterceptor(self, topic, interceptor):
        self.cluster.addInterceptor(topic, interceptor)
        return

    #
    # Get the cluster member that represents this instance.
    # 
    # @return me
    #
    def getMe(self):
        return self.cluster.getMe()

    #
    # Get the set of tags associated with this member. If the set of tags
    # returned is modified, the changes will be broadcast to the rest of
    # the simulation cluster.
    # 
    # @return the set of tags associated with this cluster member
    #
    def getTags(self):
        return self.cluster.getMe().getTags()

    #
    # Set the delivery type for a topic.
    # 
    # @param topic a topic.
    # @param type the type of delivery to perform.
    #
    def setDeliveryType(self, topic, deliveryType):
        self.cluster.setDeliveryType(topic, deliveryType)
        return

    #
    # Get the cluster service for unit testing purposes.
    # 
    # @return the cluster service
    #
    def getClusterService(self):
        return self.cluster;
