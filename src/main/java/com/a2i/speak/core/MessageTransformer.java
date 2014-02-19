/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.a2i.speak.core;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.util.TraceClassVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class MessageTransformer implements ClassFileTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(MessageTransformer.class);

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new MessageTransformer());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
            byte[] b) throws IllegalClassFormatException {
        ClassReader cr = new ClassReader(b);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new MessageAdapter(cw);
        cr.accept(cv, 0);
        if(((MessageAdapter)cv).wasMessage()) {
            LOG.info("Returning transformed class");

//            cr = new ClassReader(cw.toByteArray());
//            cv = new TraceClassVisitor(new PrintWriter(System.out));
//            cr.accept(cv, 0);
            
            return cw.toByteArray();
        } else {
            return null;
        }
    }

    public class MessageAdapter extends ClassVisitor {

        String currentClass;
        boolean isMessage = false;
        List<Tuple> fields = new ArrayList<>();

        public MessageAdapter(ClassVisitor cv) {
            super(ASM4, cv);
        }

        public boolean wasMessage() {
            return isMessage;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if(desc.equals("Lcom/a2i/speak/core/Message;")) {
                isMessage = true;
                LOG.info(currentClass + " : " + isMessage);
            }

            return cv.visitAnnotation(desc, visible);
        }

        @Override
        public void visit(int version, int access, String name,
                String signature, String superName, String[] interfaces) {

            currentClass = name;
            isMessage = false;

            cv.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if(isMessage) {
                fields.add(new Tuple(name, desc, signature));
            }

            return cv.visitField(access, name, desc, signature, value);
        }

        @Override
        public void visitEnd() {
            if(isMessage) {
                int maxStack = 3;
                int maxLocals = 4;
                
                LOG.info("Adding encoder to " + currentClass);

                MethodVisitor mv = cv.visitMethod(ACC_PRIVATE, "_encodeList_", "(Ljava/util/List;Ljava/util/List;)V", "<T:Ljava/lang/Object;>(Ljava/util/List;Ljava/util/List<TT;>;)V", new String[] { "java/io/IOException" });
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;");
                mv.visitVarInsn(ASTORE, 3);
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/util/Iterator"}, 0, null);
                mv.visitVarInsn(ALOAD, 3);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z");
                Label l1 = new Label();
                mv.visitJumpInsn(IFEQ, l1);
                mv.visitVarInsn(ALOAD, 3);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;");
                mv.visitVarInsn(ASTORE, 4);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitTypeInsn(INSTANCEOF, "java/util/List");
                Label l2 = new Label();
                mv.visitJumpInsn(IFEQ, l2);
                mv.visitTypeInsn(NEW, "java/util/ArrayList");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
                mv.visitVarInsn(ASTORE, 5);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 5);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 5);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitTypeInsn(CHECKCAST, "java/util/List");
                mv.visitMethodInsn(INVOKESPECIAL, currentClass, "_encodeList_", "(Ljava/util/List;Ljava/util/List;)V");
                Label l3 = new Label();
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l2);
                mv.visitFrame(F_APPEND, 1, new Object[] {"java/lang/Object"}, 0, null);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitTypeInsn(INSTANCEOF, "java/lang/Boolean");
                Label l4 = new Label();
                mv.visitJumpInsn(IFEQ, l4);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l4);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitTypeInsn(INSTANCEOF, "java/lang/Byte");
                Label l5 = new Label();
                mv.visitJumpInsn(IFEQ, l5);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l5);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitTypeInsn(INSTANCEOF, "java/lang/Short");
                Label l6 = new Label();
                mv.visitJumpInsn(IFEQ, l6);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l6);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitTypeInsn(INSTANCEOF, "java/lang/Integer");
                Label l7 = new Label();
                mv.visitJumpInsn(IFEQ, l7);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l7);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitTypeInsn(INSTANCEOF, "java/lang/Float");
                Label l8 = new Label();
                mv.visitJumpInsn(IFEQ, l8);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l8);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitTypeInsn(INSTANCEOF, "java/lang/Long");
                Label l9 = new Label();
                mv.visitJumpInsn(IFEQ, l9);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l9);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitTypeInsn(INSTANCEOF, "java/lang/Double");
                Label l10 = new Label();
                mv.visitJumpInsn(IFEQ, l10);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l10);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
                Label l11 = new Label();
                mv.visitJumpInsn(IFEQ, l11);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
                mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l11);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "_encode_", "()[B");
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
                mv.visitLabel(l3);
                mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
                mv.visitJumpInsn(GOTO, l0);
                mv.visitLabel(l1);
                mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
                mv.visitInsn(RETURN);
                mv.visitMaxs(3, 6);
                mv.visitEnd();
                
                mv = cv.visitMethod(ACC_PUBLIC, "_encode_", "()[B", null, new String[] { "java/io/IOException" });
                mv.visitCode();
                mv.visitTypeInsn(NEW, "org/msgpack/MessagePack");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "org/msgpack/MessagePack", "<init>", "()V");
                mv.visitVarInsn(ASTORE, 1);
                mv.visitTypeInsn(NEW, "java/io/ByteArrayOutputStream");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/io/ByteArrayOutputStream", "<init>", "()V");
                mv.visitVarInsn(ASTORE, 2);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/msgpack/MessagePack", "createPacker", "(Ljava/io/OutputStream;)Lorg/msgpack/packer/Packer;");
                mv.visitVarInsn(ASTORE, 3);

                for(Tuple tuple : fields) {
                    String type;

                    LOG.info(tuple.toString());

                    switch(tuple.getType()) {
                        case "Z":
                        case "C":
                        case "B":
                        case "S":
                        case "I":
                        case "F":
                        case "J":
                        case "D":
                        case "Ljava/lang/String;":
                            type = tuple.getType();
                            mv.visitVarInsn(ALOAD, 3);
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, currentClass, tuple.getName(), tuple.getType());
                            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(" + type + ")Lorg/msgpack/packer/Packer;");
                            mv.visitInsn(POP);
                            break;
                        case "[Z":
                        case "[C":
                        case "[B":
                        case "[S":
                        case "[I":
                        case "[F":
                        case "[J":
                        case "[D":
                        case "[Ljava/lang/String;":
                            mv.visitVarInsn(ALOAD, 3);
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, currentClass, tuple.getName(), tuple.getType());
                            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(Ljava/lang/Object;)Lorg/msgpack/packer/Packer;");
                            mv.visitInsn(POP); 
                            break;
                        case "Ljava/util/List;":
                            maxLocals = 5;
                            mv.visitTypeInsn(NEW, "java/util/ArrayList");
                            mv.visitInsn(DUP);
                            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
                            mv.visitVarInsn(ASTORE, 4);
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitVarInsn(ALOAD, 4);
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, currentClass, tuple.getName(), "Ljava/util/List;");
                            mv.visitMethodInsn(INVOKESPECIAL, currentClass, "_encodeList_", "(Ljava/util/List;Ljava/util/List;)V");
                            mv.visitVarInsn(ALOAD, 3);
                            mv.visitVarInsn(ALOAD, 4);
                            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(Ljava/lang/Object;)Lorg/msgpack/packer/Packer;");
                            mv.visitInsn(POP);
                            break;
                        default:
                            maxLocals = 5;
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitFieldInsn(GETFIELD, currentClass, tuple.getName(), tuple.getType());
                            mv.visitMethodInsn(INVOKEVIRTUAL, tuple.getType().substring(1, tuple.getType().length() - 1), "_encode_", "()[B");
                            mv.visitVarInsn(ASTORE, 4);
                            mv.visitVarInsn(ALOAD, 3);
                            mv.visitVarInsn(ALOAD, 4);
                            mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "([B)Lorg/msgpack/packer/Packer;");
                            mv.visitInsn(POP);
                            break;
                    }
                }

                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/ByteArrayOutputStream", "toByteArray", "()[B");
                mv.visitInsn(ARETURN);
                mv.visitMaxs(maxStack, maxLocals);
                mv.visitEnd();
            }
            
            cv.visitEnd();
        }
    }

    private void encodeList(Tuple tuple, MethodVisitor mv, String currentClass) {
        String[]  arr = tuple.getSignature().split("<");
        String listType = arr[1].split(">")[0];

        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, currentClass, tuple.getName(), tuple.getType());
        mv.visitMethodInsn(INVOKEINTERFACE, "org/msgpack/packer/Packer", "write", "(Ljava/lang/Object;)Lorg/msgpack/packer/Packer;");
        mv.visitInsn(POP); 
    }

    private class Tuple {
        private final String name;
        private final String type;
        private final String signature;
        
        public Tuple(String name, String type, String signature) {
            this.name = name;
            this.type = type;
            this.signature = signature;
        }

        public String getName() {
            return name;
        }

        public String getSignature() {
            return signature;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return name + " : " + type + " : " + signature;
        }
    }
}
