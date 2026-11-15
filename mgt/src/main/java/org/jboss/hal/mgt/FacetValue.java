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
package org.jboss.hal.mgt;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public sealed interface FacetValue permits FacetValue.Bool, FacetValue.Multi, FacetValue.Text {

    String toQueryParam();

    record Bool(boolean value) implements FacetValue {
        @Override
        public String toQueryParam() {
            return String.valueOf(value);
        }
    }

    record Multi(Set<String> values) implements FacetValue {
        public Multi {
            values = Collections.unmodifiableSet(new LinkedHashSet<>(values));
        }

        @Override
        public String toQueryParam() {
            return String.join(",", values);
        }
    }

    record Text(String text) implements FacetValue {
        @Override
        public String toQueryParam() {
            return "contains:" + text;
        }
    }
}
