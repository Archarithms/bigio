/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.container;

import io.bigio.Component;
import io.bigio.Initialize;

/**
 *
 * @author atrimble
 */
@Component
public class TestComponent implements InjectInterface {

    private boolean initialized = false;

    public TestComponent() {
        
    }

    @Initialize
    public void init() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
