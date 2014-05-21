/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.util;

/**
 *
 * @author atrimble
 */
public class TopicUtils {

    public static final String ALL_PARTITIONS = ".*";
    
    public static String getTopicString(String topic, String partition) {
        return new StringBuilder().append(topic).append("(").append(partition).append(")").toString();
    }

    public static String getNotifyTopicString(String topic, String partition) {
        return new StringBuilder().append(topic).append(partition).toString();
    }

    public static String getTopic(String topicPartition) {
        return topicPartition.split("\\(")[0];
    }

    public static String getPartition(String topicPartition) {
        String[] spl = topicPartition.split("\\(");
        if(spl.length > 1) {
            return spl[1];
        } 

        return ALL_PARTITIONS;
    }
}
