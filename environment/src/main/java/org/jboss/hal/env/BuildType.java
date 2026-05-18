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

    DEVELOPMENT,

    PRODUCTION,

    TEST_SUITE;

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
