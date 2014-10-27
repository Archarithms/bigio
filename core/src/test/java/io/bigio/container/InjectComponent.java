/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.container;

import io.bigio.Component;
import io.bigio.Initialize;
import io.bigio.Inject;

/**
 *
 * @author atrimble
 */
@Component
public class InjectComponent implements InjectInterface {

    private boolean initialized = false;

    @Inject
    public TestComponent testComponent = null;

    public InjectComponent() {
        
    }

    @Initialize
    public void init() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public TestComponent getTestComponent() {
        return testComponent;
    }
}
