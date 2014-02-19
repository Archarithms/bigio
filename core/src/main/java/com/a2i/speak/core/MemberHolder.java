/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core;

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
        String key = MemberKey.getKey(member);

        synchronized(members) {
            if(members.containsKey(key)) {
                if(activeMembers.containsKey(key) 
                        && (member.getStatus() == MemberStatus.Failed 
                        || member.getStatus() == MemberStatus.Left 
                        || member.getStatus() == MemberStatus.Unknown)) {
                    activeMembers.remove(key);
                    deadMembers.put(key, member);
                } else if(deadMembers.containsKey(key) && member.getStatus() == MemberStatus.Alive) {
                    deadMembers.remove(key);
                    activeMembers.put(key, member);
                }
            } else {
                members.put(key, member);
                if(MemberStatus.Alive == member.getStatus()) {
                    activeMembers.put(key, member);
                } else {
                    deadMembers.put(key, member);
                }
            }
        }
    }
}
