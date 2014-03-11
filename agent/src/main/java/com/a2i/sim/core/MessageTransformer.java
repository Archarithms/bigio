/*
 * Copyright 2014 Archarithms Inc.
 */
package com.a2i.sim.core;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
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
        inst.addTransformer(new MessageTransformer(), false);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
            byte[] b) throws IllegalClassFormatException {

        ClassReader cr = new ClassReader(b);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassVisitor cv = new MessageAdapter(cw);
        cr.accept(cv, 0);
        if (((MessageAdapter) cv).wasMessage() || ((MessageAdapter) cv).wasEnum()) {
//            LOG.info("Returning transformed class for '" + className + "'");

//            cr = new ClassReader(cw.toByteArray());
//            cv = new TraceClassVisitor(new PrintWriter(System.out));
//            cr.accept(cv, 0);

            return cw.toByteArray();
        } else {
            return null;
        }
    }
}
