/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.sim.core.member;

/**
 *
 * @author atrimble
 */
public enum MemberStatus {
    Alive, Left, Failed, Unknown;

    public static MemberStatus fromString(String in) {
        switch (in) {
            case "alive":
                return Alive;
            case "left":
                return Left;
            case "failed":
                return Failed;
            default:
                return Unknown;
        }
    }
}
