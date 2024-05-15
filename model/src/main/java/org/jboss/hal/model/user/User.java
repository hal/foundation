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

import java.util.Set;

import static org.jboss.hal.model.user.Role.ADMINISTRATOR;
import static org.jboss.hal.model.user.Role.SUPER_USER;

/** Holds information about an user. */
public class User {

    private final Set<Role> roles;
    private String name;
    private boolean authenticated;

    User(String name, Set<Role> roles, boolean authenticated) {
        this.name = name;
        this.roles = roles;
        this.authenticated = authenticated;
    }

    @Override
    public String toString() {
        return "User(" + name + ", roles=" + roles + ')';
    }

    /** @return the user name. */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void refreshRoles(Set<Role> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    /** @return true if this user belongs to the role SuperUser, false otherwise. */
    public boolean isSuperuser() {
        if (roles.contains(SUPER_USER)) {
            return true;
        } else {
            return false;
        }
    }

    /** @return true if this user belongs to the role Administrator, false otherwise. */
    public boolean isAdministrator() {
        if (roles.contains(ADMINISTRATOR)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
