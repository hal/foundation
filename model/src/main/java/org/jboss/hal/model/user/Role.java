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
package org.jboss.hal.model.user;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.elemento.Id;

/** A standard or scoped role used when RBAC is turned on. */
public class Role {

    /** Has all permissions except cannot read or write resources related to the administrative audit logging system. */
    public static final Role ADMINISTRATOR = new Role("Administrator");

    /** Can read anything. Can only modify the resources related to the administrative audit logging system. */
    public static final Role AUDITOR = new Role("Auditor");

    /**
     * Like a Maintainer, but with permission to modify persistent configuration constrained to resources that are considered to
     * be "application resources". A deployment is an application resource. The messaging server is not. Items like datasources
     * and JMS destinations are not considered to be application resources by default, but this is configurable.
     */
    public static final Role DEPLOYER = new Role("Deployer");

    /** Operator permissions, plus can modify the persistent configuration. */
    public static final Role MAINTAINER = new Role("Maintainer");

    /** A read-only role. Cannot modify any resource. */
    public static final Role MONITOR = new Role("Monitor");

    /**
     * Monitor permissions, plus can modify runtime state, but cannot modify anything that ends up in the persistent
     * configuration. Could, for example, restart a server.
     */
    public static final Role OPERATOR = new Role("Operator");

    /** Has all permissions. Equivalent to a JBoss AS 7 administrator. */
    public static final Role SUPER_USER = new Role("SuperUser");

    /** The scope type of a role. */
    public enum Type {
        /** A built-in standard role. */
        STANDARD,
        /** A scoped role restricted to specific hosts. */
        HOST,
        /** A scoped role restricted to specific server groups. */
        SERVER_GROUP
    }

    // Lowercase name. Must only be used internally!
    // Is used in equals() and hashCode()
    private final String identifier;
    private final String name;
    private final Type type;
    private final Role baseRole;
    private final SortedSet<String> scope;
    private boolean includeAll;

    /** Creates a standard role with the given name. */
    public Role(String name) {
        this(name, null, Type.STANDARD, Collections.emptySet());
    }

    /** Creates a scoped role with the given base role, type, and scope. */
    public Role(String name, Role baseRole, Type type,
            Iterable<String> scope) {
        this.identifier = name.toLowerCase();
        this.name = name;
        this.baseRole = baseRole;
        this.type = type;
        this.scope = new TreeSet<>();
        if (scope != null) {
            scope.forEach(this.scope::add);
        }
        this.includeAll = false;
    }

    /** Two roles are equal if they have the same identifier. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Role)) {
            return false;
        }

        Role role = (Role) o;
        // noinspection RedundantIfStatement
        if (!identifier.equals(role.identifier)) {
            return false;
        }
        return true;
    }

    /** Returns a hash code based on the role identifier. */
    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    /** @return a string representation of this role. */
    @Override
    public String toString() {
        if (isStandard()) {
            return name;
        }
        return name + " extends " + baseRole.getName() + " scoped to " + type.name()
                .toLowerCase() + scope + ", includeAll: " + includeAll;
    }

    /** @return the unique ID of this role. */
    public String getId() {
        return Id.build(identifier);
    }

    /** @return true if this is a standard role, false otherwise. */
    public boolean isStandard() {
        return type == Type.STANDARD;
    }

    /** @return true if this is a scoped role, false otherwise. */
    public boolean isScoped() {
        return type != Type.STANDARD;
    }

    /** @return the name of this role. */
    public String getName() {
        return name;
    }

    /** @return the base role if this is a scoped role, null otherwise. */
    public Role getBaseRole() {
        return baseRole;
    }

    /** Returns the scope type of this role. */
    public Type getType() {
        return type;
    }

    /** Returns the host or server group names this scoped role applies to. */
    public SortedSet<String> getScope() {
        return scope;
    }

    /** Returns {@code true} if this role includes all users. */
    public boolean isIncludeAll() {
        return includeAll;
    }

    /** Sets whether this role includes all users. */
    public void setIncludeAll(boolean includeAll) {
        this.includeAll = includeAll;
    }
}
