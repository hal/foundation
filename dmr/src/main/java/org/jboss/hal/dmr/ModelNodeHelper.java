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
package org.jboss.hal.dmr;

import java.util.function.Function;

import org.jboss.hal.env.Version;

import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MAJOR_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MICRO_VERSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MANAGEMENT_MINOR_VERSION;

public final class ModelNodeHelper {

    // ------------------------------------------------------ nested

    /**
     * Tries to get a deeply nested model node from the specified model node. Nested paths must be separated with ".".
     *
     * @param modelNode The model node to read from
     * @param path      A path separated with "."
     * @return The nested node or an undefined model node
     */
    public static ModelNode nested(ModelNode modelNode, String path) {
        return nested(modelNode, path, false);
    }

    /**
     * Retrieves a nested {@link ModelNode} from the specified {@code modelNode} using the given {@code path}. Nested paths are
     * separated by ".". If the path does not exist and {@code createUndefined} is set to true, undefined nodes are created
     * along the path. If the path does not exist and {@code createUndefined} is false, an undefined {@code ModelNode} is
     * returned.
     *
     * @param modelNode       the root {@link ModelNode} to navigate
     * @param path            the path to the nested node, separated by "."
     * @param createUndefined a flag indicating whether undefined nodes should be created along the path
     * @return the nested {@link ModelNode} corresponding to the path, or an undefined {@link ModelNode} if the path is invalid
     * or does not exist
     */
    public static ModelNode nested(ModelNode modelNode, String path, boolean createUndefined) {
        ModelNode undefined = new ModelNode();

        if (path != null && !path.isEmpty()) {
            String[] keys = path.split("\\.");
            ModelNode current = modelNode;
            for (String key : keys) {
                String safeKey = ValueEncoder.decode(key);
                if (current.hasDefined(safeKey)) {
                    current = current.get(safeKey);
                } else if (createUndefined) {
                    current.get(safeKey).set(new ModelNode());
                    current = current.get(safeKey);
                } else {
                    current = undefined;
                    break;
                }
            }
            return current;
        }
        return undefined;
    }

    // ------------------------------------------------------ enums

    /**
     * Converts the given {@link ModelNode} value to an enum constant using the provided {@link Function}. If the conversion
     * fails, the method returns the default value.
     * <p>
     * The method checks if the {@code attribute} is defined in {@code modelNode} and delegates to
     * {@link #asEnumValue(ModelNode, Function, Enum)}.
     *
     * @param <E>          the type of the enum
     * @param modelNode    the {@link ModelNode} that contains the value to convert
     * @param attribute    the attribute name of the value in the {@link ModelNode}
     * @param valueOf      the function to convert the value to an enum constant
     * @param defaultValue the default value to return if the conversion fails
     * @return the converted enum constant or the default value if the conversion fails
     */
    public static <E extends Enum<E>> E asEnumValue(ModelNode modelNode, String attribute, Function<String, E> valueOf,
            E defaultValue) {
        if (modelNode.hasDefined(attribute)) {
            return asEnumValue(modelNode.get(attribute), valueOf, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Converts the given {@link ModelNode} value to an enum constant using the provided {@link Function}. If the conversion
     * fails, the method returns the default value.
     * <p>
     * {@link ModelNode#asString()} is used to get the model node value. Before the conversion, '-' are replaced by '_' and
     * {@code String.toUpperCase()} is applied to the model node value.
     *
     * @param <E>            the type of the enum
     * @param modelNodeValue the ModelNode value to convert
     * @param valueOf        the function to convert the value to an enum constant
     * @param defaultValue   the default value to return if the conversion fails
     * @return the converted enum constant or the default value if the conversion fails
     */
    public static <E extends Enum<E>> E asEnumValue(ModelNode modelNodeValue, Function<String, E> valueOf, E defaultValue) {
        E value = defaultValue;
        String convertedValue = modelNodeValue.asString().replace('-', '_').toUpperCase();
        try {
            value = valueOf.apply(convertedValue);
        } catch (IllegalArgumentException ignored) {
        }
        return value;
    }

    // ------------------------------------------------------ version

    public static Version parseVersion(ModelNode modelNode) {
        if (modelNode.hasDefined(MANAGEMENT_MAJOR_VERSION) &&
                modelNode.hasDefined(MANAGEMENT_MINOR_VERSION) &&
                modelNode.hasDefined(MANAGEMENT_MICRO_VERSION)) {
            int major = modelNode.get(MANAGEMENT_MAJOR_VERSION).asInt();
            int minor = modelNode.get(MANAGEMENT_MINOR_VERSION).asInt();
            int micro = modelNode.get(MANAGEMENT_MICRO_VERSION).asInt();
            return new Version(major, minor, micro);
        }
        return Version.EMPTY_VERSION;
    }

    // ------------------------------------------------------  instance

    private ModelNodeHelper() {}
}
