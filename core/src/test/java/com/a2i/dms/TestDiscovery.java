/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.dms;

import com.a2i.dms.Starter;
import com.a2i.dms.Speaker;
import com.a2i.dms.core.member.Member;
import java.util.Collection;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author atrimble
 */
public class TestDiscovery {

    private static Speaker speaker1;
    private static Speaker speaker2;

    @BeforeClass
    public static void init() throws InterruptedException {
        speaker1 = Starter.bootstrap();
        speaker2 = Starter.bootstrap();
    }

    @AfterClass
    public static void shutdown() throws InterruptedException {
        speaker1.shutdown();
        speaker2.shutdown();
    }

    @Test
    public void testDiscovery() throws InterruptedException {

        Thread.sleep(500l);

        Collection<Member> members1 = speaker1.listMembers();
        Collection<Member> members2 = speaker2.listMembers();

        assertTrue(members1.size() == members2.size());

        assertTrue(members1.containsAll(members2));
        assertTrue(members2.containsAll(members1));
        assertTrue(members2.contains(speaker1.getMe()));
        assertTrue(members1.contains(speaker2.getMe()));
    }
}
