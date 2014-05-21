/*
 * Copyright 2014 Archarithms Inc.
 */

package com.a2i.bigio;

import com.a2i.bigio.core.Container;
import com.a2i.bigio.core.member.MemberStatus;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author atrimble
 */
public class TestInject {
    @Test
    public void testInject() throws Exception {
        Parameters.INSTANCE.currentOS();
        Container scanner = Container.INSTANCE;
        scanner.scan();

        Thread.sleep(1000l);

        Speaker speaker = scanner.getInstance(Speaker.class);

        // A bunch of stuff needs to be wired together for this to be non null
        // and active.
        assertTrue(speaker.getMe() != null);
        assertTrue(speaker.getMe().getStatus() == MemberStatus.Alive);
        
        speaker.shutdown();
    }
}
