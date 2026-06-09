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

import java.util.HashSet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * CDI producer that creates the default {@link Current} {@link User} instance, updated during bootstrap with actual credentials.
 */
public class CurrentUser {

    /** Produces the application-scoped current user instance. Updated during bootstrap with actual credentials. */
    @Produces
    @Current
    @ApplicationScoped
    public User currentUser() {
        return new User("Unknown", new HashSet<>(), false);
    }
}
