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

import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;

/**
 * A {@link ModelNode} with an associated name. Typically created from a {@link Property} in a resource listing, where the
 * property name becomes the node name and the property value becomes the node content. The name is stored both as a field
 * and as the {@code "name"} attribute inside the model node.
 */
public class NamedNode extends ModelNode {

    // ------------------------------------------------------ instance

    private final String name;
    private final ModelNode node;

    /** Creates a new named node wrapping an undefined model node. */
    public NamedNode() {
        this(new ModelNode());
    }

    /**
     * Creates a new named node from the given model node. If the node contains a {@code "name"} attribute, that value is
     * used as the name. Otherwise, a generated undefined name is assigned.
     */
    public NamedNode(ModelNode node) {
        if (node.isDefined()) {
            if (node.hasDefined(NAME)) {
                this.name = node.get(NAME).asString();
                this.node = node;
                set(node);
            } else {
                this.name = undefinedName();
                this.node = node;
                set(node);
                assignName(name);
            }
        } else {
            this.name = undefinedName();
            this.node = node;
            set(node);
            // Do not call assignName(name) here.
            // This defines the model node.
        }
    }

    /** Creates a new named node from the given property (name becomes the node name, value becomes the content). */
    public NamedNode(Property property) {
        this(property.getName(), property.getValue());
    }

    /** Creates a new named node with the given name and content. */
    public NamedNode(String name, ModelNode node) {
        this.name = name;
        this.node = node;
        set(node);
        assignName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NamedNode)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        NamedNode namedNode = (NamedNode) o;
        if (!name.equals(namedNode.name)) {
            return false;
        }
        return node.equals(namedNode.node);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + node.hashCode();
        return result;
    }

    /**
     * @return a string representation of this model node
     */
    @Override
    public String toString() {
        return "NamedNode(" + name + ")";
    }

    // ------------------------------------------------------ api

    /**
     * @return the name of this named node
     */
    public String name() {
        return name;
    }

    /** Sets the {@code "name"} attribute inside the model node to the given name. */
    public void assignName(String name) {
        get(NAME).set(name);
    }

    /**
     * @return the model node of this named node
     */
    public ModelNode asModelNode() {
        return node;
    }

    /** Updates this node's content with the given model node, preserving the original name. */
    public void update(ModelNode node) {
        set(node);
        assignName(name); // restore name!
    }

    // ------------------------------------------------------ internal

    private static String undefinedName() {
        return UNDEFINED + "-" + System.currentTimeMillis();
    }
}
