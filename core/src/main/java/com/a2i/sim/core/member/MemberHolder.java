/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.core.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public enum MemberHolder {
    INSTANCE;

    private static final Logger LOG = LoggerFactory.getLogger(MemberHolder.class);
    
    private final Map<String, Member> members = new TreeMap<>();
    private final Map<String, Member> activeMembers = new TreeMap<>();
    private final Map<String, Member> deadMembers = new TreeMap<>();

    public Member getMember(String key) {
        Member m;
        synchronized(members) {
            m = members.get(key);
        }
        return m;
    }

    public List<Member> getAllMembers() {
        List<Member> ret = new ArrayList<>();
        ret.addAll(members.values());
        return ret;
    }

    public List<Member> getActiveMembers() {
        List<Member> ret = new ArrayList<>();
        ret.addAll(activeMembers.values());
        return ret;
    }

    public List<Member> getDeadMembers() {
        List<Member> ret = new ArrayList<>();
        ret.addAll(deadMembers.values());
        return ret;
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
