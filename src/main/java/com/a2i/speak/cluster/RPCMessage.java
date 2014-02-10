/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.cluster;

import java.io.IOException;

/**
 *
 * @author atrimble
 * @param <T>
 */
public interface RPCMessage<T extends RPCMessage> {

    byte[] encode(int seq) throws IOException;

    T decode(byte[] bytes) throws IOException;
}
