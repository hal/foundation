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

import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.hal.meta.description.OperationDescription;

import elemental2.dom.HTMLDivElement;

/**
 * Factory methods for rendering management model operation descriptions as PatternFly UI elements.
 */
public final class OperationBricks {

    /**
     * Renders an operation description including its text and optional deprecation notice. Delegates to
     * {@link DescriptionBricks#description(org.jboss.hal.meta.description.Description)} for the shared rendering
     * logic.
     *
     * @param operation the operation description from the management model
     * @return a div element containing the formatted description
     */
    public static HTMLContainerBuilder<HTMLDivElement> operationDescription(OperationDescription operation) {
        return DescriptionBricks.description(operation);
    }

    private OperationBricks() {
    }
}
