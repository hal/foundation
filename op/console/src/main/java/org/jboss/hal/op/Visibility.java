package org.jboss.hal.op;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.op.mgt.ModelGraphTools;

@ApplicationScoped
public class Visibility {

    private final ModelGraphTools modelGraphTools;

    @Inject
    public Visibility(ModelGraphTools modelGraphTools) {
        this.modelGraphTools = modelGraphTools;
    }

    public void visible() {
        modelGraphTools.refresh();
    }

    public void hidden() {
    }
}
