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
package org.jboss.hal.meta.security;

import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ATTRIBUTES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.EXECUTE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.OPERATIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WRITE;

/** Represents the RBAC related payload from the read-resource-description operation. */
public class SecurityContext extends ModelNode {

    /** A security context with hardcoded permissions to read resources, write and execute operations are not allowed. */
    public static final SecurityContext READ_ONLY = new SecurityContext(new ModelNode()) {
        @Override
        public boolean readable() {
            return true;
        }

        @Override
        public boolean writable() {
            return false;
        }

        @Override
        public boolean readable(String attribute) {
            return true;
        }

        @Override
        public boolean writable(String attribute) {
            return false;
        }

        @Override
        public boolean executable(String operation) {
            return false;
        }
    };

    /** A security context with hardcoded permissions to read, write and execute any resource. */
    public static final SecurityContext RWX = new SecurityContext(new ModelNode()) {
        @Override
        public boolean readable() {
            return true;
        }

        @Override
        public boolean writable() {
            return true;
        }

        @Override
        public boolean readable(String attribute) {
            return true;
        }

        @Override
        public boolean writable(String attribute) {
            return true;
        }

        @Override
        public boolean executable(String operation) {
            return true;
        }
    };

    public SecurityContext() {
        super();
    }

    public SecurityContext(ModelNode payload) {
        set(payload);
    }

    /** @return whether the security context is readable */
    public boolean readable() {
        return hasDefined(READ) && get(READ).asBoolean();
    }

    /** @return whether the security context is writable */
    public boolean writable() {
        return hasDefined(WRITE) && get(WRITE).asBoolean();
    }

    /**
     * @param attribute The attribute to check.
     * @return whether the attribute is readable
     */
    public boolean readable(String attribute) {
        return hasDefined(ATTRIBUTES) &&
                get(ATTRIBUTES).hasDefined(attribute) &&
                get(ATTRIBUTES).get(attribute).get(READ).asBoolean();
    }

    /**
     * @param attribute The attribute to check.
     * @return whether the attribute is writable
     */
    public boolean writable(String attribute) {
        return hasDefined(ATTRIBUTES) &&
                get(ATTRIBUTES).hasDefined(attribute) &&
                get(ATTRIBUTES).get(attribute).get(WRITE).asBoolean();
    }

    /**
     * @param operation The operation to check.
     * @return whether the operation is executable
     */
    public boolean executable(String operation) {
        return hasDefined(OPERATIONS) &&
                get(OPERATIONS).hasDefined(operation) &&
                get(OPERATIONS).get(operation).get(EXECUTE).asBoolean();
    }
}
