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
package org.jboss.hal.resources;

import org.jboss.elemento.Id;

/**
 * IDs used in HTML elements across multiple classes. Please add IDs to this interface even if there's already an equivalent or
 * similar constant in {@code ModelDescriptionConstants} (SoC).
 * <p>
 * The IDs defined here are reused by QA. So please make sure that IDs are not spread over the code base but gathered in this
 * interface. This is not always possible - if the ID contains dynamic parts like a resource name or selected server. But IDs
 * which only contain static strings should be part of this interface.
 */
public interface Ids {

    // ------------------------------------------------------ static IDs (a-z)

    String COOKIE = "hal-cookie";
    String MAIN_ID = "hal-main-id";
    String STANDALONE_HOST = "hal-standalone-host";
    String STANDALONE_SERVER = "hal-standalone-server";

    // ------------------------------------------------------ dynamic IDs (a-z)

    static String hostServer(String host, String server) {
        return Id.build(host, server);
    }
}
