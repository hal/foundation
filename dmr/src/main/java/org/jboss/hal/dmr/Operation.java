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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATION_HEADERS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOLVE_EXPRESSION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ROLES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WHOAMI_OPERATION;

/**
 * Represents a DMR operation.
 */
public class Operation extends ModelNode {

    private final String name;
    private final ResourceAddress address;
    private final ModelNode parameter;
    private final ModelNode header;
    private final Set<String> roles;

    Operation(String name, ResourceAddress address, ModelNode parameter, ModelNode header, Set<String> roles) {
        this.name = name;
        this.address = address;
        this.parameter = parameter == null ? new ModelNode() : parameter;
        this.header = header;
        this.roles = roles;

        set(this.parameter.clone());
        get(OP).set(name);
        get(ADDRESS).set(address);
        if (header.isDefined()) {
            get(OPERATION_HEADERS).set(header);
        }
        addRolesAsHeaders();
    }

    private void addRolesAsHeaders() {
        if (roles != null && !roles.isEmpty() && !name.equals(WHOAMI_OPERATION)) {
            // roles are headers!
            if (roles.size() == 1) {
                header.get(ROLES).set(roles.iterator().next());
            } else {
                roles.forEach(role -> header.get(ROLES).add(role));
            }
            get(OPERATION_HEADERS).set(header);
        }
    }

    /**
     * @return the name of the operation
     */
    public String getName() {
        return get(OP).asString();
    }

    /**
     * @return the address of the operation
     */
    public ResourceAddress getAddress() {
        return address;
    }

    /**
     * @return the parameters of the operation
     */
    public ModelNode getParameter() {
        return parameter;
    }

    /**
     * @return the header of the operation
     */
    public ModelNode getHeader() {
        return header;
    }

    /** Returns {@code true} if this operation has at least one parameter defined. */
    public boolean hasParameter() {
        return parameter.isDefined() && !parameter.asList().isEmpty();
    }

    /** Returns the RBAC roles associated with this operation. */
    public Set<String> getRoles() {
        return roles;
    }

    /** Returns a copy of this operation configured to run with the specified RBAC roles. */
    public Operation runAs(Set<String> runAs) {
        return new Operation(name, address, parameter, header, new HashSet<>(runAs));
    }

    /**
     * @return the string representation of the operation as used in the CLI
     */
    @Override
    public String toString() {
        return asCli();
    }

    /**
     * @return the string representation of the operation as used in the CLI
     */
    public String asCli() {
        StringBuilder builder = new StringBuilder();
        if (address.isDefined() && !address.asList().isEmpty()) {
            builder.append(address);
        }
        builder.append(":").append(name);
        if (hasParameter()) {
            builder.append("(");
            for (Iterator<Property> iterator = parameter.asPropertyList().iterator(); iterator.hasNext();) {
                Property p = iterator.next();
                builder.append(p.getName()).append("=").append(p.getValue().asString());
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append(")");
        }
        if (header.isDefined() && !header.asList().isEmpty()) {
            builder.append("{");
            for (Iterator<Property> iterator = header.asPropertyList().iterator(); iterator.hasNext();) {
                Property p = iterator.next();
                builder.append(p.getName()).append("=").append(p.getValue().asString());
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("}");
        }
        return builder.toString();
    }

    /**
     * Fluent builder for constructing {@link Operation} instances with address, parameters, headers, and RBAC roles.
     */
    public static class Builder {

        private final String name;
        private final ResourceAddress address;
        private ModelNode parameter;
        private final ModelNode header;
        private final Set<String> roles;

        /** Creates a builder for an operation with the given address and name. */
        public Builder(ResourceAddress address, String name) {
            this(address, name, false);
        }

        /**
         * Creates a builder for an operation with the given address and name, optionally enabling expression resolution.
         */
        public Builder(ResourceAddress address, String name, boolean resolveExpression) {
            this.address = address;
            this.name = name;
            this.parameter = new ModelNode();
            this.header = new ModelNode();
            this.roles = new HashSet<>();
            if (resolveExpression) {
                parameter.get(RESOLVE_EXPRESSION).set(resolveExpression);
            }
        }

        /** Adds a boolean parameter. */
        public Builder param(String name, boolean value) {
            parameter.get(name).set(value);
            return this;
        }

        /** Adds an integer parameter. */
        public Builder param(String name, int value) {
            parameter.get(name).set(value);
            return this;
        }

        /** Adds a long parameter. */
        public Builder param(String name, long value) {
            parameter.get(name).set(value);
            return this;
        }

        /** Adds a double parameter. */
        public Builder param(String name, double value) {
            parameter.get(name).set(value);
            return this;
        }

        /** Adds a string parameter. */
        public Builder param(String name, String value) {
            parameter.get(name).set(value);
            return this;
        }

        /** Adds a string array parameter (added as a list). */
        public Builder param(String name, String[] values) {
            for (String value : values) {
                parameter.get(name).add(value);
            }
            return this;
        }

        /** Adds a model node parameter. */
        public Builder param(String name, ModelNode value) {
            parameter.get(name).set(value);
            return this;
        }

        /** Adds a string operation header. */
        public Builder header(String name, String value) {
            header.get(name).set(value);
            return this;
        }

        /** Adds an integer operation header. */
        public Builder header(String name, int value) {
            header.get(name).set(value);
            return this;
        }

        /** Adds a boolean operation header. */
        public Builder header(String name, boolean value) {
            header.get(name).set(value);
            return this;
        }

        /**
         * Uses the specified payload for the operation.
         *
         * @param payload The operation as model node.
         *
         * @return this builder
         */
        public Builder payload(ModelNode payload) {
            parameter = payload;
            return this;
        }

        /**
         * @return builds and returns the operation
         */
        public Operation build() {
            return new Operation(name, address, parameter, header, roles);
        }
    }
}
