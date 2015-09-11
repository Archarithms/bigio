/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.container;

import io.bigio.Parameters;
import io.bigio.BigIO;
import io.bigio.core.Container;
import io.bigio.core.member.MemberStatus;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
        Parameters.INSTANCE.setProperty("io.bigio.componentDir", "target");
        Container scanner = Container.INSTANCE;
        scanner.scan();

        Thread.sleep(1000l);

        BigIO speaker = scanner.getInstance(BigIO.class);

        // A bunch of stuff needs to be wired together for this to be non null
        // and active.
        assertTrue(speaker.getMe() != null);
        assertTrue(speaker.getMe().getStatus() == MemberStatus.Alive);

        TestComponent comp = scanner.getInstance(TestComponent.class);
        assertNotNull(comp);
        assertTrue(comp.isInitialized());

        InjectComponent injectComp = scanner.getInstance(InjectComponent.class);
        assertNotNull(injectComp);
        assertTrue(injectComp.isInitialized());
        assertNotNull(injectComp.getTestComponent());

        MultipleInjectComponent multInjectComp = scanner.getInstance(MultipleInjectComponent.class);
        assertNotNull(multInjectComp);
        assertTrue(multInjectComp.isInitialized());
        assertNotNull(multInjectComp.getComponents());
        assertFalse(multInjectComp.getComponents().isEmpty());
        assertTrue(multInjectComp.getComponents().size() == 2);
        
        speaker.shutdown();
    }
}
