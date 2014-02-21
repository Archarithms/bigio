/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.test;

import com.a2i.sim.core.Message;

/**
 *
 * @author atrimble
 */
@Message
public class SimpleMessage {
    private String string;

    private String hugeString = 
            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsj" +
            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsj" +
            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsj" +
            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsl";

    public SimpleMessage() {

    }

    public SimpleMessage(String string) {
        this.string = string;
    }

    /**
     * @return the string
     */
    public String getString() {
        return string;
    }

    /**
     * @param string the string to set
     */
    public void setString(String string) {
        this.string = string;
    }
}
