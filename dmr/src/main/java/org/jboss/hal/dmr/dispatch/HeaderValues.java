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
package org.jboss.hal.dmr.dispatch;

/** Constants for HTTP header values used in DMR endpoint communication. */
interface HeaderValues {

    /** MIME type for base64-encoded DMR payloads. */
    String APPLICATION_DMR_ENCODED = "application/dmr-encoded";

    /** Value sent in the {@code X-Management-Client-Name} header to identify the HAL console. */
    String HEADER_MANAGEMENT_CLIENT_VALUE = "HAL";
}
