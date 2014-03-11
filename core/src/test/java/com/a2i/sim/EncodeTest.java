/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author atrimble
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class EncodeTest {

    @Test
    public void testEncodeDecode() throws Exception {

        RepMessage message = MessageUtils.createMessage();
        RepMessage decodedMessage = new RepMessage();

        byte[] bytes = (byte[])message.getClass().getMethod("_encode_").invoke(message);
        decodedMessage.getClass().getMethod("_decode_", byte[].class).invoke(decodedMessage, bytes);

        MessageUtils.testMessageEquality(message, decodedMessage);
    }
}
