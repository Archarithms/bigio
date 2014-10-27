/*
 * Copyright 2014 Archarithms Inc.
 */

package io.bigio.container;

import io.bigio.Component;
import io.bigio.Initialize;
import io.bigio.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author atrimble
 */
@Component
public class MultipleInjectComponent {

    private boolean initialized = false;

    @Inject
    private final List<InjectInterface> comps = new ArrayList<>();

    public MultipleInjectComponent() {
        
    }

    @Initialize
    public void init() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public List<InjectInterface> getComponents() {
        return comps;
    }
}
