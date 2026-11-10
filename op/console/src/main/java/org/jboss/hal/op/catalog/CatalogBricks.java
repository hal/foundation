package org.jboss.hal.op.catalog;

import org.jboss.hal.mgt.ModelGraphType;
import org.patternfly.component.label.Label;

import static org.patternfly.component.label.Label.label;
import static org.patternfly.style.Color.blue;
import static org.patternfly.style.Color.grey;
import static org.patternfly.style.Color.purple;
import static org.patternfly.style.Color.red;
import static org.patternfly.style.Color.teal;

class CatalogBricks {

    static Label typeLabel(ModelGraphType type) {
        if (type != null) {
            return switch (type) {
                case RESOURCE -> label(type.type, type.singular, blue);
                case ATTRIBUTE -> label(type.type, type.singular, teal);
                case OPERATION -> label(type.type, type.singular, red);
                case CAPABILITY -> label(type.type, type.singular, purple);
            };
        } else {
            return label("unknown", "?", grey);
        }
    }
}
