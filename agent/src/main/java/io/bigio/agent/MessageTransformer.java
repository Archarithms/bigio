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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A java agent for injecting encoding/decoding logic into messages.
 *
 * @author Andy Trimble
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

        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = null;

        try {
            clazz = pool.makeClass(new ByteArrayInputStream(b));

            if (clazz.hasAnnotation(Class.forName("io.bigio.Message"))) {
                LOG.trace("Creating serialization helper for class " + className);
                ClassReader cr = new ClassReader(b);
                ClassWriter cw = new ClassWriter(cr, 0);
                ClassVisitor cv = new SignatureCollector(cw);
                cr.accept(cv, 0);
                Map<String, String> signatures = ((SignatureCollector) cv).getSignatures();

                JATransformer.transform(clazz, signatures, pool);
            }
        } catch (IOException ex) {
            LOG.error("IO Error", ex);
            return null;
        } catch (ClassNotFoundException ex) {
            LOG.error("Cannot find message annotation.", ex);
            return null;
        } finally {
            if (clazz != null) {
                clazz.detach();
            }
        }

        try {
            return clazz.toBytecode();
        } catch (IOException ex) {
            LOG.error("IOException.", ex);
        } catch (CannotCompileException ex) {
            LOG.error("Cannot compile.", ex);
        }

        return null;
    }
}
