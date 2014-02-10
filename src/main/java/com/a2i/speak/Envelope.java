/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak;

import org.msgpack.annotation.Message;

/**
 *
 * @author atrimble
 */
@Message
public class Envelope {
    public String type;
    public byte[] payload;
}
