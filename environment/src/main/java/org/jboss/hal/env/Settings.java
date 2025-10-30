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
package org.jboss.hal.env;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.elemento.Id;
import org.jboss.hal.resources.Ids;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

@ApplicationScoped
public class Settings {

    public static final String DEFAULT_LOCALE = "en";
    private static final int EXPIRES = 365; // days

    private final Map<Key, Value> values;

    public Settings() {
        values = new EnumMap<>(Key.class);
    }

    public <T> void load(Key key, T defaultValue) {
        String value = Cookies.get(cookieName(key));
        if (value == null) {
            if (defaultValue != null) {
                value = String.valueOf(defaultValue);
            }
        }
        if (value != null) {
            values.put(key, new Value(value));
        }
    }

    public Value get(Key key) {
        return values.getOrDefault(key, Value.EMPTY);
    }

    public <T> void set(Key key, T value) {
        values.put(key, new Value(value != null ? String.valueOf(value) : null));
        if (value == null) {
            Cookies.remove(cookieName(key));
        } else {
            if (key.persistent) {
                Cookies.set(cookieName(key), String.valueOf(value), EXPIRES);
            } else {
                Cookies.set(cookieName(key), String.valueOf(value));
            }
        }
    }

    @Override
    public String toString() {
        return values.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(joining(", "));
    }

    private String cookieName(Key key) {
        return Id.build(Ids.COOKIE, key.key);
    }

    public enum Key {
        TITLE("title", true),

        LOCALE("locale", true),

        SHOW_GLOBAL_OPERATIONS("show-global-operations", true),

        RUN_AS("run-as", false), // can contain multiple roles separated by ","

        UNDEFINED("undefined", false);

        public static Key from(String key) {
            return switch (key) {
                case "title" -> TITLE;
                case "locale" -> LOCALE;
                case "show-global-operations" -> SHOW_GLOBAL_OPERATIONS;
                case "run-as" -> RUN_AS;
                default -> UNDEFINED;
            };
        }

        private final String key;
        private final boolean persistent;

        Key(String key, boolean persistent) {
            this.key = key;
            this.persistent = persistent;
        }

        public String key() {
            return key;
        }
    }

    public static class Value {

        private static final Value EMPTY = new Value("undefined");
        private static final String SEPARATOR_REGEX = "\\|";

        private final String value;

        private Value(String value) {
            this.value = value;
        }

        public boolean asBoolean() {
            return Boolean.parseBoolean(value);
        }

        public int asInt(int defaultValue) {
            if (value != null) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        public Set<String> asSet() {
            if (value != null) {
                String[] values = value.split(SEPARATOR_REGEX);
                return new HashSet<>(asList(values));
            }
            return Collections.emptySet();
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
