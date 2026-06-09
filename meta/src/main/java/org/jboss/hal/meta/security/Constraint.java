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

import org.jboss.hal.meta.AddressTemplate;

import elemental2.core.JsRegExp;
import elemental2.core.RegExpResult;

import static org.jboss.hal.meta.security.Target.ATTRIBUTE;
import static org.jboss.hal.meta.security.Target.OPERATION;

/** A constraint for an attribute or operation of a DMR resource. */
public class Constraint {

    /** Creates a constraint requiring execute permission for the given operation on the specified resource. */
    public static Constraint executable(AddressTemplate template, String operation) {
        return new Constraint(template, operation, OPERATION, Permission.EXECUTABLE);
    }

    /** Creates a constraint requiring write permission for the given attribute on the specified resource. */
    public static Constraint writable(AddressTemplate template, String attribute) {
        return new Constraint(template, attribute, ATTRIBUTE, Permission.WRITABLE);
    }

    /**
     * Parses a constraint from its string representation (e.g., {@code "writable(subsystem=datasources@enabled)"}).
     *
     * @throws IllegalArgumentException if the input does not match the expected format
     */
    @SuppressWarnings("DuplicateStringLiteralInspection")
    public static Constraint parse(String input) throws IllegalArgumentException {
        if (!CONSTRAINT_REGEX.test(input)) {
            throw new IllegalArgumentException("Invalid constraint: " + input);
        }
        RegExpResult result = CONSTRAINT_REGEX.exec(input);
        if (result.length != 5) {
            throw new IllegalArgumentException("Invalid constraint: " + input);
        }
        return new Constraint(AddressTemplate.ofTrusted(result.at(2)), result.at(4),
                Target.parse(result.at(3)), Permission.valueOf(result.at(1).toUpperCase()));
    }

    private static final JsRegExp CONSTRAINT_REGEX = new JsRegExp(
            "^(readable|writable|executable)\\(([\\w{}=*\\-\\/\\.]+)(:|@)([\\w\\-]+)\\)$"); //NON-NLS

    /** The address template of the constrained resource. */
    public final AddressTemplate template;

    /** Whether the constraint applies to an attribute or an operation. */
    public final Target target;

    /** The name of the constrained attribute or operation. */
    public final String name;

    /** The required permission level. */
    public final Permission permission;

    private Constraint(AddressTemplate template, String name, Target target, Permission permission) {
        this.template = template;
        this.target = target;
        this.name = name;
        this.permission = permission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Constraint)) {
            return false;
        }

        Constraint that = (Constraint) o;
        if (!template.equals(that.template)) {
            return false;
        }
        if (target != that.target) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        return permission == that.permission;
    }

    @Override
    public int hashCode() {
        int result = template.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + permission.hashCode();
        return result;
    }

    @Override
    public String toString() {
        // Do NOT change the format, Constraint.parseSingle() relies on it!
        return permission.name().toLowerCase() + "(" + template + target.symbol + name + ")";
    }

    /** @return the string representation suitable for use in HTML {@code data-constraint} attributes */
    public String data() {
        return toString();
    }
}
