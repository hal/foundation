package org.jboss.hal.ui.brick;

import org.jboss.hal.core.Humanize;
import org.patternfly.component.label.Label;
import org.patternfly.style.Color;

import static org.patternfly.component.label.Label.label;

public final class ModelBricks {

    public static Label typeLabel(String type) {
        return typeLabel(type, false);
    }

    public static Label typeLabel(String type, boolean compact) {
        return label(type, Humanize.capitalCase(type), typeColor(type)).compact(compact);
    }

    private static Color typeColor(String type) {
        return switch (type) {
            case "attribute" -> Color.blue;
            case "resource" -> Color.green;
            case "operation" -> Color.purple;
            case "capability" -> Color.teal;
            default -> Color.grey;
        };
    }
}
