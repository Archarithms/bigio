/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster.gossip;

/**
 *
 * @author atrimble
 */
public enum CommandMessageType {
    Announce("Announce"), Unknown("Unk");

    private final String value;

    CommandMessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CommandMessageType fromValue(String value) {
        if(value.equals(Announce.value)) {
            return Announce;
        }

        return Unknown;
    }
}
