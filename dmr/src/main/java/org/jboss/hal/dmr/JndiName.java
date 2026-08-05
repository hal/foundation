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

/**
 * Utility for detecting JNDI names following the {@code java:} URL scheme as defined by the Jakarta EE specification.
 * <p>
 * Valid JNDI names start with the {@code java:} scheme followed by a recognized namespace ({@code comp}, {@code module},
 * {@code app}, {@code global}, {@code jboss}) or a legacy-leading slash. Examples:
 * <ul>
 *     <li>{@code java:jboss/datasources/ExampleDS}</li>
 *     <li>{@code java:global/myApp/myBean}</li>
 *     <li>{@code java:comp/env/jdbc/MyDB}</li>
 *     <li>{@code java:/legacy/name}</li>
 * </ul>
 */
public final class JndiName {

    private static final String JAVA_SCHEME = "java:";
    private static final String[] NAMESPACES = {"comp", "module", "app", "global", "jboss"};

    /**
     * Returns {@code true} if the given string is a JNDI name. A value is considered a JNDI name if it starts with
     * {@code java:} followed by either a leading slash (legacy format) or a recognized namespace.
     */
    public static boolean isJndiName(String value) {
        if (value == null || value.length() <= JAVA_SCHEME.length()) {
            return false;
        }
        if (!value.startsWith(JAVA_SCHEME)) {
            return false;
        }
        String rest = value.substring(JAVA_SCHEME.length());
        if (rest.startsWith("/")) {
            return rest.length() > 1;
        }
        for (String namespace : NAMESPACES) {
            if (rest.equals(namespace) || rest.startsWith(namespace + "/")) {
                return true;
            }
        }
        return false;
    }

    private JndiName() {
    }
}
