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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.collections.CollectionUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
public class GenericCodingTest {

    private static final Logger LOG = LoggerFactory.getLogger(GenericCodingTest.class);

    private final Random rand = new Random();

//    @Test
//    public void testSizes() throws Exception {
//        int runs = 100000;
//        long sizeSum = 0;
//        int encodeTimeSum = 0;
//        int decodeTimeSum = 0;
//        
//        long time;
//        double avgSize;
//        double avgEncodeTime;
//        double avgDecodeTime;
//
//        for (int i = 0; i < runs; ++i) {
//            TestMessage m1 = createMessage();
//            TestMessage m2 = new TestMessage();
//
//            time = System.currentTimeMillis();
//            byte[] arr = m1.encode();
//            encodeTimeSum += System.currentTimeMillis() - time;
//            sizeSum += arr.length;
//
//            time = System.currentTimeMillis();
//            m2.decode(arr);
//            decodeTimeSum += System.currentTimeMillis() - time;
//        }
//
//        avgEncodeTime = (double) encodeTimeSum / (double) runs;
//        avgDecodeTime = (double) decodeTimeSum / (double) runs;
//        avgSize = (double) sizeSum / (double) runs;
//
//        LOG.info("MsgPack Serialize");
//        LOG.info("Average Size: " + avgSize);
//        LOG.info("Average Encode Time: " + avgEncodeTime);
//        LOG.info("Average Decode Time: " + avgDecodeTime);
//        LOG.info("Total Encode Time: " + encodeTimeSum);
//        LOG.info("Total Decode Time: " + decodeTimeSum);
//
//        encodeTimeSum = 0;
//        decodeTimeSum = 0;
//        
//        for(int i = 0; i < runs; ++i) {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            ObjectOutput out = null;
//            byte[] arr;
//            try {
//                out = new ObjectOutputStream(bos);
//                TestMessage m = createMessage();
//
//                time = System.currentTimeMillis();
//                out.writeObject(m);
//                arr = bos.toByteArray();
//                encodeTimeSum += System.currentTimeMillis() - time;
//                sizeSum += arr.length;
//            } finally {
//                try {
//                    if (out != null) {
//                        out.close();
//                    }
//                } catch (IOException ex) {
//                    // ignore close exception
//                }
//                try {
//                    bos.close();
//                } catch (IOException ex) {
//                    // ignore close exception
//                }
//            }
//
//            ByteArrayInputStream bis = new ByteArrayInputStream(arr);
//            ObjectInput in = null;
//            try {
//              in = new ObjectInputStream(bis);
//
//              time = System.currentTimeMillis();
//              Object o = in.readObject(); 
//              decodeTimeSum += System.currentTimeMillis() - time;
//            } finally {
//              try {
//                bis.close();
//              } catch (IOException ex) {
//                // ignore close exception
//              }
//              try {
//                if (in != null) {
//                  in.close();
//                }
//              } catch (IOException ex) {
//                // ignore close exception
//              }
//            }
//        }
//
//        avgEncodeTime = (double) encodeTimeSum / (double) runs;
//        avgDecodeTime = (double) decodeTimeSum / (double) runs;
//        avgSize = (double) sizeSum / (double) runs;
//
//        LOG.info("Java Serialize");
//        LOG.info("Average Size: " + avgSize);
//        LOG.info("Average Encode Time: " + avgEncodeTime);
//        LOG.info("Average Decode Time: " + avgDecodeTime);
//        LOG.info("Total Encode Time: " + encodeTimeSum);
//        LOG.info("Total Decode Time: " + decodeTimeSum);
//    }

//    @Test
    public void testGeneratedSizes() throws Exception {
        int runs = 100000;
        long sizeSum = 0;
        int encodeTimeSum = 0;
        int decodeTimeSum = 0;
        
        long time;
        double avgSize;
        double avgEncodeTime;
        double avgDecodeTime;

        Method encodeMethod = TestMessage.class.getMethod("_encode_");
        Method decodeMethod = TestMessage.class.getMethod("_decode_", byte[].class);

        for (int i = 0; i < runs; ++i) {
            TestMessage m1 = createMessage();
            TestMessage m2 = new TestMessage();

            time = System.currentTimeMillis();
            byte[] arr = (byte[])encodeMethod.invoke(m1);
            encodeTimeSum += System.currentTimeMillis() - time;
            sizeSum += arr.length;

            time = System.currentTimeMillis();
            decodeMethod.invoke(m2, arr);
            decodeTimeSum += System.currentTimeMillis() - time;
        }

        avgEncodeTime = (double) encodeTimeSum / (double) runs;
        avgDecodeTime = (double) decodeTimeSum / (double) runs;
        avgSize = (double) sizeSum / (double) runs;

        LOG.info("MsgPack Serialize");
        LOG.info("Average Size: " + avgSize);
        LOG.info("Average Encode Time: " + avgEncodeTime);
        LOG.info("Average Decode Time: " + avgDecodeTime);
        LOG.info("Total Encode Time: " + encodeTimeSum);
        LOG.info("Total Decode Time: " + decodeTimeSum);

        encodeTimeSum = 0;
        decodeTimeSum = 0;
        
        for(int i = 0; i < runs; ++i) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = null;
            byte[] arr;
            try {
                out = new ObjectOutputStream(bos);
                TestMessage m = createMessage();

                time = System.currentTimeMillis();
                out.writeObject(m);
                arr = bos.toByteArray();
                encodeTimeSum += System.currentTimeMillis() - time;
                sizeSum += arr.length;
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ex) {
                    // ignore close exception
                }
                try {
                    bos.close();
                } catch (IOException ex) {
                    // ignore close exception
                }
            }

            ByteArrayInputStream bis = new ByteArrayInputStream(arr);
            ObjectInput in = null;
            try {
              in = new ObjectInputStream(bis);

              time = System.currentTimeMillis();
              Object o = in.readObject(); 
              decodeTimeSum += System.currentTimeMillis() - time;
            } finally {
              try {
                bis.close();
              } catch (IOException ex) {
                // ignore close exception
              }
              try {
                if (in != null) {
                  in.close();
                }
              } catch (IOException ex) {
                // ignore close exception
              }
            }
        }

        avgEncodeTime = (double) encodeTimeSum / (double) runs;
        avgDecodeTime = (double) decodeTimeSum / (double) runs;
        avgSize = (double) sizeSum / (double) runs;

        LOG.info("Java Serialize");
        LOG.info("Average Size: " + avgSize);
        LOG.info("Average Encode Time: " + avgEncodeTime);
        LOG.info("Average Decode Time: " + avgDecodeTime);
        LOG.info("Total Encode Time: " + encodeTimeSum);
        LOG.info("Total Decode Time: " + decodeTimeSum);
    }

//    @Test
    public void test_Encode() throws Exception {
        TestMessage message = createMessage();
        TestMessage decodedMessage = new TestMessage();

        byte[] bytes = (byte[])message.getClass().getMethod("_encode_").invoke(message);
        decodedMessage.getClass().getMethod("_decode_", byte[].class).invoke(decodedMessage, bytes);

        testMessageEquality(message, decodedMessage);
    }

//    @Test
    public void testEncode() throws Exception {
        TestMessage message = createMessage();
        TestMessage decodedMessage = new TestMessage();
        decodedMessage.decode(message.encode());

        testMessageEquality(message, decodedMessage);
    }

    private void testMessageEquality(TestMessage m1, TestMessage m2) {
        assertTrue(m1.isBooleanValue() == m2.isBooleanValue());
        assertTrue(m1.getByteValue() == m2.getByteValue());
        assertTrue(m1.getShortValue() == m2.getShortValue());
        assertTrue(m1.getIntValue() == m1.getIntValue());
        assertTrue(m1.getFloatValue() == m2.getFloatValue());
        assertTrue(m1.getLongValue() == m2.getLongValue());
        assertTrue(m1.getDoubleValue() == m2.getDoubleValue());
        assertTrue(m1.getStringValue().equals(m2.getStringValue()));
        assertTrue(m1.getEcefValue().equals(m2.getEcefValue()));
        assertEquals(m1.getEnumValue(), m2.getEnumValue());

        assertTrue(Arrays.equals(m1.getBooleanArray(), m2.getBooleanArray()));
        assertTrue(Arrays.equals(m1.getByteArray(), m2.getByteArray()));
        assertTrue(Arrays.equals(m1.getShortArray(), m2.getShortArray()));
        assertTrue(Arrays.equals(m1.getIntArray(), m2.getIntArray()));
        assertTrue(Arrays.equals(m1.getFloatArray(), m2.getFloatArray()));
        assertTrue(Arrays.equals(m1.getLongArray(), m2.getLongArray()));
        assertTrue(Arrays.equals(m1.getDoubleArray(), m2.getDoubleArray()));
        assertTrue(Arrays.equals(m1.getStringArray(), m2.getStringArray()));

        assertTrue(CollectionUtils.isEqualCollection(m1.getBooleanList(), m2.getBooleanList()));
        assertTrue(CollectionUtils.isEqualCollection(m1.getByteList(), m2.getByteList()));
        assertTrue(CollectionUtils.isEqualCollection(m1.getShortList(), m2.getShortList()));
        assertTrue(CollectionUtils.isEqualCollection(m1.getIntList(), m2.getIntList()));
        assertTrue(CollectionUtils.isEqualCollection(m1.getFloatList(), m2.getFloatList()));
        assertTrue(CollectionUtils.isEqualCollection(m1.getLongList(), m2.getLongList()));
        assertTrue(CollectionUtils.isEqualCollection(m1.getDoubleList(), m2.getDoubleList()));
        assertTrue(CollectionUtils.isEqualCollection(m1.getStringList(), m2.getStringList()));

        assertTrue(m1.getEcefList().size() == m2.getEcefList().size());
        for (int i = 0; i < m1.getEcefList().size(); ++i) {
            assertTrue(m1.getEcefList().get(i).equals(m2.getEcefList().get(i)));
        }

        assertTrue(m1.getBoolean2DList().size() == m2.getBoolean2DList().size());
        for (int i = 0; i < m1.getBoolean2DList().size(); ++i) {
            assertTrue(m1.getBoolean2DList().get(i).size() == m2.getBoolean2DList().get(i).size());
            for (int j = 0; j < m1.getBoolean2DList().get(i).size(); ++j) {
                assertTrue(m1.getBoolean2DList().get(i).get(j) == m2.getBoolean2DList().get(i).get(j));
            }
        }

        assertTrue(m1.getByte2DList().size() == m2.getByte2DList().size());
        for (int i = 0; i < m1.getByte2DList().size(); ++i) {
            assertTrue(m1.getByte2DList().get(i).size() == m2.getByte2DList().get(i).size());
            for (int j = 0; j < m1.getByte2DList().get(i).size(); ++j) {
                assertTrue(m1.getByte2DList().get(i).get(j) == m2.getByte2DList().get(i).get(j));
            }
        }

        assertTrue(m1.getShort2DList().size() == m2.getShort2DList().size());
        for (int i = 0; i < m1.getShort2DList().size(); ++i) {
            assertTrue(m1.getShort2DList().get(i).size() == m2.getShort2DList().get(i).size());
            for (int j = 0; j < m1.getShort2DList().get(i).size(); ++j) {
                assertTrue(m1.getShort2DList().get(i).get(j).equals(m2.getShort2DList().get(i).get(j)));
            }
        }

        assertTrue(m1.getInt2DList().size() == m2.getInt2DList().size());
        for (int i = 0; i < m1.getInt2DList().size(); ++i) {
            assertTrue(m1.getInt2DList().get(i).size() == m2.getInt2DList().get(i).size());
            for (int j = 0; j < m1.getInt2DList().get(i).size(); ++j) {
                assertTrue(m1.getInt2DList().get(i).get(j).equals(m2.getInt2DList().get(i).get(j)));
            }
        }

        assertTrue(m1.getFloat2DList().size() == m2.getFloat2DList().size());
        for (int i = 0; i < m1.getFloat2DList().size(); ++i) {
            assertTrue(m1.getFloat2DList().get(i).size() == m2.getFloat2DList().get(i).size());
            for (int j = 0; j < m1.getFloat2DList().get(i).size(); ++j) {
                assertTrue(m1.getFloat2DList().get(i).get(j).equals(m2.getFloat2DList().get(i).get(j)));
            }
        }

        assertTrue(m1.getLong2DList().size() == m2.getLong2DList().size());
        for (int i = 0; i < m1.getLong2DList().size(); ++i) {
            assertTrue(m1.getLong2DList().get(i).size() == m2.getLong2DList().get(i).size());
            for (int j = 0; j < m1.getLong2DList().get(i).size(); ++j) {
                assertTrue(m1.getLong2DList().get(i).get(j).equals(m2.getLong2DList().get(i).get(j)));
            }
        }

        assertTrue(m1.getDouble2DList().size() == m2.getDouble2DList().size());
        for (int i = 0; i < m1.getDouble2DList().size(); ++i) {
            assertTrue(m1.getDouble2DList().get(i).size() == m2.getDouble2DList().get(i).size());
            for (int j = 0; j < m1.getDouble2DList().get(i).size(); ++j) {
                assertTrue(m1.getDouble2DList().get(i).get(j).equals(m2.getDouble2DList().get(i).get(j)));
            }
        }

        assertTrue(m1.getString2DList().size() == m2.getString2DList().size());
        for (int i = 0; i < m1.getString2DList().size(); ++i) {
            assertTrue(m1.getString2DList().get(i).size() == m2.getString2DList().get(i).size());
            for (int j = 0; j < m1.getString2DList().get(i).size(); ++j) {
                assertTrue(m1.getString2DList().get(i).get(j).equals(m2.getString2DList().get(i).get(j)));
            }
        }

        assertTrue(m1.getEcef2DList().size() == m2.getEcef2DList().size());
        for (int i = 0; i < m1.getEcef2DList().size(); ++i) {
            assertTrue(m1.getEcef2DList().get(i).size() == m2.getEcef2DList().get(i).size());
            for (int j = 0; j < m1.getEcef2DList().get(i).size(); ++j) {
                assertTrue(m1.getEcef2DList().get(i).get(j).equals(m2.getEcef2DList().get(i).get(j)));
            }
        }

        assertTrue(m1.getBooleanMap().keySet().size() == m2.getBooleanMap().keySet().size());
        for (String key : m1.getBooleanMap().keySet()) {
            assertTrue(m1.getBooleanMap().get(key).equals(m2.getBooleanMap().get(key)));
        }

        assertTrue(m1.getByteMap().keySet().size() == m2.getByteMap().keySet().size());
        for (String key : m1.getByteMap().keySet()) {
            assertTrue(m1.getByteMap().get(key).equals(m2.getByteMap().get(key)));
        }

        assertTrue(m1.getShortMap().keySet().size() == m2.getShortMap().keySet().size());
        for (String key : m1.getShortMap().keySet()) {
            assertTrue(m1.getShortMap().get(key).equals(m2.getShortMap().get(key)));
        }

        assertTrue(m1.getIntMap().keySet().size() == m2.getIntMap().keySet().size());
        for (String key : m1.getIntMap().keySet()) {
            assertTrue(m1.getIntMap().get(key).equals(m2.getIntMap().get(key)));
        }

        assertTrue(m1.getFloatMap().keySet().size() == m2.getFloatMap().keySet().size());
        for (String key : m1.getFloatMap().keySet()) {
            assertTrue(m1.getFloatMap().get(key).equals(m2.getFloatMap().get(key)));
        }

        assertTrue(m1.getLongMap().keySet().size() == m2.getLongMap().keySet().size());
        for (String key : m1.getLongMap().keySet()) {
            assertTrue(m1.getLongMap().get(key).equals(m2.getLongMap().get(key)));
        }

        assertTrue(m1.getDoubleMap().keySet().size() == m2.getDoubleMap().keySet().size());
        for (String key : m1.getDoubleMap().keySet()) {
            assertTrue(m1.getDoubleMap().get(key).equals(m2.getDoubleMap().get(key)));
        }

        assertTrue(m1.getStringMap().keySet().size() == m2.getStringMap().keySet().size());
        for (String key : m1.getStringMap().keySet()) {
            assertTrue(m1.getStringMap().get(key).equals(m2.getStringMap().get(key)));
        }

        assertTrue(m1.getEcefMap().keySet().size() == m2.getEcefMap().keySet().size());
        for (String key : m1.getEcefMap().keySet()) {
            assertTrue(m1.getEcefMap().get(key).equals(m2.getEcefMap().get(key)));
        }

        assertTrue(m1.getBooleanListMap().keySet().size() == m2.getBooleanListMap().keySet().size());
        for (String key : m1.getBooleanListMap().keySet()) {
            for (int i = 0; i < m1.getBooleanListMap().get(key).size(); ++i) {
                assertTrue(m1.getBooleanListMap().get(key).get(i).equals(m2.getBooleanListMap().get(key).get(i)));
            }
        }

        assertTrue(m1.getByteListMap().keySet().size() == m2.getByteListMap().keySet().size());
        for (String key : m1.getByteListMap().keySet()) {
            for (int i = 0; i < m1.getByteListMap().get(key).size(); ++i) {
                assertTrue(m1.getByteListMap().get(key).get(i).equals(m2.getByteListMap().get(key).get(i)));
            }
        }

        assertTrue(m1.getShortListMap().keySet().size() == m2.getShortListMap().keySet().size());
        for (String key : m1.getShortListMap().keySet()) {
            for (int i = 0; i < m1.getShortListMap().get(key).size(); ++i) {
                assertTrue(m1.getShortListMap().get(key).get(i).equals(m2.getShortListMap().get(key).get(i)));
            }
        }

        assertTrue(m1.getIntListMap().keySet().size() == m2.getIntListMap().keySet().size());
        for (String key : m1.getIntListMap().keySet()) {
            for (int i = 0; i < m1.getIntListMap().get(key).size(); ++i) {
                assertTrue(m1.getIntListMap().get(key).get(i).equals(m2.getIntListMap().get(key).get(i)));
            }
        }

        assertTrue(m1.getFloatListMap().keySet().size() == m2.getFloatListMap().keySet().size());
        for (String key : m1.getFloatListMap().keySet()) {
            for (int i = 0; i < m1.getFloatListMap().get(key).size(); ++i) {
                assertTrue(m1.getFloatListMap().get(key).get(i).equals(m2.getFloatListMap().get(key).get(i)));
            }
        }

        assertTrue(m1.getLongListMap().keySet().size() == m2.getLongListMap().keySet().size());
        for (String key : m1.getLongListMap().keySet()) {
            for (int i = 0; i < m1.getLongListMap().get(key).size(); ++i) {
                assertTrue(m1.getLongListMap().get(key).get(i).equals(m2.getLongListMap().get(key).get(i)));
            }
        }

        assertTrue(m1.getDoubleListMap().keySet().size() == m2.getDoubleListMap().keySet().size());
        for (String key : m1.getDoubleListMap().keySet()) {
            for (int i = 0; i < m1.getDoubleListMap().get(key).size(); ++i) {
                assertTrue(m1.getDoubleListMap().get(key).get(i).equals(m2.getDoubleListMap().get(key).get(i)));
            }
        }

        assertTrue(m1.getStringListMap().keySet().size() == m2.getStringListMap().keySet().size());
        for (String key : m1.getStringListMap().keySet()) {
            for (int i = 0; i < m1.getStringListMap().get(key).size(); ++i) {
                assertTrue(m1.getStringListMap().get(key).get(i).equals(m2.getStringListMap().get(key).get(i)));
            }
        }

        assertTrue(m1.getEcefListMap().keySet().size() == m2.getEcefListMap().keySet().size());
        for (String key : m1.getEcefListMap().keySet()) {
            for (int i = 0; i < m1.getEcefListMap().get(key).size(); ++i) {
                assertTrue(m1.getEcefListMap().get(key).get(i).equals(m2.getEcefListMap().get(key).get(i)));
            }
        }
    }

//    @Test
    public void test_Encode_() throws Exception {
        TestMessage message = createMessage();

        byte[] arr = (byte[]) message.getClass().getMethod("_encode_").invoke(message);

        StringBuilder buff = new StringBuilder();
        buff.append(arr.length).append(" bytes:").append("\n");
        for (int i = 0; i < arr.length; ++i) {
            buff.append(String.format("%02x ", arr[i] & 0xff));
            if ((i + 1) % 16 == 0) {
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
        } catch (EndOfBufferException ex) {
            throwed = true;
        }
        assertTrue(throwed);

        boolean[] booleanArr = unpacker.read(boolean[].class);
        assertTrue(booleanArr.length == message.getBooleanArray().length);
        for (int i = 0; i < booleanArr.length; ++i) {
            assertTrue(booleanArr[i] == message.getBooleanArray()[i]);
        }

        byte[] byteArr = unpacker.read(byte[].class);
        assertTrue(byteArr.length == message.getByteArray().length);
        for (int i = 0; i < byteArr.length; ++i) {
            assertTrue(byteArr[i] == message.getByteArray()[i]);
        }

        short[] shortArr = unpacker.read(short[].class);
        assertTrue(shortArr.length == message.getShortArray().length);
        for (int i = 0; i < shortArr.length; ++i) {
            assertTrue(shortArr[i] == message.getShortArray()[i]);
        }

        int[] intArr = unpacker.read(int[].class);
        assertTrue(intArr.length == message.getIntArray().length);
        for (int i = 0; i < intArr.length; ++i) {
            assertTrue(intArr[i] == message.getIntArray()[i]);
        }

        float[] floatArr = unpacker.read(float[].class);
        assertTrue(floatArr.length == message.getFloatArray().length);
        for (int i = 0; i < floatArr.length; ++i) {
            assertTrue(floatArr[i] == message.getFloatArray()[i]);
        }

        long[] longArr = unpacker.read(long[].class);
        assertTrue(longArr.length == message.getLongArray().length);
        for (int i = 0; i < longArr.length; ++i) {
            assertTrue(longArr[i] == message.getLongArray()[i]);
        }

        double[] doubleArr = unpacker.read(double[].class);
        assertTrue(doubleArr.length == message.getDoubleArray().length);
        for (int i = 0; i < doubleArr.length; ++i) {
            assertTrue(doubleArr[i] == message.getDoubleArray()[i]);
        }

        String[] stringArr = unpacker.read(String[].class);
        assertTrue(stringArr.length == message.getStringArray().length);
        for (int i = 0; i < stringArr.length; ++i) {
            assertTrue(stringArr[i].equals(message.getStringArray()[i]));
        }

        List<Boolean> booleanList = unpacker.read(Templates.tList(Templates.TBoolean));
        assertTrue(booleanList.size() == message.getBooleanList().size());
        for (int i = 0; i < booleanList.size(); ++i) {
            assertTrue(booleanList.get(i) == message.getBooleanList().get(i));
        }

        List<Byte> byteList = unpacker.read(Templates.tList(Templates.TByte));
        assertTrue(byteList.size() == message.getByteList().size());
        for (int i = 0; i < byteList.size(); ++i) {
            assertTrue(byteList.get(i) == message.getByteList().get(i));
        }

        List<Short> shortList = unpacker.read(Templates.tList(Templates.TShort));
        assertTrue(shortList.size() == message.getShortList().size());
        for (int i = 0; i < shortList.size(); ++i) {
            assertTrue(shortList.get(i).equals(message.getShortList().get(i)));
        }

        List<Integer> intList = unpacker.read(Templates.tList(Templates.TInteger));
        assertTrue(intList.size() == message.getIntList().size());
        for (int i = 0; i < intList.size(); ++i) {
            assertTrue(intList.get(i).equals(message.getIntList().get(i)));
        }

        List<Float> floatList = unpacker.read(Templates.tList(Templates.TFloat));
        assertTrue(floatList.size() == message.getFloatList().size());
        for (int i = 0; i < floatList.size(); ++i) {
            assertTrue(floatList.get(i).equals(message.getFloatList().get(i)));
        }

        List<Long> longList = unpacker.read(Templates.tList(Templates.TLong));
        assertTrue(longList.size() == message.getLongList().size());
        for (int i = 0; i < longList.size(); ++i) {
            assertTrue(longList.get(i).equals(message.getLongList().get(i)));
        }

        List<Double> doubleList = unpacker.read(Templates.tList(Templates.TDouble));
        assertTrue(doubleList.size() == message.getDoubleList().size());
        for (int i = 0; i < doubleList.size(); ++i) {
            assertTrue(doubleList.get(i).equals(message.getDoubleList().get(i)));
        }

        List<String> stringList = unpacker.read(Templates.tList(Templates.TString));
        assertTrue(stringList.size() == message.getStringList().size());
        for (int i = 0; i < stringList.size(); ++i) {
            assertTrue(stringList.get(i).equals(message.getStringList().get(i)));
        }

        List<byte[]> ecefList = unpacker.read(Templates.tList(Templates.TByteArray));
        assertTrue(ecefList.size() == message.getEcefList().size());
        for (int i = 0; i < ecefList.size(); ++i) {
            ecefUnpacker = msgPack.createBufferUnpacker(ecefList.get(i));
            assertTrue(ecefUnpacker.readDouble() == message.getEcefList().get(i).getX());
            assertTrue(ecefUnpacker.readDouble() == message.getEcefList().get(i).getY());
            assertTrue(ecefUnpacker.readDouble() == message.getEcefList().get(i).getZ());
        }

        throwed = false;
        try {
            assertTrue(unpacker.getNextType() == ValueType.NIL);
        } catch (EndOfBufferException ex) {
            throwed = true;
        }
        assertTrue(throwed);
    }

    private TestMessage createMessage() {
        TestMessage ret = new TestMessage();
        ret.setBooleanValue(rand.nextBoolean());
        ret.setByteValue(Byte.decode("0x10"));
        ret.setShortValue((short) rand.nextInt());
        ret.setIntValue(rand.nextInt());
        ret.setFloatValue(rand.nextFloat());
        ret.setLongValue(rand.nextLong());
        ret.setDoubleValue(rand.nextDouble());
        ret.setStringValue("Andy is the greatest");
        ret.setEcefValue(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.setBooleanArray(new boolean[]{true, false});
        ret.setByteArray(new byte[]{Byte.decode("0x12"), Byte.decode("0x13")});
        ret.setShortArray(new short[]{(short) rand.nextInt(), (short) rand.nextInt()});
        ret.setIntArray(new int[]{rand.nextInt(), rand.nextInt(), rand.nextInt()});
        ret.setFloatArray(new float[]{rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()});
        ret.setLongArray(new long[]{rand.nextLong(), rand.nextLong(), rand.nextLong(), rand.nextLong()});
        ret.setDoubleArray(new double[]{rand.nextDouble(), rand.nextDouble()});
        ret.setStringArray(new String[]{"Andy", "is", "absolutely", "rad"});
        ret.setEnumValue(TestMessage.TestEnum.Test);

        ret.getBooleanList().add(true);
        ret.getBooleanList().add(false);
        ret.getByteList().add(Byte.decode("0x0a"));
        ret.getByteList().add(Byte.decode("0x00"));
        ret.getShortList().add((short) rand.nextInt());
        ret.getShortList().add((short) rand.nextInt());
        ret.getShortList().add((short) rand.nextInt());
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

        ret.getBoolean2DList().add(new ArrayList<Boolean>());
        ret.getBoolean2DList().get(0).add(true);
        ret.getBoolean2DList().get(0).add(true);
        ret.getBoolean2DList().add(new ArrayList<Boolean>());
        ret.getBoolean2DList().get(1).add(false);
        ret.getBoolean2DList().get(1).add(false);
        ret.getBoolean2DList().add(new ArrayList<Boolean>());
        ret.getBoolean2DList().get(2).add(true);
        ret.getBoolean2DList().get(2).add(false);

        ret.getByte2DList().add(new ArrayList<Byte>());
        ret.getByte2DList().get(0).add(Byte.decode("0x00"));
        ret.getByte2DList().get(0).add(Byte.decode("0x01"));
        ret.getByte2DList().add(new ArrayList<Byte>());
        ret.getByte2DList().get(1).add(Byte.decode("0x02"));
        ret.getByte2DList().get(1).add(Byte.decode("0x03"));
        ret.getByte2DList().add(new ArrayList<Byte>());
        ret.getByte2DList().get(2).add(Byte.decode("0x04"));
        ret.getByte2DList().get(2).add(Byte.decode("0x05"));

        ret.getShort2DList().add(new ArrayList<Short>());
        ret.getShort2DList().get(0).add((short) rand.nextInt());
        ret.getShort2DList().get(0).add((short) rand.nextInt());
        ret.getShort2DList().add(new ArrayList<Short>());
        ret.getShort2DList().get(1).add((short) rand.nextInt());
        ret.getShort2DList().get(1).add((short) rand.nextInt());
        ret.getShort2DList().add(new ArrayList<Short>());
        ret.getShort2DList().get(2).add((short) rand.nextInt());
        ret.getShort2DList().get(2).add((short) rand.nextInt());

        ret.getInt2DList().add(new ArrayList<Integer>());
        ret.getInt2DList().get(0).add(rand.nextInt());
        ret.getInt2DList().get(0).add(rand.nextInt());
        ret.getInt2DList().add(new ArrayList<Integer>());
        ret.getInt2DList().get(1).add(rand.nextInt());
        ret.getInt2DList().get(1).add(rand.nextInt());
        ret.getInt2DList().add(new ArrayList<Integer>());
        ret.getInt2DList().get(2).add(rand.nextInt());
        ret.getInt2DList().get(2).add(rand.nextInt());

        ret.getFloat2DList().add(new ArrayList<Float>());
        ret.getFloat2DList().get(0).add(rand.nextFloat());
        ret.getFloat2DList().get(0).add(rand.nextFloat());
        ret.getFloat2DList().add(new ArrayList<Float>());
        ret.getFloat2DList().get(1).add(rand.nextFloat());
        ret.getFloat2DList().get(1).add(rand.nextFloat());
        ret.getFloat2DList().add(new ArrayList<Float>());
        ret.getFloat2DList().get(2).add(rand.nextFloat());
        ret.getFloat2DList().get(2).add(rand.nextFloat());

        ret.getLong2DList().add(new ArrayList<Long>());
        ret.getLong2DList().get(0).add(rand.nextLong());
        ret.getLong2DList().get(0).add(rand.nextLong());
        ret.getLong2DList().add(new ArrayList<Long>());
        ret.getLong2DList().get(1).add(rand.nextLong());
        ret.getLong2DList().get(1).add(rand.nextLong());
        ret.getLong2DList().add(new ArrayList<Long>());
        ret.getLong2DList().get(2).add(rand.nextLong());
        ret.getLong2DList().get(2).add(rand.nextLong());

        ret.getDouble2DList().add(new ArrayList<Double>());
        ret.getDouble2DList().get(0).add(rand.nextDouble());
        ret.getDouble2DList().get(0).add(rand.nextDouble());
        ret.getDouble2DList().add(new ArrayList<Double>());
        ret.getDouble2DList().get(1).add(rand.nextDouble());
        ret.getDouble2DList().get(1).add(rand.nextDouble());
        ret.getDouble2DList().add(new ArrayList<Double>());
        ret.getDouble2DList().get(2).add(rand.nextDouble());
        ret.getDouble2DList().get(2).add(rand.nextDouble());

        ret.getString2DList().add(new ArrayList<String>());
        ret.getString2DList().get(0).add(Double.toString(rand.nextDouble()));
        ret.getString2DList().get(0).add(Double.toString(rand.nextDouble()));
        ret.getString2DList().add(new ArrayList<String>());
        ret.getString2DList().get(1).add(Double.toString(rand.nextDouble()));
        ret.getString2DList().get(1).add(Double.toString(rand.nextDouble()));
        ret.getString2DList().add(new ArrayList<String>());
        ret.getString2DList().get(2).add(Double.toString(rand.nextDouble()));
        ret.getString2DList().get(2).add(Double.toString(rand.nextDouble()));

        ret.getEcef2DList().add(new ArrayList<ECEF>());
        ret.getEcef2DList().get(0).add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcef2DList().get(0).add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcef2DList().add(new ArrayList<ECEF>());
        ret.getEcef2DList().get(1).add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcef2DList().get(1).add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcef2DList().add(new ArrayList<ECEF>());
        ret.getEcef2DList().get(2).add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcef2DList().get(2).add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));

        ret.getBooleanMap().put(Integer.toString(rand.nextInt()), Boolean.TRUE);
        ret.getBooleanMap().put(Integer.toString(rand.nextInt()), Boolean.TRUE);
        ret.getBooleanMap().put(Integer.toString(rand.nextInt()), Boolean.FALSE);

        ret.getByteMap().put(Integer.toString(rand.nextInt()), Byte.decode("0x00"));
        ret.getByteMap().put(Integer.toString(rand.nextInt()), Byte.decode("0x01"));
        ret.getByteMap().put(Integer.toString(rand.nextInt()), Byte.decode("0x02"));

        ret.getShortMap().put(Integer.toString(rand.nextInt()), (short) rand.nextInt());
        ret.getShortMap().put(Integer.toString(rand.nextInt()), (short) rand.nextInt());
        ret.getShortMap().put(Integer.toString(rand.nextInt()), (short) rand.nextInt());

        ret.getIntMap().put(Integer.toString(rand.nextInt()), rand.nextInt());
        ret.getIntMap().put(Integer.toString(rand.nextInt()), rand.nextInt());
        ret.getIntMap().put(Integer.toString(rand.nextInt()), rand.nextInt());

        ret.getFloatMap().put(Integer.toString(rand.nextInt()), rand.nextFloat());
        ret.getFloatMap().put(Integer.toString(rand.nextInt()), rand.nextFloat());
        ret.getFloatMap().put(Integer.toString(rand.nextInt()), rand.nextFloat());

        ret.getLongMap().put(Integer.toString(rand.nextInt()), rand.nextLong());
        ret.getLongMap().put(Integer.toString(rand.nextInt()), rand.nextLong());
        ret.getLongMap().put(Integer.toString(rand.nextInt()), rand.nextLong());

        ret.getDoubleMap().put(Integer.toString(rand.nextInt()), rand.nextDouble());
        ret.getDoubleMap().put(Integer.toString(rand.nextInt()), rand.nextDouble());
        ret.getDoubleMap().put(Integer.toString(rand.nextInt()), rand.nextDouble());

        ret.getStringMap().put(Integer.toString(rand.nextInt()), Integer.toString(rand.nextInt()));
        ret.getStringMap().put(Integer.toString(rand.nextInt()), Integer.toString(rand.nextInt()));
        ret.getStringMap().put(Integer.toString(rand.nextInt()), Integer.toString(rand.nextInt()));

        ret.getEcefMap().put(Integer.toString(rand.nextInt()), new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefMap().put(Integer.toString(rand.nextInt()), new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefMap().put(Integer.toString(rand.nextInt()), new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));

        ArrayList l = new ArrayList();
        l.add(true);
        l.add(true);
        ret.getBooleanListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(false);
        l.add(false);
        ret.getBooleanListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(true);
        l.add(false);
        ret.getBooleanListMap().put(Integer.toString(rand.nextInt()), l);

        l = new ArrayList();
        l.add(Byte.decode("0x00"));
        l.add(Byte.decode("0x01"));
        ret.getByteListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(Byte.decode("0x02"));
        l.add(Byte.decode("0x03"));
        ret.getByteListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(Byte.decode("0x04"));
        l.add(Byte.decode("0x05"));
        ret.getByteListMap().put(Integer.toString(rand.nextInt()), l);

        l = new ArrayList();
        l.add((short) rand.nextInt());
        l.add((short) rand.nextInt());
        ret.getShortListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add((short) rand.nextInt());
        l.add((short) rand.nextInt());
        ret.getShortListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add((short) rand.nextInt());
        l.add((short) rand.nextInt());
        ret.getShortListMap().put(Integer.toString(rand.nextInt()), l);

        l = new ArrayList();
        l.add(rand.nextInt());
        l.add(rand.nextInt());
        ret.getIntListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(rand.nextInt());
        l.add(rand.nextInt());
        ret.getIntListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(rand.nextInt());
        l.add(rand.nextInt());
        ret.getIntListMap().put(Integer.toString(rand.nextInt()), l);

        l = new ArrayList();
        l.add(rand.nextFloat());
        l.add(rand.nextFloat());
        ret.getFloatListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(rand.nextFloat());
        l.add(rand.nextFloat());
        ret.getFloatListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(rand.nextFloat());
        l.add(rand.nextFloat());
        ret.getFloatListMap().put(Integer.toString(rand.nextInt()), l);

        l = new ArrayList();
        l.add(rand.nextLong());
        l.add(rand.nextLong());
        ret.getLongListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(rand.nextLong());
        l.add(rand.nextLong());
        ret.getLongListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(rand.nextLong());
        l.add(rand.nextLong());
        ret.getLongListMap().put(Integer.toString(rand.nextInt()), l);

        l = new ArrayList();
        l.add(rand.nextDouble());
        l.add(rand.nextDouble());
        ret.getDoubleListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(rand.nextDouble());
        l.add(rand.nextDouble());
        ret.getDoubleListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(rand.nextDouble());
        l.add(rand.nextDouble());
        ret.getDoubleListMap().put(Integer.toString(rand.nextInt()), l);

        l = new ArrayList();
        l.add(Integer.toString(rand.nextInt()));
        l.add(Integer.toString(rand.nextInt()));
        ret.getStringListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(Integer.toString(rand.nextInt()));
        l.add(Integer.toString(rand.nextInt()));
        ret.getStringListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(Integer.toString(rand.nextInt()));
        l.add(Integer.toString(rand.nextInt()));
        ret.getStringListMap().put(Integer.toString(rand.nextInt()), l);

        l = new ArrayList();
        l.add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        l.add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        l.add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        l.add(new ECEF(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefListMap().put(Integer.toString(rand.nextInt()), l);

        return ret;
    }
}
