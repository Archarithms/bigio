/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster;

import com.a2i.speak.Parameters;
import com.a2i.speak.cluster.gossip.Gossiper;
import com.a2i.speak.cluster.gossip.MCDiscovery;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author atrimble
 */
@Component
public class ClusterService {

    private static final String COMMAND_PORT_PROPERTY = "com.a2i.port.command";
    private static final String DATA_PORT_PROPERTY = "com.a2i.port.data";

    @Autowired
    private MCDiscovery multicast;

    private Member me;

    private Gossiper gossiper;

    private static final Logger LOG = LoggerFactory.getLogger(ClusterService.class);
    
    public ClusterService() {
        
    }

    public Collection<Member> getAllMembers() {
        return MemberHolder.INSTANCE.getAllMembers();
    }
    
    public Collection<Member> getActiveMembers() {
        return MemberHolder.INSTANCE.getActiveMembers();
    }
    
    public Collection<Member> getDeadMembers() {
        return MemberHolder.INSTANCE.getDeadMembers();
    }

    public void initialize() {

        String commandPort = Parameters.INSTANCE.getProperty(COMMAND_PORT_PROPERTY);
        String dataPort = Parameters.INSTANCE.getProperty(DATA_PORT_PROPERTY);

        int commandPortInt;
        int dataPortInt;

        if(commandPort == null) {
            LOG.debug("Finding a random port for commands.");
            commandPortInt = NetworkUtil.getFreePort();
        } else {
            commandPortInt = Integer.parseInt(commandPort);
        }

        if(dataPort == null) {
            LOG.debug("Finding a random port for data.");
            dataPortInt = NetworkUtil.getFreePort();
        } else {
            dataPortInt = Integer.parseInt(dataPort);
        }

        String myAddress = NetworkUtil.getIp();

        if(LOG.isDebugEnabled()) {
            StringBuilder greeting = new StringBuilder();
            LOG.debug(greeting
                    .append("Greetings. I am ")
                    .append(myAddress)
                    .append(":")
                    .append(commandPortInt)
                    .append(":")
                    .append(dataPortInt)
                    .toString());
        }

        me = new MeMember(myAddress, commandPortInt, dataPortInt);
        me.setStatus(Member.Status.Alive);
        MemberHolder.INSTANCE.updateMember(me);

        ((MeMember)me).addCommandConsumer(CommandMessageType.MemberList, new CommandListener() {
            @Override
            public void accept(CommandMessage message) {
                LOG.info("Got a member list");
                for(String ip : message.getTags().keySet()) {
                    Member m = new Member(
                            ip, 
                            Integer.parseInt(message.getTags().get(ip).split(":")[0]), 
                            Integer.parseInt(message.getTags().get(ip).split(":")[1]));

                    MemberHolder.INSTANCE.updateMember(m);
                }
            }
        });

        multicast.initialize(me);

        gossiper = new Gossiper(me);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    public void members() {
        for(Member member : getAllMembers()) {
            LOG.info(member.toString());
        }
    }

    public void join(String ip) {
        
    }

    public void leave() {
        
    }

    public void shutdown() {
        multicast.shutdown();
        for(Member member : MemberHolder.INSTANCE.getAllMembers()) {
            member.close();
        }
    }
}
