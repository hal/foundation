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
package org.jboss.hal.ui.resource.pipeline;

import java.util.List;

import elemental2.dom.HTMLElement;

import org.jboss.elemento.IsElement;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;

/**
 * An editable form item produced by the pipeline. Each form item knows how to produce 1..n DMR operations that are flat-mapped
 * into a single composite operation for the resource write.
 * <p>
 * The {@link #operations(ResourceAddress)} method is the key difference from the existing {@code FormItem} class, where
 * operations were produced by the form, not the item.
 */
public interface FormItem extends IsElement<HTMLElement> {

    /** Returns a unique identifier for this form item, suitable for use as a DOM element ID. */
    String identifier();

    /** Returns whether the value has been modified from its original. */
    boolean isModified();

    /** Validates the current input. Returns {@code true} if valid. */
    boolean validate();

    /** Clears any validation state (error messages, visual indicators). */
    void resetValidation();

    /**
     * Produces the DMR operations needed to persist this form item's current value. Returns an empty list if the item has not
     * been modified. The operations are flat-mapped with all other form items' operations into a single composite operation.
     * <p>
     * Depending on the item type:
     * <ul>
     *     <li>Single attribute → 1 {@code write-attribute} or {@code undefine-attribute} operation</li>
     *     <li>Composite OBJECT → 1 {@code write-attribute} with nested object value</li>
     *     <li>Sibling group → n {@code write-attribute}/{@code undefine-attribute} operations (one per attribute)</li>
     *     <li>Flattened sub-attribute → 1 {@code write-attribute} using the FQN path (e.g. {@code "file.path"})</li>
     * </ul>
     *
     * @param address the resolved resource address for the DMR operations
     */
    List<Operation> operations(ResourceAddress address);
}
