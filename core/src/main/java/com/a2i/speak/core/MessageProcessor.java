/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.a2i.speak.core;

import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author atrimble
 */
public class MessageProcessor extends AbstractProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for(TypeElement element : annotations) {
            if(element.getKind() == ElementKind.ANNOTATION_TYPE) {
                LOG.info("Annotation");
            } else if(element.getKind() == ElementKind.FIELD) {
                LOG.info("Field");
            } else if(element.getKind() == ElementKind.METHOD) {
                LOG.info("Method");
            }
        }

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("com.a2i.speak.core.Message");
    }
}
