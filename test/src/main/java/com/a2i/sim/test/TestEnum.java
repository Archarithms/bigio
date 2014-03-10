/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;

/**
 *
 * @author atrimble
 */
public enum TestEnum {
    Value1, Value2;

    private static final transient MessagePack msgPack = new MessagePack();

    public byte[] encode() throws IOException {
        System.out.println("Encoding an enum " + ordinal());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Packer packer = msgPack.createPacker(out);
        packer.write(ordinal());
        return out.toByteArray();
    }
}
