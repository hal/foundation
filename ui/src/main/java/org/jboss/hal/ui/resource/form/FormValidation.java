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
package org.jboss.hal.ui.resource.form;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;

/**
 * Validates the form as a whole by examining relationships between multiple form items. Unlike per-item validation (handled by
 * {@link NativeControl#validate}), form-level validation checks cross-field constraints such as requires and alternatives
 * relationships from the WildFly management model metadata.
 *
 * @see RequiredByValidation
 * @see ExactlyOneAlternativeValidation
 * @see NotMoreThanOneAlternativeValidation
 */
public interface FormValidation {

    /**
     * The result of a form-level validation. Contains a form-level message shown as an alert above the form and per-item error
     * messages shown on individual form items.
     *
     * @param message    the form-level error message for the alert
     * @param itemErrors a map of attribute name to error message for each affected form item
     */
    record Result(String message, Map<String, String> itemErrors) {

        /** Creates a result with per-item errors. */
        public Result(String message, Map<String, String> itemErrors) {
            this.message = message;
            this.itemErrors = Collections.unmodifiableMap(itemErrors);
        }

        /** Creates a result with a form-level message only, without per-item errors. */
        public Result(String message) {
            this(message, Collections.emptyMap());
        }
    }

    /**
     * Validates the form by examining the given form items. Returns a {@link Result} describing the validation failure, or
     * {@code null} if the form is valid.
     */
    Result validate(List<FormItem> items);

    /** Finds a form item by attribute name. Returns {@code null} if not found. */
    static FormItem findItem(List<FormItem> items, String name) {
        for (FormItem item : items) {
            if (item.attribute().name().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /** Returns {@code true} if the item's current value is empty or equals its default value. */
    static boolean isEmptyOrDefault(FormItem item) {
        EditableControl<?> ec = item.editableControl();
        if (ec == null) {
            return true;
        }
        ModelNode value = ec.modelNode();
        if (!value.isDefined()) {
            return true;
        }
        if (item.attribute().description().hasDefault()) {
            String defaultValue = item.attribute().description().get(DEFAULT).asString();
            return value.asString().equals(defaultValue);
        }
        return false;
    }

    /** Returns {@code true} if the item's current value is empty (undefined). */
    static boolean isEmpty(FormItem item) {
        EditableControl<?> ec = item.editableControl();
        if (ec == null) {
            return true;
        }
        return !ec.modelNode().isDefined();
    }
}
