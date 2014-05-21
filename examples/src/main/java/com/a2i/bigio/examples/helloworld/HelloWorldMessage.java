/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.bigio.examples.helloworld;

import com.a2i.bigio.Message;

/**
 *
 * @author atrimble
 */
@Message
public class HelloWorldMessage {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
