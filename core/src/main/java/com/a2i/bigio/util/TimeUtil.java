/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio.util;

import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author atrimble
 */
public class TimeUtil {
    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
    private static final Calendar CALENDAR = Calendar.getInstance(TIME_ZONE);
        
    public static int getMillisecondsSinceMidnight() {
        long ct = System.currentTimeMillis();
        CALENDAR.setTimeInMillis(ct);
        int hour = CALENDAR.get(Calendar.HOUR_OF_DAY);
        int minute = CALENDAR.get(Calendar.MINUTE);
        int second = CALENDAR.get(Calendar.SECOND);
        int msec = CALENDAR.get(Calendar.MILLISECOND);

        return (hour * 60 * 60 + minute * 60 + second) * 1000 + msec;
    }
}
