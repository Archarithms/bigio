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

package com.a2i.bigio.core.member;

import com.a2i.bigio.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for containing all known BigIO cluster members.
 * 
 * @author Andy Trimble
 */
@Component
public class MemberHolder {

    private static final Logger LOG = LoggerFactory.getLogger(MemberHolder.class);
    
    private final Map<String, Member> members = new TreeMap<>();
    private final Map<String, Member> activeMembers = new TreeMap<>();
    private final Map<String, Member> deadMembers = new TreeMap<>();

    public void clear() {
        members.clear();
        activeMembers.clear();
        deadMembers.clear();
    }

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
