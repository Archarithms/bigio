/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.bigio;

import io.bigio.agent.MessageTransformer;
import org.junit.Test;

/**
 *
 * @author atrimble
 */
public class EncodeTest {
    
    @Test
    public void testEncodeDecode() throws Exception {

        RepMessage message = MessageUtils.createMessage();
        RepMessage decodedMessage = new RepMessage();

        byte[] bytes = (byte[])message.getClass().getMethod("bigioencode").invoke(message);
        decodedMessage.getClass().getMethod("bigiodecode", byte[].class).invoke(decodedMessage, bytes);

        MessageUtils.testMessageEquality(message, decodedMessage);
    }
}
