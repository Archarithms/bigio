/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core;

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
