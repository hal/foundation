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

/**
 * The build type of the console (development or production)
 */
public enum BuildType {

    /** Development build with unoptimized J2CL output and source maps. */
    DEVELOPMENT,

    /** Production build with advanced J2CL optimizations. */
    PRODUCTION,

    /** Test suite build used for integration testing in a container environment. */
    TEST_SUITE;

    /**
     * Parses a build type from its string name, returning the given default if the value is {@code null} or unrecognized.
     * Hyphens in the input are replaced with underscores before matching.
     */
    public static BuildType parse(String value, BuildType defaultValue) {
        BuildType build = defaultValue;
        if (value != null) {
            try {
                build = BuildType.valueOf(value.toUpperCase().replace('-', '_'));
            } catch (IllegalArgumentException ignore) {
                // ignore
            }
        }
        return build;
    }
}
