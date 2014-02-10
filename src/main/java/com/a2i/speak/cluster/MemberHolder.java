/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author atrimble
 */
enum MemberHolder {
    INSTANCE;
    
    private final Map<String, Member> members = new ConcurrentHashMap<>();
    private final Map<String, Member> activeMembers = new ConcurrentHashMap<>();
    private final Map<String, Member> deadMembers = new ConcurrentHashMap<>();

    public Map<String, Member> getAllMembers() {
        return members;
    }

    public Map<String, Member> getActiveMembers() {
        return activeMembers;
    }

    public Map<String, Member> getDeadMembers() {
        return deadMembers;
    }
}
