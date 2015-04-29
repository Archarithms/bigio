/*
 * Copyright (c) 2015, Archarithms Inc.
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

package io.bigio.agent;

import java.util.Map;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a bytecode injector for BigIO messages. It injects the code
 * necessary to serialize and deserialize BigIO messages.
 * 
 * @author Andrew Trimble
 */
public class JATransformer {

    private static final Logger LOG = LoggerFactory.getLogger(JATransformer.class);

    private JATransformer() {

    }

    /**
     * Perform the transformation.
     * 
     * @param clazz the class.
     * @param signatures the set of field signatures.
     * @param pool the class pool.
     */
    public static final void transform(CtClass clazz, Map<String, String> signatures, ClassPool pool) {

        CtField[] fields = clazz.getDeclaredFields();

        try {
            clazz.addInterface(pool.getCtClass("io.bigio.core.codec.BigIOMessage"));
        } catch (NotFoundException ex) {
            LOG.error("Could not find interface BigIOMessage", ex);
        }

        CtField msgPackField;
        CtMethod decodeValueMethod;
        CtMethod decodeUnpackerMethod;
        CtMethod decodeBytesMethod;
        CtMethod encodeMethod;
        CtMethod encodeListMethod;
        CtMethod encodeBooleanArrayMethod;
        CtMethod encodeByteArrayMethod;
        CtMethod encodeShortArrayMethod;
        CtMethod encodeIntArrayMethod;
        CtMethod encodeFloatArrayMethod;
        CtMethod encodeLongArrayMethod;
        CtMethod encodeDoubleArrayMethod;
        CtMethod encodeStringArrayMethod;
        CtMethod encodeMapMethod;

        StringBuilder decodeBuff = new StringBuilder();
        decodeBuff.append("public void bigiodecode(byte[] bytes) {");
        decodeBuff.append("org.msgpack.core.MessageUnpacker unpacker = msgPack.newUnpacker(bytes);");
        if(fields.length > 0) {
            decodeBuff.append("try {");
        }

        StringBuilder encodeBuff = new StringBuilder();
        encodeBuff.append("public byte[] bigioencode() throws Exception {");
        encodeBuff.append("java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();");
        encodeBuff.append("org.msgpack.core.MessagePacker packer = msgPack.newPacker(out);");

        for (CtField field : fields) {
            try {
                if (field.getType().subclassOf(pool.get("java.util.List"))) {
                    String type = getType(signatures.get(field.getName()));

                    encodeBuff.append("encodeList(packer, ").append(field.getName()).append(", ").append(type).append(".class);");
                    
                    switch (type) {
                        case "java.lang.Boolean":
                        case "java.lang.Byte":
                        case "java.lang.Short":
                        case "java.lang.Integer":
                        case "java.lang.Float":
                        case "java.lang.Long":
                        case "java.lang.Double":
                        case "java.lang.String":
                            decodeBuff.append(field.getName()).append(".clear();");
                            decodeBuff.append(field.getName()).append(".addAll((java.util.List)bigiodecode(unpacker, ").append(type).append(".class));");
                            break;
                        default:
                            decodeBuff.append(field.getName()).append(".clear();");
                            decodeBuff.append(field.getName()).append(".addAll((java.util.List)bigiodecode(unpacker, ").append(type).append(".class));");
                            break;
                    }
                } else if (field.getType().subclassOf(pool.get("java.util.Map"))) {
                    String type = getType(signatures.get(field.getName()));
                    
                    encodeBuff.append("encodeMap(packer, ").append(field.getName()).append(", ").append(type).append(".class);");

                    switch (type) {
                        case "java.lang.Boolean":
                        case "java.lang.Byte":
                        case "java.lang.Short":
                        case "java.lang.Integer":
                        case "java.lang.Float":
                        case "java.lang.Long":
                        case "java.lang.Double":
                        case "java.lang.String":
                            decodeBuff.append(field.getName()).append(".clear();");
                            decodeBuff.append(field.getName()).append(".putAll((java.util.Map)bigiodecode(unpacker, ").append(type).append(".class));");
                            break;
                        default:
                            decodeBuff.append(field.getName()).append(".clear();");
                            decodeBuff.append(field.getName()).append(".putAll((java.util.Map)bigiodecode(unpacker, ").append(type).append(".class));");
                            break;
                    }
                } else if (field.getType().subclassOf(pool.get("java.lang.Enum"))) {
                    encodeBuff.append("packer.packInt(").append(field.getName()).append(".ordinal());");
                    decodeBuff.append(field.getName()).append("=").append("(").append(field.getType().getName()).append(")bigiodecode(unpacker, ").append(field.getType().getName()).append(".class);");
                } else if (field.getType().hasAnnotation(Class.forName("io.bigio.Message"))) {
                    encodeBuff.append("byte[] arr = (byte[])((io.bigio.core.codec.BigIOMessage)").append(field.getName()).append(").bigioencode();");
                    encodeBuff.append("encodeByteArray(packer, arr);");
                    decodeBuff.append(field.getName()).append("=").append("(").append(field.getType().getName()).append(")bigiodecode(unpacker, ").append(field.getType().getName()).append(".class);");
                } else {
                    switch (field.getType().getName()) {
                        case "java.lang.Boolean":
                            encodeBuff.append("packer.packBoolean(").append(field.getName()).append(".booleanValue());");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker, Boolean.class);");
                            break;
                        case "boolean":
                            encodeBuff.append("packer.packBoolean(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Boolean)bigiodecode(unpacker, Boolean.class)).booleanValue();");
                            break;
                        case "java.lang.Byte":
                            encodeBuff.append("packer.packByte(").append(field.getName()).append(".byteValue());");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker, Byte.class);");
                            break;
                        case "byte":
                            encodeBuff.append("packer.packByte(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Byte)bigiodecode(unpacker, Byte.class)).byteValue();");
                            break;
                        case "java.lang.Short":
                            encodeBuff.append("packer.packShort(").append(field.getName()).append(".shortValue());");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker, Short.class);");
                            break;
                        case "short":
                            encodeBuff.append("packer.packShort(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Short)bigiodecode(unpacker, Short.class)).shortValue();");
                            break;
                        case "java.lang.Integer":
                            encodeBuff.append("packer.packInt(").append(field.getName()).append(".intValue());");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker, Integer.class);");
                            break;
                        case "int":
                            encodeBuff.append("packer.packInt(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Integer)bigiodecode(unpacker, Integer.class)).intValue();");
                            break;
                        case "java.lang.Float":
                            encodeBuff.append("packer.packFloat(").append(field.getName()).append(".floatValue());");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker, Float.class);");
                            break;
                        case "float":
                            encodeBuff.append("packer.packFloat(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Float)bigiodecode(unpacker, Float.class)).floatValue();");
                            break;
                        case "java.lang.Long":
                            encodeBuff.append("packer.packLong(").append(field.getName()).append(".longValue());");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker, Long.class);");
                            break;
                        case "long":
                            encodeBuff.append("packer.packLong(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Long)bigiodecode(unpacker, Long.class)).longValue();");
                            break;
                        case "java.lang.Double":
                            encodeBuff.append("packer.packDouble(").append(field.getName()).append(".doubleValue());");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker, Double.class);");
                            break;
                        case "double":
                            encodeBuff.append("packer.packDouble(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Double)bigiodecode(unpacker, Double.class)).doubleValue();");
                            break;
                        case "java.lang.String":
                            encodeBuff.append("packer.packString(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(String)bigiodecode(unpacker, String.class);");
                            break;
                        case "boolean[]":
                            encodeBuff.append("encodeBooleanArray(packer, ").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(boolean[])bigiodecode(unpacker, boolean[].class);");
                            break;
                        case "byte[]":
                            encodeBuff.append("encodeByteArray(packer, ").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(byte[])bigiodecode(unpacker, byte[].class);");
                            break;
                        case "short[]":
                            encodeBuff.append("encodeShortArray(packer, ").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(short[])bigiodecode(unpacker, short[].class);");
                            break;
                        case "int[]":
                            encodeBuff.append("encodeIntArray(packer, ").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(int[])bigiodecode(unpacker, int[].class);");
                            break;
                        case "float[]":
                            encodeBuff.append("encodeFloatArray(packer, ").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(float[])bigiodecode(unpacker, float[].class);");
                            break;
                        case "long[]":
                            encodeBuff.append("encodeLongArray(packer, ").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(long[])bigiodecode(unpacker, long[].class);");
                            break;
                        case "double[]":
                            encodeBuff.append("encodeDoubleArray(packer, ").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(double[])bigiodecode(unpacker, double[].class);");
                            break;
                        case "java.lang.String[]":
                            encodeBuff.append("encodeStringArray(packer, ").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(String[])bigiodecode(unpacker, String[].class);");
                            break;
                    }
                }
            } catch (NotFoundException ex) {
                LOG.error("Field type not found.", ex);
            } catch (ClassNotFoundException ex) {
                LOG.error("Cannot find message annotation class.", ex);
            }
        }

        if(fields.length > 0) {
            decodeBuff.append("} catch (Exception ex) { ex.printStackTrace(); }");
        }
        decodeBuff.append("}");
        encodeBuff.append("packer.close();");
        encodeBuff.append("return out.toByteArray();}");

        try {
            // Add the MessagePack field
            msgPackField = CtField.make("private static final transient org.msgpack.core.MessagePack msgPack = new org.msgpack.core.MessagePack();", clazz);
            clazz.addField(msgPackField);

            // Store initial modifiers before the trickery that follows
            int modifiers = clazz.getModifiers();

            // Since encodeList calls encodeMap, we need to add a placeholder
            // implementation so that it will compile. Add an abstract method
            // called encodeMap to be removed later. Note that this changes
            // the class modifiers, so we'll have to change them back later.
            CtMethod tmpMethod = CtNewMethod.abstractMethod(
                    CtClass.voidType, 
                    "encodeMap", 
                    new CtClass[] {
                        pool.get("org.msgpack.core.MessagePacker"), 
                        pool.get("java.util.Map"), 
                        pool.get("java.lang.Class")
                    }, 
                    new CtClass[] {
                        pool.get("java.lang.Exception")
                    }, 
                    clazz);
            clazz.addMethod(tmpMethod);

            // Add the encode primitive array methods.
            encodeBooleanArrayMethod = CtMethod.make(encodeBooleanArraySrc, clazz);
            clazz.addMethod(encodeBooleanArrayMethod);
            encodeByteArrayMethod = CtMethod.make(encodeByteArraySrc, clazz);
            clazz.addMethod(encodeByteArrayMethod);
            encodeShortArrayMethod = CtMethod.make(encodeShortArraySrc, clazz);
            clazz.addMethod(encodeShortArrayMethod);
            encodeIntArrayMethod = CtMethod.make(encodeIntArraySrc, clazz);
            clazz.addMethod(encodeIntArrayMethod);
            encodeFloatArrayMethod = CtMethod.make(encodeFloatArraySrc, clazz);
            clazz.addMethod(encodeFloatArrayMethod);
            encodeLongArrayMethod = CtMethod.make(encodeLongArraySrc, clazz);
            clazz.addMethod(encodeLongArrayMethod);
            encodeDoubleArrayMethod = CtMethod.make(encodeDoubleArraySrc, clazz);
            clazz.addMethod(encodeDoubleArrayMethod);
            encodeStringArrayMethod = CtMethod.make(encodeStringArraySrc, clazz);
            clazz.addMethod(encodeStringArrayMethod);

            // Add the encodeList method.
            encodeListMethod = CtMethod.make(encodeListSrc, clazz);
            clazz.addMethod(encodeListMethod);

            // Remove the temporary abstract encodeMap method and reset the 
            // modifiers.
            clazz.removeMethod(tmpMethod);
            clazz.setModifiers(modifiers);

            // Add the encodeMap method.
            encodeMapMethod = CtMethod.make(encodeMapSrc, clazz);
            clazz.addMethod(encodeMapMethod);

            // Add the decode method.
            decodeValueMethod = CtMethod.make(decodeValueSrc, clazz);
            clazz.addMethod(decodeValueMethod);

            // Add the decode method.
            decodeUnpackerMethod = CtMethod.make(decodeUnpackerSrc, clazz);
            clazz.addMethod(decodeUnpackerMethod);
        
            // Add the high level encode and decode methods.
            decodeBytesMethod = CtMethod.make(decodeBuff.toString(), clazz);
            encodeMethod = CtMethod.make(encodeBuff.toString(), clazz);

            clazz.addMethod(encodeMethod);
            clazz.addMethod(decodeBytesMethod);
        } catch (CannotCompileException ex) {
            LOG.error("Cannot compile decode method.", ex);
        } catch (NotFoundException ex) {
            LOG.error("Not Found.", ex);
        }
    }

    private static final String encodeListSrc = 
            "private void encodeList(org.msgpack.core.MessagePacker packer, java.util.List list, Class expectedType) { "
            + "    packer.packArrayHeader(list.size());"
            + "    for(int i = 0; i < list.size(); ++i) { "
            + "        Object t = list.get(i); "
            + "        if(t instanceof java.util.List) { "
            + "            encodeList(packer, (java.util.List)t, expectedType); "
            + "        } else if(t instanceof java.util.Map) { "
            + "            encodeMap(packer, (java.util.Map)t, expectedType); "
            + "        } else { "
            + "            if(t instanceof Boolean) { "
            + "                packer.packBoolean(((Boolean)t).booleanValue()); "
            + "            } else if(t instanceof Byte) { "
            + "                packer.packByte(((Byte)t).byteValue()); "
            + "            } else if(t instanceof Short) { "
            + "                packer.packShort(((Short)t).shortValue()); "
            + "            } else if(t instanceof Integer) { "
            + "                packer.packInt(((Integer)t).intValue()); "
            + "            } else if(t instanceof Float) { "
            + "                packer.packFloat(((Float)t).floatValue()); "
            + "            } else if(t instanceof Long) { "
            + "                packer.packLong(((Long)t).longValue()); "
            + "            } else if(t instanceof Double) { "
            + "                packer.packDouble(((Double)t).doubleValue()); "
            + "            } else if(t instanceof String) { "
            + "                packer.packString((String)t); "
            + "            } else if(t instanceof Enum) { "
            + "                packer.packInt(new Integer(((Enum)t).ordinal()).intValue()); "
            + "            } else { "
            + "                encodeByteArray(packer, (byte[])((io.bigio.core.codec.BigIOMessage)t).bigioencode());"
            + "            } "
            + "        } "
            + "    } "
            + "}";

    private static final String encodeBooleanArraySrc = 
            "private void encodeBooleanArray(org.msgpack.core.MessagePacker packer, boolean[] array) { "
            + "    packer.packArrayHeader(array.length);"
            + "    for(int i = 0; i < array.length; ++i) { "
            + "        boolean b = array[i]; "
            + "        packer.packBoolean(b);"
            + "    } "
            + "}";

    private static final String encodeByteArraySrc = 
            "private void encodeByteArray(org.msgpack.core.MessagePacker packer, byte[] array) { "
            + "    packer.packBinaryHeader(array.length);"
            + "    packer.writePayload(array);"
            + "}";

    private static final String encodeShortArraySrc = 
            "private void encodeShortArray(org.msgpack.core.MessagePacker packer, short[] array) { "
            + "    packer.packArrayHeader(array.length);"
            + "    for(int i = 0; i < array.length; ++i) { "
            + "        short s = array[i]; "
            + "        packer.packShort(s);"
            + "    } "
            + "}";

    private static final String encodeIntArraySrc = 
            "private void encodeIntArray(org.msgpack.core.MessagePacker packer, int[] array) { "
            + "    packer.packArrayHeader(array.length);"
            + "    for(int i = 0; i < array.length; ++i) { "
            + "        int s = array[i]; "
            + "        packer.packInt(s);"
            + "    } "
            + "}";

    private static final String encodeFloatArraySrc = 
            "private void encodeFloatArray(org.msgpack.core.MessagePacker packer, float[] array) { "
            + "    packer.packArrayHeader(array.length);"
            + "    for(int i = 0; i < array.length; ++i) { "
            + "        float f = array[i]; "
            + "        packer.packFloat(f);"
            + "    } "
            + "}";

    private static final String encodeLongArraySrc = 
            "private void encodeLongArray(org.msgpack.core.MessagePacker packer, long[] array) { "
            + "    packer.packArrayHeader(array.length);"
            + "    for(int i = 0; i < array.length; ++i) { "
            + "        long l = array[i]; "
            + "        packer.packLong(l);"
            + "    } "
            + "}";

    private static final String encodeDoubleArraySrc = 
            "private void encodeDoubleArray(org.msgpack.core.MessagePacker packer, double[] array) { "
            + "    packer.packArrayHeader(array.length);"
            + "    for(int i = 0; i < array.length; ++i) { "
            + "        double d = array[i]; "
            + "        packer.packDouble(d);"
            + "    } "
            + "}";

    private static final String encodeStringArraySrc = 
            "private void encodeStringArray(org.msgpack.core.MessagePacker packer, String[] array) { "
            + "    packer.packArrayHeader(array.length);"
            + "    for(int i = 0; i < array.length; ++i) { "
            + "        String s = array[i]; "
            + "        packer.packString(s);"
            + "    } "
            + "}";

    private static final String encodeMapSrc = 
            "private void encodeMap(org.msgpack.core.MessagePacker packer, java.util.Map map, Class expectedType) throws Exception { "
            + "    packer.packMapHeader(map.keySet().size());"
            + "    java.util.Iterator it = map.keySet().iterator(); "
            + "    while(it.hasNext()) { "
            + "        Object k = it.next(); "
            + "        Object v = map.get(k); "
            + "        packer.packString(k.toString());"
            + "        if(v instanceof java.util.List) { "
            + "            encodeList(packer, (java.util.List)v, expectedType); "
            + "        } else if(v instanceof java.util.Map) { "
            + "            encodeMap(packer, (java.util.Map)v, expectedType); "
            + "        } else { "
            + "            if(v instanceof Boolean) { "
            + "                packer.packBoolean(((Boolean)v).booleanValue());"
            + "            } else if(v instanceof Byte) { "
            + "                packer.packByte(((Byte)v).byteValue()); "
            + "            } else if(v instanceof Short) { "
            + "                packer.packShort(((Short)v).shortValue());"
            + "            } else if(v instanceof Integer) { "
            + "                packer.packInt(((Integer)v).intValue());"
            + "            } else if(v instanceof Float) { "
            + "                packer.packFloat(((Float)v).floatValue());"
            + "            } else if(v instanceof Long) { "
            + "                packer.packLong(((Long)v).longValue());"
            + "            } else if(v instanceof Double) { "
            + "                packer.packDouble(((Double)v).doubleValue());"
            + "            } else if(v instanceof Enum) { "
            + "                packer.packInt(new Integer(((Enum)v).ordinal()).intValue()); "
            + "            } else if(v instanceof String) { "
            + "                packer.packString((String)v); "
            + "            } else { "
            + "                encodeByteArray(packer, (byte[])((io.bigio.core.codec.BigIOMessage)v).bigioencode());"
            + "            } "
            + "        } "
            + "    } "
            + "}";

    private static final String decodeValueSrc = 
            "private Object bigiodecode(org.msgpack.value.Value value, org.msgpack.value.ValueType type, Class expectedType) throws Exception {"
            + "    Object ret = null;"
            + "    if(type == org.msgpack.value.ValueType.ARRAY) {"
            + "        org.msgpack.value.ArrayValue elements = value.asArrayValue();"
            + "        if(expectedType.isArray()) {"
            + "            if(expectedType.equals(boolean[].class)) {"
            + "                ret = new boolean[elements.size()];"
            + "                for(int i = 0; i < elements.size(); ++i) {"
            + "                    ((boolean[])ret)[i] = elements.get(i).asBoolean().toBoolean();"
            + "                }"
            + "            } else if(expectedType.equals(short[].class)) {"
            + "                ret = new short[elements.size()];"
            + "                for(int i = 0; i < elements.size(); ++i) {"
            + "                    ((short[])ret)[i] = elements.get(i).asInteger().toShort();"
            + "                }"
            + "            } else if(expectedType.equals(int[].class)) {"
            + "                ret = new int[elements.size()];"
            + "                for(int i = 0; i < elements.size(); ++i) {"
            + "                    ((int[])ret)[i] = elements.get(i).asInteger().toInt();"
            + "                }" 
            + "            } else if(expectedType.equals(float[].class)) {" 
            + "                ret = new float[elements.size()];" 
            + "                for(int i = 0; i < elements.size(); ++i) {" 
            + "                    ((float[])ret)[i] = elements.get(i).asFloat().toFloat();" 
            + "                }"
            + "            } else if(expectedType.equals(long[].class)) {" 
            + "                ret = new long[elements.size()];" 
            + "                for(int i = 0; i < elements.size(); ++i) {" 
            + "                    ((long[])ret)[i] = elements.get(i).asInteger().toLong();" 
            + "                }" 
            + "            } else if(expectedType.equals(double[].class)) {" 
            + "                ret = new double[elements.size()];" 
            + "                for(int i = 0; i < elements.size(); ++i) {" 
            + "                    ((double[])ret)[i] = elements.get(i).asFloat().toDouble();" 
            + "                }" 
            + "            } else if(expectedType.equals(String[].class)) {" 
            + "                ret = new String[elements.size()];" 
            + "                for(int i = 0; i < elements.size(); ++i) {" 
            + "                    ((String[])ret)[i] = elements.get(i).asString().toString();" 
            + "                }" 
            + "            }"
            + "        } else {"
            + "            ret = new java.util.ArrayList();"
            + "            for(int i = 0; i < elements.size(); ++i) {"
            + "                ((java.util.List)ret).add(bigiodecode(elements.get(i), elements.get(i).getValueType(), expectedType));"
            + "            }"
            + "        }"
            + "    } else if(type == org.msgpack.value.ValueType.BINARY) {"
            + "        ret = expectedType.newInstance();"
            + "        ((io.bigio.core.codec.BigIOMessage)ret).bigiodecode(value.asBinary().toByteArray());"
            + "    } else if(type == org.msgpack.value.ValueType.BOOLEAN) {"
            + "        ret = Boolean.valueOf(value.asBoolean().toBoolean());"
            + "    } else if(type == org.msgpack.value.ValueType.FLOAT) {"
            + "        if(expectedType == Float.class) {"
            + "            ret = Float.valueOf(value.asFloat().toFloat());"
            + "        } else if(expectedType == Double.class) {"
            + "            ret = Double.valueOf(value.asFloat().toDouble());"
            + "        }"
            + "    } else if(type == org.msgpack.value.ValueType.INTEGER) {"
            + "        if(expectedType == Integer.class) {"
            + "            ret = Integer.valueOf(value.asInteger().toInt());"
            + "        } else if(expectedType == Long.class) {"
            + "            ret = Long.valueOf(value.asInteger().toLong());"
            + "        } else if(expectedType == Byte.class) {"
            + "            ret = Byte.valueOf(value.asInteger().toByte());"
            + "        } else if(expectedType == Short.class) {"
            + "            ret = Short.valueOf(value.asInteger().toShort());"
            + "        } else if(expectedType.isEnum()) {"
            + "            ret = expectedType.getEnumConstants()[value.asInteger().toInt()];"
            + "        }"
            + "    } else if(type == org.msgpack.value.ValueType.MAP) {"
            + "        ret = new java.util.HashMap();"
            + "        java.util.Iterator iter = value.asMapValue().toMap().keySet().iterator();"
            + "        while(iter.hasNext()) {"
            + "            org.msgpack.value.Value k = (org.msgpack.value.Value)iter.next();"
            + "            org.msgpack.value.Value v = value.asMapValue().toMap().get(k);"
            + "            ((java.util.Map)ret).put(bigiodecode(k, k.getValueType(), String.class), bigiodecode(v, v.getValueType(), expectedType));"
            + "        }"
            + "    } else if(type == org.msgpack.value.ValueType.STRING) {"
            + "        ret = value.asString().toString();"
            + "    } else {"
            + "        throw new java.io.IOException(\"Cannot decode message\");"
            + "    }"
            + "    return ret;"
            + "}";

    private static final String decodeUnpackerSrc = 
            "private Object bigiodecode(org.msgpack.core.MessageUnpacker unpacker, Class expectedType) throws Exception {"
            + "    org.msgpack.value.holder.ValueHolder value = new org.msgpack.value.holder.ValueHolder();"
            + "    org.msgpack.core.MessageFormat format = unpacker.unpackValue(value);"
            + "    return bigiodecode(value.get().toValue(), format.getValueType(), expectedType);"
            + "}";

    /**
     * Get the base type of a field based on its signature.
     * 
     * @param signature a field signature.
     * @return the contained type.
     */
    private static String getType(String signature) {
        String[] arr = signature.split("<");
        String type = arr[arr.length - 1];
        arr = type.split(">");
        type = arr[0];

        String[] mapSplit = type.split(";");
        if (mapSplit.length > 1) {
            type = mapSplit[1] + ";";
        }

        return type.substring(1, type.length() - 1).replace("/", ".");
    }
}
