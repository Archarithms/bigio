/*
 * Copyright 2014 Archarithms Inc.
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
 *
 * @author atrimble
 */
public class JATransformer {

    private static final Logger LOG = LoggerFactory.getLogger(JATransformer.class);

    public static final void transform(CtClass clazz, Map<String, String> signatures, ClassPool pool) {

        CtField[] fields = clazz.getDeclaredFields();

        try {
            clazz.addInterface(pool.getCtClass("io.bigio.core.codec.BigIOMessage"));
        } catch (NotFoundException ex) {
            LOG.error("Could not find interface BigIOMessage", ex);
        }

        CtField msgPackField;
        CtMethod decodeValueMethod;
        CtMethod decodeBytesMethod;
        CtMethod encodeMethod;
        CtMethod encodeListMethod;
        CtMethod encodeMapMethod;

        StringBuilder decodeBuff = new StringBuilder();
        decodeBuff.append("public void bigiodecode(byte[] bytes) {");
        decodeBuff.append("org.msgpack.unpacker.Unpacker unpacker = msgPack.createBufferUnpacker(bytes);");
        decodeBuff.append("try {");

        StringBuilder encodeBuff = new StringBuilder();
        encodeBuff.append("public byte[] bigioencode() throws Exception {");
        encodeBuff.append("java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();");
        encodeBuff.append("org.msgpack.packer.Packer packer = msgPack.createPacker(out);");

        for (CtField field : fields) {
            try {
                if (field.getType().subclassOf(pool.get("java.util.List"))) {
                    String type = getType(signatures.get(field.getName()));
                    switch (type) {
                        case "java.lang.Boolean":
                        case "java.lang.Byte":
                        case "java.lang.Short":
                        case "java.lang.Integer":
                        case "java.lang.Float":
                        case "java.lang.Long":
                        case "java.lang.Double":
                        case "java.lang.String":
                            encodeBuff.append("try { packer.write(").append(field.getName()).append("); } catch(Exception ex) { ex.printStackTrace(); }");
                            decodeBuff.append(field.getName()).append(".clear();");
                            decodeBuff.append(field.getName()).append(".addAll((java.util.List)bigiodecode(unpacker.readValue(), ").append(type).append(".class));");
                            break;
                        default:
                            encodeBuff.append("java.util.List newList = new java.util.ArrayList();");
                            encodeBuff.append("encodeList(newList, ").append(field.getName()).append(", ").append(type).append(".class);");
                            encodeBuff.append("packer.write(newList);");
                            decodeBuff.append(field.getName()).append(".clear();");
                            decodeBuff.append(field.getName()).append(".addAll((java.util.List)bigiodecode(unpacker.readValue(), ").append(type).append(".class));");
                            break;
                    }
                } else if (field.getType().subclassOf(pool.get("java.util.Map"))) {
                    String type = getType(signatures.get(field.getName()));
                    switch (type) {
                        case "java.lang.Boolean":
                        case "java.lang.Byte":
                        case "java.lang.Short":
                        case "java.lang.Integer":
                        case "java.lang.Float":
                        case "java.lang.Long":
                        case "java.lang.Double":
                        case "java.lang.String":
                            encodeBuff.append("try { packer.write(").append(field.getName()).append("); } catch(Exception ex) { ex.printStackTrace(); }");
                            decodeBuff.append(field.getName()).append(".clear();");
                            decodeBuff.append(field.getName()).append(".putAll((java.util.Map)bigiodecode(unpacker.readValue(), ").append(type).append(".class));");
                            break;
                        default:
                            encodeBuff.append("java.util.Map newMap = new java.util.HashMap();");
                            encodeBuff.append("encodeMap(newMap, ").append(field.getName()).append(", ").append(type).append(".class);");
                            encodeBuff.append("packer.write(newMap);");
                            decodeBuff.append(field.getName()).append(".clear();");
                            decodeBuff.append(field.getName()).append(".putAll((java.util.Map)bigiodecode(unpacker.readValue(), ").append(type).append(".class));");
                            break;
                    }
                } else if (field.getType().subclassOf(pool.get("java.lang.Enum"))) {
                    encodeBuff.append("packer.write(").append(field.getName()).append(".ordinal());");
                    decodeBuff.append(field.getName()).append("=").append("(").append(field.getType().getName()).append(")bigiodecode(unpacker.readValue(), ").append(field.getType().getName()).append(".class);");
                } else if (field.getType().hasAnnotation(Class.forName("io.bigio.Message"))) {
                    encodeBuff.append("byte[] arr = (byte[])((io.bigio.core.codec.BigIOMessage)").append(field.getName()).append(").bigioencode();");
                    encodeBuff.append("packer.write(arr);");
                    decodeBuff.append(field.getName()).append("=").append("(").append(field.getType().getName()).append(")bigiodecode(unpacker.readValue(), ").append(field.getType().getName()).append(".class);");
                } else {
                    switch (field.getType().getName()) {
                        case "java.lang.Boolean":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker.readValue(), Boolean.class);");
                            break;
                        case "boolean":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Boolean)bigiodecode(unpacker.readValue(), Boolean.class)).booleanValue();");
                            break;
                        case "java.lang.Byte":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker.readValue(), Byte.class);");
                            break;
                        case "byte":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Byte)bigiodecode(unpacker.readValue(), Byte.class)).byteValue();");
                            break;
                        case "java.lang.Short":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker.readValue(), Short.class);");
                            break;
                        case "short":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Short)bigiodecode(unpacker.readValue(), Short.class)).shortValue();");
                            break;
                        case "java.lang.Integer":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker.readValue(), Integer.class);");
                            break;
                        case "int":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Integer)bigiodecode(unpacker.readValue(), Integer.class)).intValue();");
                            break;
                        case "java.lang.Float":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker.readValue(), Float.class);");
                            break;
                        case "float":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Float)bigiodecode(unpacker.readValue(), Float.class)).floatValue();");
                            break;
                        case "java.lang.Long":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker.readValue(), Long.class);");
                            break;
                        case "long":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Long)bigiodecode(unpacker.readValue(), Long.class)).longValue();");
                            break;
                        case "java.lang.Double":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("bigiodecode(unpacker.readValue(), Double.class);");
                            break;
                        case "double":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("((Double)bigiodecode(unpacker.readValue(), Double.class)).doubleValue();");
                            break;
                        case "java.lang.String":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(String)bigiodecode(unpacker.readValue(), String.class);");
                            break;
                        case "boolean[]":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(boolean[])unpacker.read(boolean[].class);");
                            break;
                        case "byte[]":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(byte[])unpacker.read(byte[].class);");
                            break;
                        case "short[]":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(short[])unpacker.read(short[].class);");
                            break;
                        case "int[]":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(int[])unpacker.read(int[].class);");
                            break;
                        case "float[]":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(float[])unpacker.read(float[].class);");
                            break;
                        case "long[]":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(long[])unpacker.read(long[].class);");
                            break;
                        case "double[]":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(double[])unpacker.read(double[].class);");
                            break;
                        case "java.lang.String[]":
                            encodeBuff.append("packer.write(").append(field.getName()).append(");");
                            decodeBuff.append(field.getName()).append("=").append("(String[])unpacker.read(String[].class);");
                            break;
                    }
                }
            } catch (NotFoundException ex) {
                LOG.error("Field type not found.", ex);
            } catch (ClassNotFoundException ex) {
                LOG.error("Cannot find message annotation class.", ex);
            }
        }

        decodeBuff.append("} catch (Exception ex) { ex.printStackTrace(); }");
        decodeBuff.append("}");
        encodeBuff.append("return out.toByteArray();}");

        try {
            // Add the MessagePack field
            msgPackField = CtField.make("private static final transient org.msgpack.MessagePack msgPack = new org.msgpack.MessagePack();", clazz);
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
                        pool.get("java.util.Map"), 
                        pool.get("java.util.Map"), 
                        pool.get("java.lang.Class")
                    }, 
                    new CtClass[] {
                        pool.get("java.lang.Exception")
                    }, 
                    clazz);
            clazz.addMethod(tmpMethod);

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
            "private void encodeList(java.util.List out, java.util.List list, Class expectedType) { "
            + "    for(int i = 0; i < list.size(); ++i) { "
            + "        Object t = list.get(i); "
            + "        if(t instanceof java.util.List) { "
            + "            java.util.List newOut = new java.util.ArrayList(); "
            + "            out.add(newOut); "
            + "            encodeList(newOut, (java.util.List)t, expectedType); "
            + "        } else if(t instanceof java.util.Map) { "
            + "            java.util.Map newOut = new java.util.HashMap(); "
            + "            out.add(newOut); "
            + "            encodeMap(newOut, (java.util.Map)t, expectedType); "
            + "        } else { "
            + "            if(t instanceof Boolean) { "
            + "                out.add(t); "
            + "            } else if(t instanceof Byte) { "
            + "                out.add(t); "
            + "            } else if(t instanceof Short) { "
            + "                out.add(t); "
            + "            } else if(t instanceof Integer) { "
            + "                out.add(t); "
            + "            } else if(t instanceof Float) { "
            + "                out.add(t); "
            + "            } else if(t instanceof Long) { "
            + "                out.add(t); "
            + "            } else if(t instanceof Double) { "
            + "                out.add(t); "
            + "            } else if(t instanceof String) { "
            + "                out.add(t); "
            + "            } else if(t instanceof Enum) { "
            + "                out.add(new Integer(((Enum)t).ordinal())); "
            + "            } else { "
            + "                out.add((byte[])((io.bigio.core.codec.BigIOMessage)t).bigioencode());"
            + "            } "
            + "        } "
            + "    } "
            + "}";

    private static final String encodeMapSrc = 
            "private void encodeMap(java.util.Map out, java.util.Map map, Class expectedType) throws Exception { "
            + "    java.util.Iterator it = map.keySet().iterator(); "
            + "    while(it.hasNext()) { "
            + "        Object k = it.next(); "
            + "        Object v = map.get(k); "
            + "        if(v instanceof java.util.List) { "
            + "            java.util.List newOut = new java.util.ArrayList(); "
            + "            out.put(k, newOut); "
            + "            encodeList(newOut, (java.util.List)v, expectedType); "
            + "        } else if(v instanceof java.util.Map) { "
            + "            java.util.Map newOut = new java.util.HashMap(); "
            + "            out.put(k, newOut); "
            + "            encodeMap(newOut, (java.util.Map)v, expectedType); "
            + "        } else { "
            + "            if(v instanceof Boolean) { "
            + "                out.put(k, v); "
            + "            } else if(v instanceof Byte) { "
            + "                out.put(k, v); "
            + "            } else if(v instanceof Short) { "
            + "                out.put(k, v); "
            + "            } else if(v instanceof Integer) { "
            + "                out.put(k, v); "
            + "            } else if(v instanceof Float) { "
            + "                out.put(k, v); "
            + "            } else if(v instanceof Long) { "
            + "                out.put(k, v); "
            + "            } else if(v instanceof Double) { "
            + "                out.put(k, v); "
            + "            } else if(v instanceof Enum) { "
            + "                out.put(k, new Integer(((Enum)v).ordinal())); "
            + "            } else if(v instanceof String) { "
            + "                out.put(k, v); "
            + "            } else { "
            + "                out.put(k, (byte[])((io.bigio.core.codec.BigIOMessage)v).bigioencode());"
            + "            } "
            + "        } "
            + "    } "
            + "}";

    private static final String decodeValueSrc = 
            "private Object bigiodecode(org.msgpack.type.Value value, Class expectedType) throws Exception {"
            + "    Object ret = null;"
            + "    if(value.getType() == org.msgpack.type.ValueType.ARRAY) {"
            + "        ret = new java.util.ArrayList();"
            + "        org.msgpack.type.Value[] elements = value.asArrayValue().getElementArray();"
            + "        for(int i = 0; i < elements.length; ++i) {"
            + "            ((java.util.List)ret).add(bigiodecode(elements[i], expectedType));"
            + "        }"
            + "    } else if(value.getType() == org.msgpack.type.ValueType.BOOLEAN) {"
            + "        ret = Boolean.valueOf(value.asBooleanValue().getBoolean());"
            + "    } else if(value.getType() == org.msgpack.type.ValueType.FLOAT) {"
            + "        if(expectedType == Float.class) {"
            + "            ret = Float.valueOf(value.asFloatValue().getFloat());"
            + "        } else if(expectedType == Double.class) {"
            + "            ret = Double.valueOf(value.asFloatValue().getDouble());"
            + "        }"
            + "    } else if(value.getType() == org.msgpack.type.ValueType.INTEGER) {"
            + "        if(expectedType == Integer.class) {"
            + "            ret = Integer.valueOf(value.asIntegerValue().getInt());"
            + "        } else if(expectedType == Long.class) {"
            + "            ret = Long.valueOf(value.asIntegerValue().getLong());"
            + "        } else if(expectedType == Byte.class) {"
            + "            ret = Byte.valueOf(value.asIntegerValue().getByte());"
            + "        } else if(expectedType == Short.class) {"
            + "            ret = Short.valueOf(value.asIntegerValue().getShort());"
            + "        } else if(expectedType.isEnum()) {"
            + "            ret = expectedType.getEnumConstants()[value.asIntegerValue().getInt()];"
            + "        }"
            + "    } else if(value.getType() == org.msgpack.type.ValueType.MAP) {"
            + "        ret = new java.util.HashMap();"
            + "        java.util.Iterator iter = value.asMapValue().keySet().iterator();"
            + "        while(iter.hasNext()) {"
            + "            org.msgpack.type.Value k = (org.msgpack.type.Value)iter.next();"
            + "            org.msgpack.type.Value v = value.asMapValue().get(k);"
            + "            ((java.util.Map)ret).put(bigiodecode(k, String.class), bigiodecode(v, expectedType));"
            + "        }"
            + "    } else if(value.getType() == org.msgpack.type.ValueType.RAW) {"
            + "        if(expectedType == String.class) {"
            + "            ret = value.asRawValue().getString();"
            + "        } else if(expectedType.isEnum()) {"
            + "            ret = bigiodecode(msgPack.createBufferUnpacker(value.asRawValue().getByteArray()).readValue(), expectedType);"
            + "        } else if(expectedType == byte[].class || expectedType == Byte[].class) {"
            + "            ret = value.asRawValue().getByteArray();"
            + "        } else {"
            + "            ret = expectedType.newInstance();"
            + "            ((io.bigio.core.codec.BigIOMessage)ret).bigiodecode(value.asRawValue().getByteArray());"
            + "        }"
            + "    } else {"
            + "        throw new java.io.IOException(\"Cannot decode message\");"
            + "    }"
            + "    return ret;"
            + "}";

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
