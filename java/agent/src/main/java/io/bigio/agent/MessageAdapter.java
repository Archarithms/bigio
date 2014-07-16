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

package io.bigio.agent;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A message adapter for injecting encoding/decoding code into messages.
 * 
 * @author Andy Trimble
 */
public class MessageAdapter extends ClassVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageAdapter.class);

    String currentClass;
    boolean isMessage = false;
    boolean isEnum = false;
    boolean msgPackDefined = false;
    boolean clinitFound = false;
    List<FieldInfo> fields = new ArrayList<>();

    public MessageAdapter(ClassVisitor cv) {
        super(ASM4, cv);
    }

    public boolean wasMessage() {
        return isMessage;
    }

    public boolean wasEnum() {
        return isEnum;
    }

    private class AddMsgPack extends MethodVisitor {

        public AddMsgPack(MethodVisitor mv) {
            super(ASM4, mv);
        }

        @Override
        public void visitCode() {
            mv.visitTypeInsn(NEW, "org/msgpack/MessagePack");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "org/msgpack/MessagePack", "<init>", "()V");
            mv.visitFieldInsn(PUTSTATIC, currentClass, "_MSG_PACK_", "Lorg/msgpack/MessagePack;");
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            mv.visitMaxs(maxStack + 1, maxLocals);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals("Lio/bigio/Message;")) {
            isMessage = true;
        }

        return cv.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(int version, String name, String desc, String signature, String[] interfaces) {

        MethodVisitor mv = cv.visitMethod(version, name, desc, signature, interfaces);

        if (name.equals("<clinit>") && (isMessage || isEnum)) {
            clinitFound = true;
            mv = new AddMsgPack(mv);
        }

        return mv;
    }

    @Override
    public void visit(int version, int access, String name,
            String signature, String superName, String[] interfaces) {

        currentClass = name;
        isMessage = false;
        msgPackDefined = false;

        if (superName.equals("java/lang/Enum")) {
            if (!name.startsWith("com/sun") && !name.startsWith("sun") 
                    && !name.startsWith("java") && !name.startsWith("org/spring") 
                    && !name.startsWith("org/junit")) {
                isEnum = true;
            }
        }

        cv.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (isMessage) {

            boolean isTransient = (access & ACC_TRANSIENT) != 0;

            if (!isTransient) {
                fields.add(new FieldInfo(name, desc, signature));
            }

            if (!msgPackDefined) {
                FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "_MSG_PACK_", "Lorg/msgpack/MessagePack;", null, null);
                fv.visitEnd();
                msgPackDefined = true;
            }
        } else if (isEnum) {
            if (!msgPackDefined) {
                FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "_MSG_PACK_", "Lorg/msgpack/MessagePack;", null, null);
                fv.visitEnd();
                msgPackDefined = true;
            }
        }

        return cv.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visitEnd() {

        if (isMessage && !clinitFound) {
            if (!msgPackDefined) {
                FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "_MSG_PACK_", "Lorg/msgpack/MessagePack;", null, null);
                fv.visitEnd();
                msgPackDefined = true;
            }

            MethodVisitor mv = cv.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            mv.visitCode();
            mv.visitTypeInsn(NEW, "org/msgpack/MessagePack");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "org/msgpack/MessagePack", "<init>", "()V");
            mv.visitFieldInsn(PUTSTATIC, currentClass, "_MSG_PACK_", "Lorg/msgpack/MessagePack;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 0);
            mv.visitEnd();
        }

        if (isEnum) {
            if (!msgPackDefined) {
                FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "_MSG_PACK_", "Lorg/msgpack/MessagePack;", null, null);
                fv.visitEnd();
                msgPackDefined = true;
            }

            MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "_encode_", "()[B", null, new String[]{"java/io/IOException"});
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/io/ByteArrayOutputStream");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayOutputStream", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 1);
            mv.visitFieldInsn(GETSTATIC, currentClass, "_MSG_PACK_", "Lorg/msgpack/MessagePack;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/MessagePack", "createPacker", "(Ljava/io/OutputStream;)Lorg/msgpack/packer/Packer;");
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "ordinal", "()I");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(I)Lorg/msgpack/packer/Packer;");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/ByteArrayOutputStream", "toByteArray", "()[B");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(2, 3);
            mv.visitEnd();
        }

        if (isMessage) {

            MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;", null, new String[]{"java/io/IOException", "java/lang/InstantiationException", "java/lang/IllegalAccessException", "java/lang/NoSuchMethodException", "java/lang/IllegalArgumentException", "java/lang/reflect/InvocationTargetException"});
            mv.visitCode();
            mv.visitInsn(ACONST_NULL);
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "getType", "()Lorg/msgpack/type/ValueType;");
            mv.visitFieldInsn(GETSTATIC, "org/msgpack/type/ValueType", "ARRAY", "Lorg/msgpack/type/ValueType;");
            Label l0 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l0);
            mv.visitTypeInsn(NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asArrayValue", "()Lorg/msgpack/type/ArrayValue;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/ArrayValue", "getElementArray", "()[Lorg/msgpack/type/Value;");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, 5);
            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_APPEND, 3, new Object[]{"java/lang/Object", "[Lorg/msgpack/type/Value;", Opcodes.INTEGER}, 0, null);
            mv.visitVarInsn(ILOAD, 5);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitInsn(ARRAYLENGTH);
            Label l2 = new Label();
            mv.visitJumpInsn(IF_ICMPGE, l2);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitTypeInsn(CHECKCAST, "java/util/List");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitVarInsn(ILOAD, 5);
            mv.visitInsn(AALOAD);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitIincInsn(5, 1);
            mv.visitJumpInsn(GOTO, l1);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_CHOP, 2, null, 0, null);
            Label l3 = new Label();
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "getType", "()Lorg/msgpack/type/ValueType;");
            mv.visitFieldInsn(GETSTATIC, "org/msgpack/type/ValueType", "BOOLEAN", "Lorg/msgpack/type/ValueType;");
            Label l4 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l4);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asBooleanValue", "()Lorg/msgpack/type/BooleanValue;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/BooleanValue", "getBoolean", "()Z");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l4);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "getType", "()Lorg/msgpack/type/ValueType;");
            mv.visitFieldInsn(GETSTATIC, "org/msgpack/type/ValueType", "FLOAT", "Lorg/msgpack/type/ValueType;");
            Label l5 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l5);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn(Type.getType("Ljava/lang/Float;"));
            Label l6 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l6);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asFloatValue", "()Lorg/msgpack/type/FloatValue;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/type/FloatValue", "getFloat", "()F");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l6);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn(Type.getType("Ljava/lang/Double;"));
            mv.visitJumpInsn(IF_ACMPNE, l3);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asFloatValue", "()Lorg/msgpack/type/FloatValue;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/type/FloatValue", "getDouble", "()D");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l5);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "getType", "()Lorg/msgpack/type/ValueType;");
            mv.visitFieldInsn(GETSTATIC, "org/msgpack/type/ValueType", "INTEGER", "Lorg/msgpack/type/ValueType;");
            Label l7 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l7);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn(Type.getType("Ljava/lang/Integer;"));
            Label l8 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l8);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asIntegerValue", "()Lorg/msgpack/type/IntegerValue;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/type/IntegerValue", "getInt", "()I");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l8);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn(Type.getType("Ljava/lang/Long;"));
            Label l9 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l9);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asIntegerValue", "()Lorg/msgpack/type/IntegerValue;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/type/IntegerValue", "getLong", "()J");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l9);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn(Type.getType("Ljava/lang/Byte;"));
            Label l10 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l10);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asIntegerValue", "()Lorg/msgpack/type/IntegerValue;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/type/IntegerValue", "getByte", "()B");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l10);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn(Type.getType("Ljava/lang/Short;"));
            Label l11 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l11);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asIntegerValue", "()Lorg/msgpack/type/IntegerValue;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/type/IntegerValue", "getShort", "()S");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l11);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isEnum", "()Z");
            mv.visitJumpInsn(IFEQ, l3);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getEnumConstants", "()[Ljava/lang/Object;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asIntegerValue", "()Lorg/msgpack/type/IntegerValue;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/type/IntegerValue", "getInt", "()I");
            mv.visitInsn(AALOAD);
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l7);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "getType", "()Lorg/msgpack/type/ValueType;");
            mv.visitFieldInsn(GETSTATIC, "org/msgpack/type/ValueType", "MAP", "Lorg/msgpack/type/ValueType;");
            Label l12 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l12);
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asMapValue", "()Lorg/msgpack/type/MapValue;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/MapValue", "keySet", "()Ljava/util/Set;");
            mv.visitVarInsn(ASTORE, 4);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;");
            mv.visitVarInsn(ASTORE, 5);
            Label l13 = new Label();
            mv.visitLabel(l13);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/util/Set", "java/util/Iterator"}, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
            Label l14 = new Label();
            mv.visitJumpInsn(IFEQ, l14);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "org/msgpack/type/Value");
            mv.visitVarInsn(ASTORE, 6);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asMapValue", "()Lorg/msgpack/type/MapValue;");
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/MapValue", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "org/msgpack/type/Value");
            mv.visitVarInsn(ASTORE, 7);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitTypeInsn(CHECKCAST, "java/util/Map");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l13);
            mv.visitLabel(l14);
            mv.visitFrame(Opcodes.F_CHOP, 2, null, 0, null);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l12);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "getType", "()Lorg/msgpack/type/ValueType;");
            mv.visitFieldInsn(GETSTATIC, "org/msgpack/type/ValueType", "RAW", "Lorg/msgpack/type/ValueType;");
            Label l15 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l15);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
            Label l16 = new Label();
            mv.visitJumpInsn(IF_ACMPNE, l16);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asRawValue", "()Lorg/msgpack/type/RawValue;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/RawValue", "getString", "()Ljava/lang/String;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l16);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isEnum", "()Z");
            Label l17 = new Label();
            mv.visitJumpInsn(IFEQ, l17);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETSTATIC, currentClass, "_MSG_PACK_", "Lorg/msgpack/MessagePack;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asRawValue", "()Lorg/msgpack/type/RawValue;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/RawValue", "getByteArray", "()[B");
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/MessagePack", "createBufferUnpacker", "([B)Lorg/msgpack/unpacker/BufferUnpacker;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/BufferUnpacker", "readValue", "()Lorg/msgpack/type/Value;");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l17);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "newInstance", "()Ljava/lang/Object;");
            mv.visitVarInsn(ASTORE, 3);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitLdcInsn("_decode_");
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitLdcInsn(Type.getType("[B"));
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            mv.visitVarInsn(ALOAD, 3);
            mv.visitInsn(ICONST_1);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/Value", "asRawValue", "()Lorg/msgpack/type/RawValue;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/type/RawValue", "getByteArray", "()[B");
            mv.visitInsn(AASTORE);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l15);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitTypeInsn(NEW, "java/io/IOException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Cannot decode message");
            mv.visitMethodInsn(INVOKESPECIAL, "java/io/IOException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(6, 8);
            mv.visitEnd();

            mv = cv.visitMethod(ACC_PRIVATE, "_encodeList_", "(Ljava/util/List;Ljava/util/List;Ljava/lang/Class;)V", null, new String[]{"java/lang/NoSuchMethodException", "java/lang/IllegalAccessException", "java/lang/IllegalArgumentException", "java/lang/reflect/InvocationTargetException"});
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;");
            mv.visitVarInsn(ASTORE, 4);
            l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/util/Iterator"}, 0, null);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
            l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
            mv.visitVarInsn(ASTORE, 5);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(INSTANCEOF, "java/util/List");
            l2 = new Label();
            mv.visitJumpInsn(IFEQ, l2);
            mv.visitTypeInsn(NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 6);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(CHECKCAST, "java/util/List");
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESPECIAL, currentClass, "_encodeList_", "(Ljava/util/List;Ljava/util/List;Ljava/lang/Class;)V");
            l3 = new Label();
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/lang/Object"}, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(INSTANCEOF, "java/util/Map");
            l4 = new Label();
            mv.visitJumpInsn(IFEQ, l4);
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 6);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(CHECKCAST, "java/util/Map");
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESPECIAL, currentClass, "_encodeMap_", "(Ljava/util/Map;Ljava/util/Map;Ljava/lang/Class;)V");
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l4);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Boolean");
            l5 = new Label();
            mv.visitJumpInsn(IFEQ, l5);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l5);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Byte");
            l6 = new Label();
            mv.visitJumpInsn(IFEQ, l6);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l6);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Short");
            l7 = new Label();
            mv.visitJumpInsn(IFEQ, l7);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l7);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Integer");
            l8 = new Label();
            mv.visitJumpInsn(IFEQ, l8);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l8);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Float");
            l9 = new Label();
            mv.visitJumpInsn(IFEQ, l9);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l9);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Long");
            l10 = new Label();
            mv.visitJumpInsn(IFEQ, l10);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l10);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Double");
            l11 = new Label();
            mv.visitJumpInsn(IFEQ, l11);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l11);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
            l12 = new Label();
            mv.visitJumpInsn(IFEQ, l12);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l12);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitLdcInsn("_encode_");
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            mv.visitVarInsn(ALOAD, 5);
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "[B");
            mv.visitTypeInsn(CHECKCAST, "[B");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
            mv.visitInsn(POP);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitJumpInsn(GOTO, l0);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 7);
            mv.visitEnd();

            mv = cv.visitMethod(ACC_PRIVATE, "_encodeMap_", "(Ljava/util/Map;Ljava/util/Map;Ljava/lang/Class;)V", null, new String[]{"java/lang/NoSuchMethodException", "java/lang/IllegalAccessException", "java/lang/IllegalArgumentException", "java/lang/reflect/InvocationTargetException"});
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "keySet", "()Ljava/util/Set;");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Set", "iterator", "()Ljava/util/Iterator;");
            mv.visitVarInsn(ASTORE, 4);
            l0 = new Label();
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{"java/util/Iterator"}, 0, null);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
            l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            mv.visitVarInsn(ALOAD, 4);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
            mv.visitVarInsn(ASTORE, 5);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitVarInsn(ASTORE, 6);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(INSTANCEOF, "java/util/List");
            l2 = new Label();
            mv.visitJumpInsn(IFEQ, l2);
            mv.visitTypeInsn(NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 7);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(CHECKCAST, "java/util/List");
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESPECIAL, currentClass, "_encodeList_", "(Ljava/util/List;Ljava/util/List;Ljava/lang/Class;)V");
            l3 = new Label();
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_APPEND, 2, new Object[]{"java/lang/Object", "java/lang/Object"}, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(INSTANCEOF, "java/util/Map");
            l4 = new Label();
            mv.visitJumpInsn(IFEQ, l4);
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 7);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 7);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(CHECKCAST, "java/util/Map");
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESPECIAL, currentClass, "_encodeMap_", "(Ljava/util/Map;Ljava/util/Map;Ljava/lang/Class;)V");
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l4);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Boolean");
            l5 = new Label();
            mv.visitJumpInsn(IFEQ, l5);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l5);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Byte");
            l6 = new Label();
            mv.visitJumpInsn(IFEQ, l6);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l6);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Short");
            l7 = new Label();
            mv.visitJumpInsn(IFEQ, l7);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l7);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Integer");
            l8 = new Label();
            mv.visitJumpInsn(IFEQ, l8);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l8);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Float");
            l9 = new Label();
            mv.visitJumpInsn(IFEQ, l9);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l9);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Long");
            l10 = new Label();
            mv.visitJumpInsn(IFEQ, l10);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l10);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/Double");
            l11 = new Label();
            mv.visitJumpInsn(IFEQ, l11);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l11);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
            l12 = new Label();
            mv.visitJumpInsn(IFEQ, l12);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 6);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitJumpInsn(GOTO, l3);
            mv.visitLabel(l12);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitVarInsn(ALOAD, 5);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitLdcInsn("_encode_");
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
            mv.visitVarInsn(ALOAD, 6);
            mv.visitInsn(ICONST_0);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "[B");
            mv.visitTypeInsn(CHECKCAST, "[B");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(POP);
            mv.visitLabel(l3);
            mv.visitFrame(Opcodes.F_CHOP, 2, null, 0, null);
            mv.visitJumpInsn(GOTO, l0);
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
            mv.visitInsn(RETURN);
            mv.visitMaxs(5, 8);
            mv.visitEnd();

            mv = cv.visitMethod(ACC_PUBLIC, "_encode_", "()[B", null, new String[]{"java/io/IOException", "java/lang/NoSuchMethodException", "java/lang/IllegalAccessException", "java/lang/IllegalArgumentException", "java/lang/reflect/InvocationTargetException"});
            mv.visitCode();
            mv.visitTypeInsn(NEW, "java/io/ByteArrayOutputStream");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayOutputStream", "<init>", "()V");
            mv.visitVarInsn(ASTORE, 1);
            mv.visitFieldInsn(GETSTATIC, currentClass, "_MSG_PACK_", "Lorg/msgpack/MessagePack;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/MessagePack", "createPacker", "(Ljava/io/OutputStream;)Lorg/msgpack/packer/Packer;");
            mv.visitVarInsn(ASTORE, 2);

            for (FieldInfo field : fields) {
                String sig;

                switch (field.getType()) {
                    case "Z":
                    case "B":
                    case "S":
                    case "I":
                    case "F":
                    case "J":
                    case "D":
                    case "Ljava/lang/String;":
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, currentClass, field.getName(), field.getType());
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(" + field.getType() + ")Lorg/msgpack/packer/Packer;");
                        mv.visitInsn(POP);
                        break;
                    case "[B":
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, currentClass, field.getName(), field.getType());
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "([B)Lorg/msgpack/packer/Packer;");
                        mv.visitInsn(POP);
                        break;
                    case "[Z":
                    case "[S":
                    case "[I":
                    case "[F":
                    case "[J":
                    case "[D":
                    case "[Ljava/lang/String;":
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, currentClass, field.getName(), field.getType());
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(Ljava/lang/Object;)Lorg/msgpack/packer/Packer;");
                        mv.visitInsn(POP);
                        break;
                    case "Ljava/util/List;":
                        sig = getType(field.getSignature());
                        switch (sig) {
                            case "Ljava/lang/Boolean;":
                            case "Ljava/lang/Byte;":
                            case "Ljava/lang/Short;":
                            case "Ljava/lang/Integer;":
                            case "Ljava/lang/Float;":
                            case "Ljava/lang/Long;":
                            case "Ljava/lang/Double;":
                            case "Ljava/lang/String;":
                                mv.visitVarInsn(ALOAD, 2);
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitFieldInsn(GETFIELD, currentClass, field.getName(), field.getType());
                                mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(Ljava/lang/Object;)Lorg/msgpack/packer/Packer;");
                                mv.visitInsn(POP);
                                break;
                            default:
                                mv.visitTypeInsn(NEW, "java/util/ArrayList");
                                mv.visitInsn(DUP);
                                mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
                                mv.visitVarInsn(ASTORE, 4);
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitVarInsn(ALOAD, 4);
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitFieldInsn(GETFIELD, currentClass, field.getName(), field.getType());
                                mv.visitLdcInsn(Type.getType(sig));
                                mv.visitMethodInsn(INVOKESPECIAL, currentClass, "_encodeList_", "(Ljava/util/List;Ljava/util/List;Ljava/lang/Class;)V");
                                mv.visitVarInsn(ALOAD, 2);
                                mv.visitVarInsn(ALOAD, 4);
                                mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(Ljava/lang/Object;)Lorg/msgpack/packer/Packer;");
                                mv.visitInsn(POP);
                                break;
                        }
                        break;
                    case "Ljava/util/Map;":
                        sig = getType(field.getSignature());
                        switch (sig) {
                            case "Ljava/lang/Boolean;":
                            case "Ljava/lang/Byte;":
                            case "Ljava/lang/Short;":
                            case "Ljava/lang/Integer;":
                            case "Ljava/lang/Float;":
                            case "Ljava/lang/Long;":
                            case "Ljava/lang/Double;":
                            case "Ljava/lang/String;":
                                mv.visitVarInsn(ALOAD, 2);
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitFieldInsn(GETFIELD, currentClass, field.getName(), field.getType());
                                mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(Ljava/lang/Object;)Lorg/msgpack/packer/Packer;");
                                mv.visitInsn(POP);
                                break;
                            default:
                                mv.visitTypeInsn(NEW, "java/util/HashMap");
                                mv.visitInsn(DUP);
                                mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
                                mv.visitVarInsn(ASTORE, 6);
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitVarInsn(ALOAD, 6);
                                mv.visitVarInsn(ALOAD, 0);
                                mv.visitFieldInsn(GETFIELD, currentClass, field.getName(), field.getType());
                                mv.visitLdcInsn(Type.getType(sig));
                                mv.visitMethodInsn(INVOKESPECIAL, currentClass, "_encodeMap_", "(Ljava/util/Map;Ljava/util/Map;Ljava/lang/Class;)V");
                                mv.visitVarInsn(ALOAD, 2);
                                mv.visitVarInsn(ALOAD, 6);
                                mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(Ljava/lang/Object;)Lorg/msgpack/packer/Packer;");
                                mv.visitInsn(POP);
                                break;
                        }
                        break;
                    default:
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, currentClass, field.getName(), field.getType());
                        mv.visitMethodInsn(INVOKEVIRTUAL, field.getType(), "_encode_", "()[B");
                        mv.visitVarInsn(ASTORE, 3);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitVarInsn(ALOAD, 3);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "([B)Lorg/msgpack/packer/Packer;");
                        mv.visitInsn(POP);
                        break;
                }
            }

            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/ByteArrayOutputStream", "toByteArray", "()[B");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(4, 8);
            mv.visitEnd();

            mv = cv.visitMethod(ACC_PUBLIC, "_decode_", "([B)V", null, new String[]{"java/io/IOException", "java/lang/InstantiationException", "java/lang/IllegalAccessException", "java/lang/NoSuchMethodException", "java/lang/IllegalArgumentException", "java/lang/reflect/InvocationTargetException"});

//                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//                mv.visitLdcInsn("Decoding");
//                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, currentClass, "_MSG_PACK_", "Lorg/msgpack/MessagePack;");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/MessagePack", "createBufferUnpacker", "([B)Lorg/msgpack/unpacker/BufferUnpacker;");
            mv.visitVarInsn(ASTORE, 2);

            for (FieldInfo field : fields) {
                String sig;

                switch (field.getType()) {
                    case "Z":
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType("Ljava/lang/Boolean;"));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()" + field.getType());
                        mv.visitFieldInsn(PUTFIELD, currentClass, field.getName(), field.getType());
                        break;
                    case "B":
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType("Ljava/lang/Byte;"));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()" + field.getType());
                        mv.visitFieldInsn(PUTFIELD, currentClass, field.getName(), field.getType());
                        break;
                    case "S":
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType("Ljava/lang/Short;"));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()" + field.getType());
                        mv.visitFieldInsn(PUTFIELD, currentClass, field.getName(), field.getType());
                        break;
                    case "I":
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType("Ljava/lang/Integer;"));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()" + field.getType());
                        mv.visitFieldInsn(PUTFIELD, currentClass, field.getName(), field.getType());
                        break;
                    case "F":
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType("Ljava/lang/Float;"));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()" + field.getType());
                        mv.visitFieldInsn(PUTFIELD, currentClass, field.getName(), field.getType());
                        break;
                    case "J":
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType("Ljava/lang/Long;"));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()" + field.getType());
                        mv.visitFieldInsn(PUTFIELD, currentClass, field.getName(), field.getType());
                        break;
                    case "D":
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType("Ljava/lang/Double;"));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()" + field.getType());
                        mv.visitFieldInsn(PUTFIELD, currentClass, field.getName(), field.getType());
                        break;
                    case "Ljava/lang/String;":
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "java/lang/String");
                        mv.visitFieldInsn(PUTFIELD, currentClass, field.getName(), field.getType());
                        break;
                    case "[Z":
                    case "[B":
                    case "[S":
                    case "[I":
                    case "[F":
                    case "[J":
                    case "[D":
                    case "[Ljava/lang/String;":
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitLdcInsn(Type.getType(field.getType()));
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "read", "(Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, field.getType());
                        mv.visitFieldInsn(PUTFIELD, currentClass, field.getName(), field.getType());
                        break;
                    case "Ljava/util/List;":
                        sig = getType(field.getSignature());
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, currentClass, field.getName(), field.getType());
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType(sig));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "java/util/List");
                        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "addAll", "(Ljava/util/Collection;)Z");
                        mv.visitInsn(POP);
                        break;
                    case "Ljava/util/Map;":
                        sig = getType(field.getSignature());
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitFieldInsn(GETFIELD, currentClass, field.getName(), field.getType());
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType(sig));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, "java/util/Map");
                        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "putAll", "(Ljava/util/Map;)V");
                        break;
                    default:
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitVarInsn(ALOAD, 2);
                        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/unpacker/Unpacker", "readValue", "()Lorg/msgpack/type/Value;");
                        mv.visitLdcInsn(Type.getType(field.getType()));
                        mv.visitMethodInsn(INVOKEVIRTUAL, currentClass, "_decode_", "(Lorg/msgpack/type/Value;Ljava/lang/Class;)Ljava/lang/Object;");
                        mv.visitTypeInsn(CHECKCAST, field.getType().substring(1, field.getType().length() - 1));
                        mv.visitFieldInsn(PUTFIELD, currentClass, field.getName(), field.getType());
                        break;
                }
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 3);
            mv.visitEnd();
        }

        cv.visitEnd();
    }

    private String getType(String signature) {
        String[] arr = signature.split("<");
        String type = arr[arr.length - 1];
        arr = type.split(">");
        type = arr[0];

        String[] mapSplit = type.split(";");
        if (mapSplit.length > 1) {
            type = mapSplit[1] + ";";
        }

        return type;
    }
}
