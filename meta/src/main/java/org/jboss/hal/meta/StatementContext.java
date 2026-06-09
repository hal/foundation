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
package org.jboss.hal.meta;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.env.Environment;

/**
 * Holds the current values for {@link Placeholder placeholders} used in {@link AddressTemplate address templates}.
 * Populated during navigation and used to resolve templates into concrete DMR addresses.
 */
@ApplicationScoped
public class StatementContext {

    final Environment environment;
    private final Map<Placeholder, String> values;
    private final Map<String, Placeholder> placeholders;

    @Inject
    public StatementContext(Environment environment) {
        this.environment = environment;
        this.values = new HashMap<>();
        this.placeholders = new HashMap<>();
    }

    /** @return whether the console is connected to a standalone server */
    public boolean standalone() {
        return environment.standalone();
    }

    /** @return whether the console is connected to a domain controller */
    public boolean domain() {
        return environment.domain();
    }

    /** Assigns a value to the placeholder identified by its name. */
    public void assign(String placeholder, String value) {
        assign(new Placeholder(placeholder, null, false), value);
    }

    /** Assigns a value to the given placeholder. */
    public void assign(Placeholder placeholder, String value) {
        values.put(placeholder, value);
        placeholders.put(placeholder.name, placeholder);
    }

    /** @return the placeholder instance registered under the given name, or {@code null} */
    public Placeholder placeholder(String placeholder) {
        return placeholders.get(placeholder);
    }

    /** @return the current value for the placeholder with the given name, or {@code null} */
    public String value(String placeholder) {
        return value(placeholder(placeholder));
    }

    /** @return the current value for the given placeholder, or {@code null} */
    public String value(Placeholder placeholder) {
        if (placeholder != null) {
            return values.get(placeholder);
        }
        return null;
    }
}
