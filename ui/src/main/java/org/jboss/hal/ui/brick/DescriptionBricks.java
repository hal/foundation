/*
 *  Copyright 2024 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.ui.brick;

import java.util.Iterator;

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.description.Deprecation;
import org.jboss.hal.meta.description.Description;
import org.patternfly.component.list.ListItem;

import elemental2.dom.HTMLDivElement;

import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.patternfly.style.Classes.util;

/**
 * Shared description rendering foundation used by {@link AttributeBricks} and {@link OperationBricks}. Provides the
 * common logic for rendering description text and deprecation notices from the management model metadata.
 */
public final class DescriptionBricks {

    /** Controls the level of detail included when rendering an attribute description. */
    public enum AttributeDescriptionContent {

        /** Include all metadata: read-only flag, required, capability reference, expressions, unit, default, requires, alternatives, and restart mode. */
        all,

        /** Same as {@link #all} but without the read-only indicator. Used when the read-only state is shown elsewhere. */
        allButReadOnly,

        /** Only the description text and deprecation notice — no metadata list. */
        descriptionOnly;
    }

    /**
     * Renders a description with its text and optional deprecation notice. The deprecation notice includes the version
     * it was deprecated since and the reason.
     */
    static HTMLContainerBuilder<HTMLDivElement> description(Description description) {
        HTMLContainerBuilder<HTMLDivElement> div = div();
        div.add(div().text(description.description()));
        Deprecation deprecation = description.deprecation();
        if (deprecation.isDefined()) {
            div.add(div().css(util("mt-sm"))
                    .add("Deprecated since " + deprecation.since().toString())
                    .add(br())
                    .add("Reason: " + deprecation.reason()));
        }
        return div;
    }

    /** Appends a comma-separated list of {@code code}-formatted values to the given list item. */
    static void enumerate(ListItem listItem, java.util.List<ModelNode> values) {
        for (Iterator<ModelNode> iterator = values.iterator(); iterator.hasNext(); ) {
            ModelNode value = iterator.next();
            listItem.add(code().text(value.asString()));
            if (iterator.hasNext()) {
                listItem.add(", ");
            }
        }
    }

    private DescriptionBricks() {
    }
}
