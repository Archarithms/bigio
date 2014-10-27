/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.agent;

import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import static org.objectweb.asm.Opcodes.ASM4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class SignatureCollector extends ClassVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageAdapter.class);

    private final Map<String, String> signatures = new HashMap<>();

    public SignatureCollector(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if(signature != null) {
            signatures.put(name, signature);
        }
        return cv.visitField(access, name, desc, signature, value);
    }

    public Map<String, String> getSignatures() {
        return signatures;
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
