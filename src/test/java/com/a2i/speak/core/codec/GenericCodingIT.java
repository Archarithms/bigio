/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core.codec;

import java.util.List;
import java.util.Random;
import static org.junit.Assert.*;
import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.io.EndOfBufferException;
import org.msgpack.template.Templates;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class GenericCodingIT {
    private static final Logger LOG = LoggerFactory.getLogger(GenericCodingIT.class);

    private final Random rand = new Random();

    @Test
    public void testEncode() throws Exception {
        TestMessage message = createMessage();

        byte[] arr = (byte[]) message.getClass().getMethod("_encode_").invoke(message);

        StringBuilder buff = new StringBuilder();
        buff.append(arr.length).append(" bytes:").append("\n");
        for(int i = 0; i < arr.length; ++i) {
            buff.append(String.format("%02x ", arr[i]&0xff));
            if((i + 1) % 16 == 0) {
                buff.append("\n");
            }
        }
        LOG.info(buff.toString());

        MessagePack msgPack = new MessagePack();
        Unpacker unpacker = msgPack.createBufferUnpacker(arr);

        assertTrue(unpacker.readBoolean() == message.isBooleanValue());
        assertTrue(unpacker.readByte() == message.getByteValue());
        assertTrue(unpacker.readShort() == message.getShortValue());
        assertTrue(unpacker.readInt() == message.getIntValue());
        assertTrue(unpacker.readFloat() == message.getFloatValue());
        assertTrue(unpacker.readLong() == message.getLongValue());
        assertTrue(unpacker.readDouble() == message.getDoubleValue());
        assertTrue(unpacker.readString().equals(message.getStringValue()));
        
        byte[] ecefArr = unpacker.readByteArray();
        Unpacker ecefUnpacker = msgPack.createBufferUnpacker(ecefArr);
        assertTrue(ecefUnpacker.readDouble() == message.getEcefValue().getX());
        assertTrue(ecefUnpacker.readDouble() == message.getEcefValue().getY());
        assertTrue(ecefUnpacker.readDouble() == message.getEcefValue().getZ());

        boolean throwed = false;
        try {
            assertTrue(ecefUnpacker.getNextType() == ValueType.NIL);
        } catch(EndOfBufferException ex) {
            throwed = true;
        }
        assertTrue(throwed);

        boolean[] booleanArr = unpacker.read(boolean[].class);
        assertTrue(booleanArr.length == message.getBooleanArray().length);
        for(int i = 0; i < booleanArr.length; ++i) {
            assertTrue(booleanArr[i] == message.getBooleanArray()[i]);
        }

        byte[] byteArr = unpacker.read(byte[].class);
        assertTrue(byteArr.length == message.getByteArray().length);
        for(int i = 0; i < byteArr.length; ++i) {
            assertTrue(byteArr[i] == message.getByteArray()[i]);
        }

        short[] shortArr = unpacker.read(short[].class);
        assertTrue(shortArr.length == message.getShortArray().length);
        for(int i = 0; i < shortArr.length; ++i) {
            assertTrue(shortArr[i] == message.getShortArray()[i]);
        }

        int[] intArr = unpacker.read(int[].class);
        assertTrue(intArr.length == message.getIntArray().length);
        for(int i = 0; i < intArr.length; ++i) {
            assertTrue(intArr[i] == message.getIntArray()[i]);
        }

        float[] floatArr = unpacker.read(float[].class);
        assertTrue(floatArr.length == message.getFloatArray().length);
        for(int i = 0; i < floatArr.length; ++i) {
            assertTrue(floatArr[i] == message.getFloatArray()[i]);
        }

        long[] longArr = unpacker.read(long[].class);
        assertTrue(longArr.length == message.getLongArray().length);
        for(int i = 0; i < longArr.length; ++i) {
            assertTrue(longArr[i] == message.getLongArray()[i]);
        }

        double[] doubleArr = unpacker.read(double[].class);
        assertTrue(doubleArr.length == message.getDoubleArray().length);
        for(int i = 0; i < doubleArr.length; ++i) {
            assertTrue(doubleArr[i] == message.getDoubleArray()[i]);
        }

        String[] stringArr = unpacker.read(String[].class);
        assertTrue(stringArr.length == message.getStringArray().length);
        for(int i = 0; i < stringArr.length; ++i) {
            assertTrue(stringArr[i].equals(message.getStringArray()[i]));
        }

        List<Boolean> booleanList = unpacker.read(Templates.tList(Templates.TBoolean));
        assertTrue(booleanList.size() == message.getBooleanList().size());
        for(int i = 0; i < booleanList.size(); ++i) {
            assertTrue(booleanList.get(i) == message.getBooleanList().get(i));
        }

        List<Byte> byteList = unpacker.read(Templates.tList(Templates.TByte));
        assertTrue(byteList.size() == message.getByteList().size());
        for(int i = 0; i < byteList.size(); ++i) {
            assertTrue(byteList.get(i) == message.getByteList().get(i));
        }

        List<Short> shortList = unpacker.read(Templates.tList(Templates.TShort));
        assertTrue(shortList.size() == message.getShortList().size());
        for(int i = 0; i < shortList.size(); ++i) {
            assertTrue(shortList.get(i).equals(message.getShortList().get(i)));
        }

        List<Integer> intList = unpacker.read(Templates.tList(Templates.TInteger));
        assertTrue(intList.size() == message.getIntList().size());
        for(int i = 0; i < intList.size(); ++i) {
            assertTrue(intList.get(i).equals(message.getIntList().get(i)));
        }

        List<Float> floatList = unpacker.read(Templates.tList(Templates.TFloat));
        assertTrue(floatList.size() == message.getFloatList().size());
        for(int i = 0; i < floatList.size(); ++i) {
            assertTrue(floatList.get(i).equals(message.getFloatList().get(i)));
        }

        List<Long> longList = unpacker.read(Templates.tList(Templates.TLong));
        assertTrue(longList.size() == message.getLongList().size());
        for(int i = 0; i < longList.size(); ++i) {
            assertTrue(longList.get(i).equals(message.getLongList().get(i)));
        }

        List<Double> doubleList = unpacker.read(Templates.tList(Templates.TDouble));
        assertTrue(doubleList.size() == message.getDoubleList().size());
        for(int i = 0; i < doubleList.size(); ++i) {
            assertTrue(doubleList.get(i).equals(message.getDoubleList().get(i)));
        }

        List<String> stringList = unpacker.read(Templates.tList(Templates.TString));
        assertTrue(stringList.size() == message.getStringList().size());
        for(int i = 0; i < stringList.size(); ++i) {
            assertTrue(stringList.get(i).equals(message.getStringList().get(i)));
        }

        List<byte[]> ecefList = unpacker.read(Templates.tList(Templates.TByteArray));
        assertTrue(ecefList.size() == message.getEcefList().size());
        for(int i = 0; i < ecefList.size(); ++i) {
            ecefUnpacker = msgPack.createBufferUnpacker(ecefList.get(i));
            assertTrue(ecefUnpacker.readDouble() == message.getEcefList().get(i).getX());
            assertTrue(ecefUnpacker.readDouble() == message.getEcefList().get(i).getY());
            assertTrue(ecefUnpacker.readDouble() == message.getEcefList().get(i).getZ());
        }
        
        throwed = false;
        try {
            assertTrue(unpacker.getNextType() == ValueType.NIL);
        } catch(EndOfBufferException ex) {
            throwed = true;
        }
        assertTrue(throwed);
    }

    private TestMessage createMessage() {
        TestMessage ret = new TestMessage();
        ret.setBooleanValue(rand.nextBoolean());
        ret.setByteValue(Byte.decode("0x10"));
        ret.setShortValue((short)rand.nextInt());
        ret.setIntValue(rand.nextInt());
        ret.setFloatValue(rand.nextFloat());
        ret.setLongValue(rand.nextLong());
        ret.setDoubleValue(rand.nextDouble());
        ret.setStringValue("Andy is the greatest");
        ret.setEcefValue(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.setBooleanArray(new boolean[] {true, false});
        ret.setByteArray(new byte[] { Byte.decode("0x12"), Byte.decode("0x13") });
        ret.setShortArray(new short[] { (short)rand.nextInt(), (short)rand.nextInt() });
        ret.setIntArray(new int[] { rand.nextInt(), rand.nextInt(), rand.nextInt() });
        ret.setFloatArray(new float[] { rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat() });
        ret.setLongArray(new long[] { rand.nextLong(), rand.nextLong(), rand.nextLong(), rand.nextLong() });
        ret.setDoubleArray(new double[] { rand.nextDouble(), rand.nextDouble() });
        ret.setStringArray(new String[] { "Andy", "is", "absolutely", "rad" });

        ret.getBooleanList().add(true);
        ret.getBooleanList().add(false);
        ret.getByteList().add(Byte.decode("0x0a"));
        ret.getByteList().add(Byte.decode("0x00"));
        ret.getShortList().add((short)rand.nextInt());
        ret.getShortList().add((short)rand.nextInt());
        ret.getShortList().add((short)rand.nextInt());
        ret.getIntList().add(rand.nextInt());
        ret.getIntList().add(rand.nextInt());
        ret.getIntList().add(rand.nextInt());
        ret.getIntList().add(rand.nextInt());
        ret.getIntList().add(rand.nextInt());
        ret.getFloatList().add(rand.nextFloat());
        ret.getFloatList().add(rand.nextFloat());
        ret.getLongList().add(rand.nextLong());
        ret.getLongList().add(rand.nextLong());
        ret.getLongList().add(rand.nextLong());
        ret.getLongList().add(rand.nextLong());
        ret.getDoubleList().add(rand.nextDouble());
        ret.getDoubleList().add(rand.nextDouble());
        ret.getDoubleList().add(rand.nextDouble());
        ret.getStringList().add("Hi");
        ret.getStringList().add("there");
        ret.getStringList().add("world");
        ret.getEcefList().add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefList().add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefList().add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        return ret;
    }
}
