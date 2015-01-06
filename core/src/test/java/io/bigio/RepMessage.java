/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.bigio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
@Message
public class RepMessage implements Serializable {

    public static enum TestEnum { Test, SecondTest }

    private static final transient Logger LOG = LoggerFactory.getLogger(RepMessage.class);

    private boolean booleanValue;
    private byte byteValue;
    private short shortValue;
    private int intValue;
    private float floatValue;
    private long longValue;
    private double doubleValue;
    private String stringValue;
    private ECEFPos ecefValue;
    private TestEnum enumValue;

    private boolean[] booleanArray;
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
    private final List<ECEFPos> ecefList = new ArrayList<>();

    private final List<List<Boolean>> boolean2DList = new ArrayList<>();
    private final List<List<Byte>> byte2DList = new ArrayList<>();
    private final List<List<Short>> short2DList = new ArrayList<>();
    private final List<List<Integer>> int2DList = new ArrayList<>();
    private final List<List<Float>> float2DList = new ArrayList<>();
    private final List<List<Long>> long2DList = new ArrayList<>();
    private final List<List<Double>> double2DList = new ArrayList<>();
    private final List<List<String>> string2DList = new ArrayList<>();
    private final List<List<ECEFPos>> ecef2DList = new ArrayList<>();

    private final Map<String, Boolean> booleanMap = new HashMap<>();
    private final Map<String, Byte> byteMap = new HashMap<>();
    private final Map<String, Short> shortMap = new HashMap<>();
    private final Map<String, Integer> intMap = new HashMap<>();
    private final Map<String, Float> floatMap = new HashMap<>();
    private final Map<String, Long> longMap = new HashMap<>();
    private final Map<String, Double> doubleMap = new HashMap<>();
    private final Map<String, String> stringMap = new HashMap<>();
    private final Map<String, ECEFPos> ecefMap = new HashMap<>();

    private final Map<String, List<Boolean>> booleanListMap = new HashMap<>();
    private final Map<String, List<Byte>> byteListMap = new HashMap<>();
    private final Map<String, List<Short>> shortListMap = new HashMap<>();
    private final Map<String, List<Integer>> intListMap = new HashMap<>();
    private final Map<String, List<Float>> floatListMap = new HashMap<>();
    private final Map<String, List<Long>> longListMap = new HashMap<>();
    private final Map<String, List<Double>> doubleListMap = new HashMap<>();
    private final Map<String, List<String>> stringListMap = new HashMap<>();
    private final Map<String, List<ECEFPos>> ecefListMap = new HashMap<>();

    public RepMessage() {
        
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
    public ECEFPos getEcefValue() {
        return ecefValue;
    }

    /**
     * @param ecefValue the ecefValue to set
     */
    public void setEcefValue(ECEFPos ecefValue) {
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
    public List<ECEFPos> getEcefList() {
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
    public Map<String, ECEFPos> getEcefMap() {
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
    public List<List<ECEFPos>> getEcef2DList() {
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
    public Map<String, List<ECEFPos>> getEcefListMap() {
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
