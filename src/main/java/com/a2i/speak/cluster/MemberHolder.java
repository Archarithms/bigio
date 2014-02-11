/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public enum MemberHolder {
    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(MemberHolder.class);
    
    private final Map<String, Member> members = new ConcurrentHashMap<>();
    private final Map<String, Member> activeMembers = new ConcurrentHashMap<>();
    private final Map<String, Member> deadMembers = new ConcurrentHashMap<>();

    public Collection<Member> getAllMembers() {
        return Collections.unmodifiableCollection(members.values());
    }

    public Collection<Member> getActiveMembers() {
        return Collections.unmodifiableCollection(activeMembers.values());
    }

    public Collection<Member> getDeadMembers() {
        return Collections.unmodifiableCollection(deadMembers.values());
    }

    public void updateMember(Member member) {
        String id =  getId(member);

        if(members.containsKey(id)) {
            LOG.debug("Updating member " + id);
            if(activeMembers.containsKey(id) 
                    && (member.getStatus() == Member.Status.Failed 
                    || member.getStatus() == Member.Status.Left 
                    || member.getStatus() == Member.Status.Unknown)) {
                activeMembers.remove(id);
                deadMembers.put(id, member);
            } else if(deadMembers.containsKey(id) && member.getStatus() == Member.Status.Alive) {
                deadMembers.remove(id);
                activeMembers.put(id, member);
            }
        } else {
            LOG.debug("Adding new member " + id);
            member.initializeClients();
            members.put(id, member);
            if(member.getStatus() == Member.Status.Alive) {
                activeMembers.put(id, member);
            } else {
                deadMembers.put(id, member);
            }
        }
    }

    private String getId(Member member) {
        return member.getIp() + ":" + member.getCommandPort();
    }
}
