/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.sim.test;

import com.a2i.sim.Message;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
@Message
public class TestMessage implements Serializable {

    public static enum TestEnum { Test, SecondTest }

    private static final transient Logger LOG = LoggerFactory.getLogger(TestMessage.class);

    private static final transient MessagePack msgPack = new MessagePack();

    private boolean booleanValue;
    private byte byteValue;
    private short shortValue;
    private int intValue;
    private float floatValue;
    private long longValue;
    private double doubleValue;
    private String stringValue;
    private ECEF ecefValue;
    private TestEnum enumValue;

    private boolean[] booleanArray;
    private byte[] byteArray;
    private short[] shortArray;
    private int[] intArray;
    private float[] floatArray;
    private long[] longArray;
    private double[] doubleArray;
    private String[] stringArray;

    private final List<Boolean> booleanList = new ArrayList<>();
    private final List<Byte> byteList = new ArrayList<>();
    private final List<Short> shortList = new ArrayList<>();
    private final List<Integer> intList = new ArrayList<>();
    private final List<Float> floatList = new ArrayList<>();
    private final List<Long> longList = new ArrayList<>();
    private final List<Double> doubleList = new ArrayList<>();
    private final List<String> stringList = new ArrayList<>();
    private final List<ECEF> ecefList = new ArrayList<>();

    private final List<List<Boolean>> boolean2DList = new ArrayList<>();
    private final List<List<Byte>> byte2DList = new ArrayList<>();
    private final List<List<Short>> short2DList = new ArrayList<>();
    private final List<List<Integer>> int2DList = new ArrayList<>();
    private final List<List<Float>> float2DList = new ArrayList<>();
    private final List<List<Long>> long2DList = new ArrayList<>();
    private final List<List<Double>> double2DList = new ArrayList<>();
    private final List<List<String>> string2DList = new ArrayList<>();
    private final List<List<ECEF>> ecef2DList = new ArrayList<>();

    private final Map<String, Boolean> booleanMap = new HashMap<>();
    private final Map<String, Byte> byteMap = new HashMap<>();
    private final Map<String, Short> shortMap = new HashMap<>();
    private final Map<String, Integer> intMap = new HashMap<>();
    private final Map<String, Float> floatMap = new HashMap<>();
    private final Map<String, Long> longMap = new HashMap<>();
    private final Map<String, Double> doubleMap = new HashMap<>();
    private final Map<String, String> stringMap = new HashMap<>();
    private final Map<String, ECEF> ecefMap = new HashMap<>();

    private final Map<String, List<Boolean>> booleanListMap = new HashMap<>();
    private final Map<String, List<Byte>> byteListMap = new HashMap<>();
    private final Map<String, List<Short>> shortListMap = new HashMap<>();
    private final Map<String, List<Integer>> intListMap = new HashMap<>();
    private final Map<String, List<Float>> floatListMap = new HashMap<>();
    private final Map<String, List<Long>> longListMap = new HashMap<>();
    private final Map<String, List<Double>> doubleListMap = new HashMap<>();
    private final Map<String, List<String>> stringListMap = new HashMap<>();
    private final Map<String, List<ECEF>> ecefListMap = new HashMap<>();

    public TestMessage() {
        
    }

    public Object decode(final Value value, final Class expectedType) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Object ret = null;

        System.out.println(value.getType());

        if(value.getType() == ValueType.ARRAY) {
                ret = new ArrayList();
                Value[] elements = value.asArrayValue().getElementArray();
                for(int i = 0; i < elements.length; ++i) {
                    ((List)ret).add(decode(elements[i], expectedType));
                }
        } else if(value.getType() == ValueType.BOOLEAN) {
                ret = value.asBooleanValue().getBoolean();
        } else if(value.getType() == ValueType.FLOAT) {
                if(expectedType == Float.class) {
                    ret = value.asFloatValue().getFloat();
                } else if(expectedType == Double.class) {
                    ret = value.asFloatValue().getDouble();
                }
        } else if(value.getType() == ValueType.INTEGER) {
                if(expectedType == Integer.class) {
                    ret = value.asIntegerValue().getInt();
                } else if(expectedType == Long.class) {
                    ret = value.asIntegerValue().getLong();
                } else if(expectedType == Byte.class) {
                    ret = value.asIntegerValue().getByte();
                } else if(expectedType == Short.class) {
                    ret = value.asIntegerValue().getShort();
                } else if(expectedType.isEnum()) {
                    ret = expectedType.getEnumConstants()[value.asIntegerValue().getInt()];
                }
        } else if(value.getType() == ValueType.MAP) {
                ret = new HashMap();
                Set<Value> keys = value.asMapValue().keySet();
                for(Value k : keys) {
                    Value v = value.asMapValue().get(k);
                    ((Map)ret).put(decode(k, String.class), decode(v, expectedType));
                }
        } else if(value.getType() == ValueType.RAW) {
                if(expectedType == String.class) {
                    ret = value.asRawValue().getString();
                } else if(expectedType.isEnum()) {
                    ret = decode(msgPack.createBufferUnpacker(value.asRawValue().getByteArray()).readValue(), expectedType);
                } else {
                    ret = expectedType.newInstance();
                    expectedType.getMethod("decode", byte[].class).invoke(ret, value.asRawValue().getByteArray());
                }
        } else {
                throw new IOException("Cannot decode message");
        }

        return ret;
    }

//    public Object decodeString(final Value value) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
//        Object ret = null;
//
//        switch(value.getType()) {
//            case ARRAY:
//                ret = new ArrayList();
//                Value[] elements = value.asArrayValue().getElementArray();
//                for(int i = 0; i < elements.length; ++i) {
//                    ((List)ret).add(decodeString(elements[i]));
//                }
//                break;
//            case MAP:
//                ret = new HashMap();
//                Set<Value> keys = value.asMapValue().keySet();
//                for(Value k : keys) {
//                    Value v = value.asMapValue().get(k);
//                    ((Map)ret).put(decodeString(k), decodeString(v));
//                }
//                break;
//            case RAW:
//                ret = value.asRawValue().getString();
//                break;
//            case NIL:
//            default:
//                throw new IOException("Cannot decode message");
//        }
//
//        return ret;
//    }
//    
//    public Object decodeBoolean(final Value value) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
//        Object ret = null;
//
//        switch(value.getType()) {
//            case ARRAY:
//                ret = new ArrayList();
//                Value[] elements = value.asArrayValue().getElementArray();
//                for(int i = 0; i < elements.length; ++i) {
//                    ((List)ret).add(decodeBoolean(elements[i]));
//                }
//                break;
//            case BOOLEAN:
//                ret = value.asBooleanValue().getBoolean();
//                break;
//            case MAP:
//                ret = new HashMap();
//                Set<Value> keys = value.asMapValue().keySet();
//                for(Value k : keys) {
//                    Value v = value.asMapValue().get(k);
//                    ((Map)ret).put(decodeString(k), decodeBoolean(v));
//                }
//                break;
//            case NIL:
//            default:
//                throw new IOException("Cannot decode message");
//        }
//
//        return ret;
//    }

    public void decode(byte[] bytes) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);
        
        booleanValue = (boolean)decode(unpacker.readValue(), Boolean.class);
        byteValue = (byte)decode(unpacker.readValue(), Byte.class);
        shortValue = (short)decode(unpacker.readValue(), Short.class);
        intValue = (int)decode(unpacker.readValue(), Integer.class);
        floatValue = (float)decode(unpacker.readValue(), Float.class);
        longValue = (long)decode(unpacker.readValue(), Long.class);
        doubleValue = (double)decode(unpacker.readValue(), Double.class);
        stringValue = (String)decode(unpacker.readValue(), String.class);
        enumValue = (TestEnum)decode(unpacker.readValue(), TestEnum.class);

        ecefValue = (ECEF)decode(unpacker.readValue(), ECEF.class);

        booleanArray = unpacker.read(boolean[].class);
        byteArray = unpacker.read(byte[].class);
        shortArray = unpacker.read(short[].class);
        intArray = unpacker.read(int[].class);
        floatArray = unpacker.read(float[].class);
        longArray = unpacker.read(long[].class);
        doubleArray = unpacker.read(double[].class);
        stringArray = unpacker.read(String[].class);

        booleanList.addAll((List)decode(unpacker.readValue(), Boolean.class));
        byteList.addAll((List)decode(unpacker.readValue(), Byte.class));
        shortList.addAll((List)decode(unpacker.readValue(), Short.class));
        intList.addAll((List)decode(unpacker.readValue(), Integer.class));
        floatList.addAll((List)decode(unpacker.readValue(), Float.class));
        longList.addAll((List)decode(unpacker.readValue(), Long.class));
        doubleList.addAll((List)decode(unpacker.readValue(), Double.class));
        stringList.addAll((List)decode(unpacker.readValue(), String.class));
        ecefList.addAll((List)decode(unpacker.readValue(), ECEF.class));

        boolean2DList.addAll((List)decode(unpacker.readValue(), Boolean.class));
        byte2DList.addAll((List)decode(unpacker.readValue(), Byte.class));
        short2DList.addAll((List)decode(unpacker.readValue(), Short.class));
        int2DList.addAll((List)decode(unpacker.readValue(), Integer.class));
        float2DList.addAll((List)decode(unpacker.readValue(), Float.class));
        long2DList.addAll((List)decode(unpacker.readValue(), Long.class));
        double2DList.addAll((List)decode(unpacker.readValue(), Double.class));
        string2DList.addAll((List)decode(unpacker.readValue(), String.class));
        ecef2DList.addAll((List)decode(unpacker.readValue(), ECEF.class));

        booleanMap.putAll((Map)decode(unpacker.readValue(), Boolean.class));
        byteMap.putAll((Map)decode(unpacker.readValue(), Byte.class));
        shortMap.putAll((Map)decode(unpacker.readValue(), Short.class));
        intMap.putAll((Map)decode(unpacker.readValue(), Integer.class));
        floatMap.putAll((Map)decode(unpacker.readValue(), Float.class));
        longMap.putAll((Map)decode(unpacker.readValue(), Long.class));
        doubleMap.putAll((Map)decode(unpacker.readValue(), Double.class));
        stringMap.putAll((Map)decode(unpacker.readValue(), String.class));
        ecefMap.putAll((Map)decode(unpacker.readValue(), ECEF.class));

        booleanListMap.putAll((Map)decode(unpacker.readValue(), Boolean.class));
        byteListMap.putAll((Map)decode(unpacker.readValue(), Byte.class));
        shortListMap.putAll((Map)decode(unpacker.readValue(), Short.class));
        intListMap.putAll((Map)decode(unpacker.readValue(), Integer.class));
        floatListMap.putAll((Map)decode(unpacker.readValue(), Float.class));
        longListMap.putAll((Map)decode(unpacker.readValue(), Long.class));
        doubleListMap.putAll((Map)decode(unpacker.readValue(), Double.class));
        stringListMap.putAll((Map)decode(unpacker.readValue(), String.class));
        ecefListMap.putAll((Map)decode(unpacker.readValue(), ECEF.class));
    }

    public byte[] encode() throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        Packer packer = msgPack.createPacker(out);

        packer.write(booleanValue);
        packer.write(byteValue);
        packer.write(shortValue);
        packer.write(intValue);
        packer.write(floatValue);
        packer.write(longValue);
        packer.write(doubleValue);
        packer.write(stringValue);
        packer.write(enumValue.ordinal());

        byte[] arr = (byte[])ecefValue.getClass().getMethod("encode").invoke(ecefValue);
        packer.write(arr);

        packer.write(booleanArray);
        packer.write(byteArray);
        packer.write(shortArray);
        packer.write(intArray);
        packer.write(floatArray);
        packer.write(longArray);
        packer.write(doubleArray);
        packer.write(stringArray);
    
        packer.write(booleanList);
        packer.write(byteList);
        packer.write(shortList);
        packer.write(intList);
        packer.write(floatList);
        packer.write(longList);
        packer.write(doubleList);
        packer.write(stringList);
        
        List newList = new ArrayList();
        encodeList(newList, ecefList, ECEF.class);
        packer.write(newList);

        packer.write(boolean2DList);
        packer.write(byte2DList);
        packer.write(short2DList);
        packer.write(int2DList);
        packer.write(float2DList);
        packer.write(long2DList);
        packer.write(double2DList);
        packer.write(string2DList);

        List anotherList = new ArrayList();
        encodeList(anotherList, ecef2DList, ECEF.class);
        packer.write(anotherList);

        packer.write(booleanMap);
        packer.write(byteMap);
        packer.write(shortMap);
        packer.write(intMap);
        packer.write(floatMap);
        packer.write(longMap);
        packer.write(doubleMap);
        packer.write(stringMap);

        Map map = new HashMap();
        encodeMap(map, ecefMap, ECEF.class);
        packer.write(map);

        packer.write(booleanListMap);
        packer.write(byteListMap);
        packer.write(shortListMap);
        packer.write(intListMap);
        packer.write(floatListMap);
        packer.write(longListMap);
        packer.write(doubleListMap);
        packer.write(stringListMap);

        Map anotherMap = new HashMap();
        encodeMap(anotherMap, ecefListMap, ECEF.class);
        packer.write(anotherMap);

        return out.toByteArray();
    }

    private void encodeList(List out, List list, Class expectedType) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        for(Object t : list) {
            if(t instanceof List) {
                List newOut = new ArrayList();
                out.add(newOut);
                encodeList(newOut, (List)t, expectedType);
            } else if(t instanceof Map) {
                Map newOut = new HashMap();
                out.add(newOut);
                encodeMap(newOut, (Map)t, expectedType);
            } else {
                if(t instanceof Boolean) {
                    out.add(t);
                } else if(t instanceof Byte) {
                    out.add(t);
                } else if(t instanceof Short) {
                    out.add(t);
                } else if(t instanceof Integer) {
                    out.add(t);
                } else if(t instanceof Float) {
                    out.add(t);
                } else if(t instanceof Long) {
                    out.add(t);
                } else if(t instanceof Double) {
                    out.add(t);
                } else if(t instanceof String) {
                    out.add(t);
                } else if(t instanceof Enum) {
                    out.add(((Enum)t).ordinal());
                } else {
                    out.add((byte[])expectedType.getMethod("encode").invoke(t));
                }
            }
        }
    }

    private void encodeMap(Map out, Map map, Class expectedType) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        for(Object k : map.keySet()) {

            Object v = map.get(k);

            if(v instanceof List) {
                List newOut = new ArrayList();
                out.put(k, newOut);
                encodeList(newOut, (List)v, expectedType);
            } else if(v instanceof Map) {
                Map newOut = new HashMap();
                out.put(k, newOut);
                encodeMap(newOut, (Map)v, expectedType);
            } else {
                if(v instanceof Boolean) {
                    out.put(k, v);
                } else if(v instanceof Byte) {
                    out.put(k, v);
                } else if(v instanceof Short) {
                    out.put(k, v);
                } else if(v instanceof Integer) {
                    out.put(k, v);
                } else if(v instanceof Float) {
                    out.put(k, v);
                } else if(v instanceof Long) {
                    out.put(k, v);
                } else if(v instanceof Double) {
                    out.put(k, v);
                } else if(v instanceof Enum) {
                    out.put(k, ((Enum)v).ordinal());
                } else if(v instanceof String) {
                    out.put(k, v);
                } else {
                    out.put(k, (byte[])expectedType.getMethod("encode").invoke(v));
                }
            }
        }
    }

    /**
     * @return the booleanValue
     */
    public boolean isBooleanValue() {
        return booleanValue;
    }

    /**
     * @param booleanValue the booleanValue to set
     */
    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    /**
     * @return the byteValue
     */
    public byte getByteValue() {
        return byteValue;
    }

    /**
     * @param byteValue the byteValue to set
     */
    public void setByteValue(byte byteValue) {
        this.byteValue = byteValue;
    }

    /**
     * @return the shortValue
     */
    public short getShortValue() {
        return shortValue;
    }

    /**
     * @param shortValue the shortValue to set
     */
    public void setShortValue(short shortValue) {
        this.shortValue = shortValue;
    }

    /**
     * @return the intValue
     */
    public int getIntValue() {
        return intValue;
    }

    /**
     * @param intValue the intValue to set
     */
    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    /**
     * @return the floatValue
     */
    public float getFloatValue() {
        return floatValue;
    }

    /**
     * @param floatValue the floatValue to set
     */
    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    /**
     * @return the longValue
     */
    public long getLongValue() {
        return longValue;
    }

    /**
     * @param longValue the longValue to set
     */
    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    /**
     * @return the doubleValue
     */
    public double getDoubleValue() {
        return doubleValue;
    }

    /**
     * @param doubleValue the doubleValue to set
     */
    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    /**
     * @return the stringValue
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * @param stringValue the stringValue to set
     */
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * @return the ecefValue
     */
    public ECEF getEcefValue() {
        return ecefValue;
    }

    /**
     * @param ecefValue the ecefValue to set
     */
    public void setEcefValue(ECEF ecefValue) {
        this.ecefValue = ecefValue;
    }

    /**
     * @return the booleanArray
     */
    public boolean[] getBooleanArray() {
        return booleanArray;
    }

    /**
     * @param booleanArray the booleanArray to set
     */
    public void setBooleanArray(boolean[] booleanArray) {
        this.booleanArray = booleanArray;
    }

    /**
     * @return the byteArray
     */
    public byte[] getByteArray() {
        return byteArray;
    }

    /**
     * @param byteArray the byteArray to set
     */
    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    /**
     * @return the shortArray
     */
    public short[] getShortArray() {
        return shortArray;
    }

    /**
     * @param shortArray the shortArray to set
     */
    public void setShortArray(short[] shortArray) {
        this.shortArray = shortArray;
    }

    /**
     * @return the intArray
     */
    public int[] getIntArray() {
        return intArray;
    }

    /**
     * @param intArray the intArray to set
     */
    public void setIntArray(int[] intArray) {
        this.intArray = intArray;
    }

    /**
     * @return the floatArray
     */
    public float[] getFloatArray() {
        return floatArray;
    }

    /**
     * @param floatArray the floatArray to set
     */
    public void setFloatArray(float[] floatArray) {
        this.floatArray = floatArray;
    }

    /**
     * @return the longArray
     */
    public long[] getLongArray() {
        return longArray;
    }

    /**
     * @param longArray the longArray to set
     */
    public void setLongArray(long[] longArray) {
        this.longArray = longArray;
    }

    /**
     * @return the doubleArray
     */
    public double[] getDoubleArray() {
        return doubleArray;
    }

    /**
     * @param doubleArray the doubleArray to set
     */
    public void setDoubleArray(double[] doubleArray) {
        this.doubleArray = doubleArray;
    }

    /**
     * @return the stringArray
     */
    public String[] getStringArray() {
        return stringArray;
    }

    /**
     * @param stringArray the stringArray to set
     */
    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }

    /**
     * @return the booleanList
     */
    public List<Boolean> getBooleanList() {
        return booleanList;
    }

    /**
     * @return the byteList
     */
    public List<Byte> getByteList() {
        return byteList;
    }

    /**
     * @return the shortList
     */
    public List<Short> getShortList() {
        return shortList;
    }

    /**
     * @return the intList
     */
    public List<Integer> getIntList() {
        return intList;
    }

    /**
     * @return the floatList
     */
    public List<Float> getFloatList() {
        return floatList;
    }

    /**
     * @return the longList
     */
    public List<Long> getLongList() {
        return longList;
    }

    /**
     * @return the doubleList
     */
    public List<Double> getDoubleList() {
        return doubleList;
    }

    /**
     * @return the stringList
     */
    public List<String> getStringList() {
        return stringList;
    }

    /**
     * @return the ecefList
     */
    public List<ECEF> getEcefList() {
        return ecefList;
    }

    /**
     * @return the booleanMap
     */
    public Map<String, Boolean> getBooleanMap() {
        return booleanMap;
    }

    /**
     * @return the byteMap
     */
    public Map<String, Byte> getByteMap() {
        return byteMap;
    }

    /**
     * @return the shortMap
     */
    public Map<String, Short> getShortMap() {
        return shortMap;
    }

    /**
     * @return the intMap
     */
    public Map<String, Integer> getIntMap() {
        return intMap;
    }

    /**
     * @return the floatMap
     */
    public Map<String, Float> getFloatMap() {
        return floatMap;
    }

    /**
     * @return the longMap
     */
    public Map<String, Long> getLongMap() {
        return longMap;
    }

    /**
     * @return the doubleMap
     */
    public Map<String, Double> getDoubleMap() {
        return doubleMap;
    }

    /**
     * @return the stringMap
     */
    public Map<String, String> getStringMap() {
        return stringMap;
    }

    /**
     * @return the ecefMap
     */
    public Map<String, ECEF> getEcefMap() {
        return ecefMap;
    }

    /**
     * @return the boolean2DList
     */
    public List<List<Boolean>> getBoolean2DList() {
        return boolean2DList;
    }

    /**
     * @return the byte2DList
     */
    public List<List<Byte>> getByte2DList() {
        return byte2DList;
    }

    /**
     * @return the short2DList
     */
    public List<List<Short>> getShort2DList() {
        return short2DList;
    }

    /**
     * @return the int2DList
     */
    public List<List<Integer>> getInt2DList() {
        return int2DList;
    }

    /**
     * @return the float2DList
     */
    public List<List<Float>> getFloat2DList() {
        return float2DList;
    }

    /**
     * @return the long2DList
     */
    public List<List<Long>> getLong2DList() {
        return long2DList;
    }

    /**
     * @return the double2DList
     */
    public List<List<Double>> getDouble2DList() {
        return double2DList;
    }

    /**
     * @return the string2DList
     */
    public List<List<String>> getString2DList() {
        return string2DList;
    }

    /**
     * @return the ecef2DList
     */
    public List<List<ECEF>> getEcef2DList() {
        return ecef2DList;
    }

    /**
     * @return the booleanListMap
     */
    public Map<String, List<Boolean>> getBooleanListMap() {
        return booleanListMap;
    }

    /**
     * @return the byteListMap
     */
    public Map<String, List<Byte>> getByteListMap() {
        return byteListMap;
    }

    /**
     * @return the shortListMap
     */
    public Map<String, List<Short>> getShortListMap() {
        return shortListMap;
    }

    /**
     * @return the intListMap
     */
    public Map<String, List<Integer>> getIntListMap() {
        return intListMap;
    }

    /**
     * @return the floatListMap
     */
    public Map<String, List<Float>> getFloatListMap() {
        return floatListMap;
    }

    /**
     * @return the longListMap
     */
    public Map<String, List<Long>> getLongListMap() {
        return longListMap;
    }

    /**
     * @return the doubleListMap
     */
    public Map<String, List<Double>> getDoubleListMap() {
        return doubleListMap;
    }

    /**
     * @return the stringListMap
     */
    public Map<String, List<String>> getStringListMap() {
        return stringListMap;
    }

    /**
     * @return the ecefListMap
     */
    public Map<String, List<ECEF>> getEcefListMap() {
        return ecefListMap;
    }

    /**
     * @return the enumValue
     */
    public TestEnum getEnumValue() {
        return enumValue;
    }

    /**
     * @param enumValue the enumValue to set
     */
    public void setEnumValue(TestEnum enumValue) {
        this.enumValue = enumValue;
    }
}
