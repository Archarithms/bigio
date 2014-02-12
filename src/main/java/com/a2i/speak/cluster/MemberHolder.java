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

    public String getKey(String ip, String commandPort, String dataPort) {
        return new StringBuilder().append(ip).append(":")
                .append(commandPort).append(":").append(dataPort).toString();
    }

    public Member getMember(String key) {
        Member m;
        synchronized(members) {
            m = members.get(key);
        }
        return m;
    }

    public Collection<Member> getAllMembers() {
        return Collections.unmodifiableCollection(members.values());
    }

    public Collection<Member> getActiveMembers() {
        return Collections.unmodifiableCollection(activeMembers.values());
    }

    public Collection<Member> getDeadMembers() {
        return Collections.unmodifiableCollection(deadMembers.values());
    }

    public void updateMemberStatus(Member member) {
        String id = getId(member);

        synchronized(members) {
            if(members.containsKey(id)) {
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
                members.put(id, member);
                if(member.getStatus() == Member.Status.Alive) {
                    activeMembers.put(id, member);
                } else {
                    deadMembers.put(id, member);
                }
            }
        }
    }

    private String getId(Member member) {
        return member.getIp() + ":" + member.getCommandPort() + ":" + member.getDataPort();
    }
}
