/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core.codec;

import com.a2i.sim.core.Message;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.msgpack.MessagePack;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

/**
 *
 * @author atrimble
 */
@Message
public class TestMessage {
    private boolean booleanValue;
    private byte byteValue;
    private short shortValue;
    private int intValue;
    private float floatValue;
    private long longValue;
    private double doubleValue;
    private String stringValue;
    private ECEF ecefValue;

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

//    private final List<List<Boolean>> boolean2DList = new ArrayList<>();
//    private final List<List<Byte>> byte2DList = new ArrayList<>();
//    private final List<List<Short>> short2DList = new ArrayList<>();
//    private final List<List<Integer>> int2DList = new ArrayList<>();
//    private final List<List<Float>> float2DList = new ArrayList<>();
//    private final List<List<Long>> long2DList = new ArrayList<>();
//    private final List<List<Double>> double2DList = new ArrayList<>();
//    private final List<List<String>> string2DList = new ArrayList<>();
//    private final List<List<ECEF>> ecef2DList = new ArrayList<>();
//
//    private final Map<String, Boolean> booleanMap = new HashMap<>();
//    private final Map<String, Byte> byteMap = new HashMap<>();
//    private final Map<String, Short> shortMap = new HashMap<>();
//    private final Map<String, Integer> intMap = new HashMap<>();
//    private final Map<String, Float> floatMap = new HashMap<>();
//    private final Map<String, Long> longMap = new HashMap<>();
//    private final Map<String, Double> doubleMap = new HashMap<>();
//    private final Map<String, String> stringMap = new HashMap<>();
//    private final Map<String, ECEF> ecefMap = new HashMap<>();
//
//    private final Map<String, List<Boolean>> booleanListMap = new HashMap<>();
//    private final Map<String, List<Byte>> byteListMap = new HashMap<>();
//    private final Map<String, List<Short>> shortListMap = new HashMap<>();
//    private final Map<String, List<Integer>> intListMap = new HashMap<>();
//    private final Map<String, List<Float>> floatListMap = new HashMap<>();
//    private final Map<String, List<Long>> longListMap = new HashMap<>();
//    private final Map<String, List<Double>> doubleListMap = new HashMap<>();
//    private final Map<String, List<String>> stringListMap = new HashMap<>();
//    private final Map<String, List<ECEF>> ecefListMap = new HashMap<>();

    public TestMessage() {
        
    }

//    public void _decode_(byte[] bytes) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//        MessagePack msgPack = new MessagePack();
//        Unpacker unpacker = msgPack.createBufferUnpacker(bytes);
//        _decode_(unpacker);
//    }

//    public void _decode_(Unpacker unpacker) throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//        
//        booleanValue = unpacker.read(Boolean.class);
//        charValue = unpacker.read(Character.class);
//        byteValue = unpacker.read(Byte.class);
//        shortValue = unpacker.read(Short.class);
//        intValue = unpacker.read(Integer.class);
//        floatValue = unpacker.read(Float.class);
//        longValue = unpacker.read(Long.class);
//        doubleValue = unpacker.read(Double.class);
//        stringValue = unpacker.read(stringValue.getClass());
//        ecefValue = new ECEF();
//        ecefValue.getClass().getMethod("_decode_", unpacker.getClass()).invoke(ecefValue, unpacker);
//
//        booleanArray = unpacker.read(booleanArray.getClass());
//        charArray = unpacker.read(charArray.getClass());
//        byteArray = unpacker.read(byteArray.getClass());
//        shortArray = unpacker.read(shortArray.getClass());
//        intArray = unpacker.read(intArray.getClass());
//        floatArray = unpacker.read(floatArray.getClass());
//        longArray = unpacker.read(longArray.getClass());
//        doubleArray = unpacker.read(doubleArray.getClass());
//        stringArray = unpacker.read(stringArray.getClass());
//
//        List<byte[]> arrList = unpacker.read(Templates.tList(Templates.TByteArray));
//        ecefArray = new ECEF[arrList.size()];
//        for(int i = 0; i < arrList.size(); ++i) {
//            byte[] tmp = arrList.get(i);
//            ecefArray[i] = new ECEF();
//            ecefArray[i].getClass().getMethod("_decode_", byte[].class).invoke(ecefArray[i], tmp);
//        }
//    
//        booleanList.addAll(unpacker.read(Templates.tList(Templates.TBoolean)));
//        charList.addAll(unpacker.read(Templates.tList(Templates.TCharacter)));
//        byteList.addAll(unpacker.read(Templates.tList(Templates.TByte)));
//        shortList.addAll(unpacker.read(Templates.tList(Templates.TShort)));
//        intList.addAll(unpacker.read(Templates.tList(Templates.TInteger)));
//        floatList.addAll(unpacker.read(Templates.tList(Templates.TFloat)));
//        longList.addAll(unpacker.read(Templates.tList(Templates.TLong)));
//        doubleList.addAll(unpacker.read(Templates.tList(Templates.TDouble)));
//        stringList.addAll(unpacker.read(Templates.tList(Templates.TString)));
//
//        List<byte[]> tmpList = unpacker.read(Templates.tList(Templates.TByteArray));
//        for(int i = 0; i < tmpList.size(); ++i) {
//            byte[] tmp = tmpList.get(i);
//            ECEF ecef = new ECEF();
//            ecef.getClass().getMethod("_decode_", byte[].class).invoke(ecef, tmp);
//            ecefList.add(ecef);
//        }
//
//        packer.write(booleanMap);
//        packer.write(charMap);
//        packer.write(byteMap);
//        packer.write(shortMap);
//        packer.write(intMap);
//        packer.write(floatMap);
//        packer.write(longMap);
//        packer.write(doubleMap);
//        packer.write(stringMap);
//        packer.write(ecefMap);
//
//    }

    public byte[] _encode() throws IOException {
        MessagePack msgPack = new MessagePack();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        Packer packer = msgPack.createPacker(out);

//        packer.write(booleanValue);
//        packer.write(byteValue);
//        packer.write(shortValue);
//        packer.write(intValue);
//        packer.write(floatValue);
//        packer.write(longValue);
//        packer.write(doubleValue);
//        packer.write(stringValue);
//        byte[] arr = ecefValue.test();
//        packer.write(arr);
//
//        packer.write(booleanArray);
//        packer.write(byteArray);
//        packer.write(shortArray);
//        packer.write(intArray);
//        packer.write(floatArray);
//        packer.write(longArray);
//        packer.write(doubleArray);
//        packer.write(stringArray);
//    
//        packer.write(booleanList);
//        packer.write(byteList);
//        packer.write(shortList);
//        packer.write(intList);
//        packer.write(floatList);
//        packer.write(longList);
//        packer.write(doubleList);
//        packer.write(stringList);
        
        List newList = new ArrayList();
        encodeList(newList, ecefList, ECEF.class);
        packer.write(newList);

//        packer.write(boolean2DList);
//        packer.write(char2DList);
//        packer.write(byte2DList);
//        packer.write(short2DList);
//        packer.write(int2DList);
//        packer.write(float2DList);
//        packer.write(long2DList);
//        packer.write(double2DList);
//        packer.write(string2DList);
//        packer.write(ecef2DList);
//
//        packer.write(booleanMap);
//        packer.write(charMap);
//        packer.write(byteMap);
//        packer.write(shortMap);
//        packer.write(intMap);
//        packer.write(floatMap);
//        packer.write(longMap);
//        packer.write(doubleMap);
//        packer.write(stringMap);
//        packer.write(ecefMap);
//
//        packer.write(booleanListMap);
//        packer.write(charListMap);
//        packer.write(byteListMap);
//        packer.write(shortListMap);
//        packer.write(intListMap);
//        packer.write(floatListMap);
//        packer.write(longListMap);
//        packer.write(doubleListMap);
//        packer.write(stringListMap);
//        packer.write(ecefListMap);
//
//        out.write((short)msgBuffer.size() >>> 8);
//        out.write((short)msgBuffer.size());
//        msgBuffer.writeTo(out);
//
        return out.toByteArray();
    }

    private <T extends ECEF> void encodeList(List out, List<T> list, Class<T> type) {
        for(T t : list) {
            if(t instanceof List) {
                List newOut = new ArrayList();
                out.add(newOut);
                encodeList(newOut, (List)t, type);
            } else {
//                if(t instanceof Boolean) {
//                    out.add(t);
//                } else if(t instanceof Byte) {
//                    out.add(t);
//                } else if(t instanceof Short) {
//                    out.add(t);
//                } else if(t instanceof Integer) {
//                    out.add(t);
//                } else if(t instanceof Float) {
//                    out.add(t);
//                } else if(t instanceof Long) {
//                    out.add(t );
//                } else if(t instanceof Double) {
//                    out.add(t);
//                } else if(t instanceof String) {
//                    out.add(t);
//                } else {
                    out.add(t.getX());
//                }
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

//    /**
//     * @return the booleanMap
//     */
//    public Map<String, Boolean> getBooleanMap() {
//        return booleanMap;
//    }
//
//    /**
//     * @return the charMap
//     */
//    public Map<String, Character> getCharMap() {
//        return charMap;
//    }
//
//    /**
//     * @return the byteMap
//     */
//    public Map<String, Byte> getByteMap() {
//        return byteMap;
//    }
//
//    /**
//     * @return the shortMap
//     */
//    public Map<String, Short> getShortMap() {
//        return shortMap;
//    }
//
//    /**
//     * @return the intMap
//     */
//    public Map<String, Integer> getIntMap() {
//        return intMap;
//    }
//
//    /**
//     * @return the floatMap
//     */
//    public Map<String, Float> getFloatMap() {
//        return floatMap;
//    }
//
//    /**
//     * @return the longMap
//     */
//    public Map<String, Long> getLongMap() {
//        return longMap;
//    }
//
//    /**
//     * @return the doubleMap
//     */
//    public Map<String, Double> getDoubleMap() {
//        return doubleMap;
//    }
//
//    /**
//     * @return the stringMap
//     */
//    public Map<String, String> getStringMap() {
//        return stringMap;
//    }
//
//    /**
//     * @return the ecefMap
//     */
//    public Map<String, ECEF> getEcefMap() {
//        return ecefMap;
//    }
//
//    /**
//     * @return the boolean2DList
//     */
//    public List<List<Boolean>> getBoolean2DList() {
//        return boolean2DList;
//    }
//
//    /**
//     * @return the char2DList
//     */
//    public List<List<Character>> getChar2DList() {
//        return char2DList;
//    }
//
//    /**
//     * @return the byte2DList
//     */
//    public List<List<Byte>> getByte2DList() {
//        return byte2DList;
//    }
//
//    /**
//     * @return the short2DList
//     */
//    public List<List<Short>> getShort2DList() {
//        return short2DList;
//    }
//
//    /**
//     * @return the int2DList
//     */
//    public List<List<Integer>> getInt2DList() {
//        return int2DList;
//    }
//
//    /**
//     * @return the float2DList
//     */
//    public List<List<Float>> getFloat2DList() {
//        return float2DList;
//    }
//
//    /**
//     * @return the long2DList
//     */
//    public List<List<Long>> getLong2DList() {
//        return long2DList;
//    }
//
//    /**
//     * @return the double2DList
//     */
//    public List<List<Double>> getDouble2DList() {
//        return double2DList;
//    }
//
//    /**
//     * @return the string2DList
//     */
//    public List<List<String>> getString2DList() {
//        return string2DList;
//    }
//
//    /**
//     * @return the ecef2DList
//     */
//    public List<List<ECEF>> getEcef2DList() {
//        return ecef2DList;
//    }
//
//    /**
//     * @return the booleanListMap
//     */
//    public Map<String, List<Boolean>> getBooleanListMap() {
//        return booleanListMap;
//    }
//
//    /**
//     * @return the charListMap
//     */
//    public Map<String, List<Character>> getCharListMap() {
//        return charListMap;
//    }
//
//    /**
//     * @return the byteListMap
//     */
//    public Map<String, List<Byte>> getByteListMap() {
//        return byteListMap;
//    }
//
//    /**
//     * @return the shortListMap
//     */
//    public Map<String, List<Short>> getShortListMap() {
//        return shortListMap;
//    }
//
//    /**
//     * @return the intListMap
//     */
//    public Map<String, List<Integer>> getIntListMap() {
//        return intListMap;
//    }
//
//    /**
//     * @return the floatListMap
//     */
//    public Map<String, List<Float>> getFloatListMap() {
//        return floatListMap;
//    }
//
//    /**
//     * @return the longListMap
//     */
//    public Map<String, List<Long>> getLongListMap() {
//        return longListMap;
//    }
//
//    /**
//     * @return the doubleListMap
//     */
//    public Map<String, List<Double>> getDoubleListMap() {
//        return doubleListMap;
//    }
//
//    /**
//     * @return the stringListMap
//     */
//    public Map<String, List<String>> getStringListMap() {
//        return stringListMap;
//    }
//
//    /**
//     * @return the ecefListMap
//     */
//    public Map<String, List<ECEF>> getEcefListMap() {
//        return ecefListMap;
//    }
}
