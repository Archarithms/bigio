/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.bigio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.apache.commons.collections.CollectionUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author atrimble
 */
public class MessageUtils {

    private static final Random rand = new Random();

    public static void testMessageEquality(RepMessage m1, RepMessage m2) {
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

    public static RepMessage createMessage() {
        RepMessage ret = new RepMessage();
        ret.setBooleanValue(rand.nextBoolean());
        ret.setByteValue(Byte.decode("0x10"));
        ret.setShortValue((short) rand.nextInt());
        ret.setIntValue(rand.nextInt());
        ret.setFloatValue(rand.nextFloat());
        ret.setLongValue(rand.nextLong());
        ret.setDoubleValue(rand.nextDouble());
        ret.setStringValue("Andy is the greatest");
        ret.setEcefValue(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.setBooleanArray(new boolean[]{true, false});
        ret.setShortArray(new short[]{(short) rand.nextInt(), (short) rand.nextInt()});
        ret.setIntArray(new int[]{rand.nextInt(), rand.nextInt(), rand.nextInt()});
        ret.setFloatArray(new float[]{rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), rand.nextFloat()});
        ret.setLongArray(new long[]{rand.nextLong(), rand.nextLong(), rand.nextLong(), rand.nextLong()});
        ret.setDoubleArray(new double[]{rand.nextDouble(), rand.nextDouble()});
        ret.setStringArray(new String[]{"Andy", "is", "absolutely", "rad"});
        ret.setEnumValue(RepMessage.TestEnum.Test);

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
        ret.getEcefList().add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefList().add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefList().add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));

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

        ret.getEcef2DList().add(new ArrayList<ECEFPos>());
        ret.getEcef2DList().get(0).add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcef2DList().get(0).add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcef2DList().add(new ArrayList<ECEFPos>());
        ret.getEcef2DList().get(1).add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcef2DList().get(1).add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcef2DList().add(new ArrayList<ECEFPos>());
        ret.getEcef2DList().get(2).add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcef2DList().get(2).add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));

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

        ret.getEcefMap().put(Integer.toString(rand.nextInt()), new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefMap().put(Integer.toString(rand.nextInt()), new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefMap().put(Integer.toString(rand.nextInt()), new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));

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
        l.add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        l.add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        l.add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefListMap().put(Integer.toString(rand.nextInt()), l);
        l = new ArrayList();
        l.add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        l.add(new ECEFPos(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        ret.getEcefListMap().put(Integer.toString(rand.nextInt()), l);

        return ret;
    }
}
