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
