/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.bigio.test;

import com.a2i.bigio.Message;

/**
 *
 * @author atrimble
 */
@Message
public class SimpleMessage {
    private String string = "";

    private long sequence = 0;

    private long sendTime = 0;

//    private String hugeString = 
//            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsj" +
//            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsj" +
//            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsj" +
//            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsj" +
//            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsj" +
//            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsj" +
//            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsj" +
//            "lkdsjfljsdflkdsjflksdjflskajflfjsdlkfjsldfjksldfkjsadlfkjlsadjfsldfjslafjsdlfkjslfjsdfljsdflskdjfslkjfslakdjfsdlfkjsldfjksdlfjsl";

    // 100 Bytes
    private String hugeString = "abcabcabcabcabcabcabca";

    // 512 Bytes
//    private String hugeString = "abcabcabcabcabcabcabca" +
//            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
//            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
//            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + 
//            "aaaaaaaaaaaaaaaaaaaa";

    // 1024 Bytes
//    private String hugeString = "abcabcabcabcabcabcabca" +
//            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
//            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
//            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + 
//            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
//            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + 
//            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + 
//            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + 
//            "aaaaaaaaaaaa";
            

    public SimpleMessage() {

    }

    public SimpleMessage(String string, long sequence, long sendTime) {
        this.string = string;
        this.sequence = sequence;
        this.sendTime = sendTime;
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

    /**
     * @return the sequence
     */
    public long getSequence() {
        return sequence;
    }

    /**
     * @param sequence the sequence to set
     */
    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    /**
     * @return the sendTime
     */
    public long getSendTime() {
        return sendTime;
    }

    /**
     * @param sendTime the sendTime to set
     */
    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }
}
