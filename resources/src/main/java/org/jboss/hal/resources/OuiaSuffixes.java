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

/**
 * Suffix constants for dynamic OUIA ID composition. Used as arguments to {@link OuiaIds#ouia(String, String...)}.
 *
 * @see OuiaIds
 * @see OuiaContexts
 */
public interface OuiaSuffixes {

    String ADD = "add";
    String BTN = "btn";
    String CANCEL = "cancel";
    String CLOSE = "close";
    String CONNECT = "connect";
    String DELETE = "delete";
    String EDIT = "edit";
    String EXECUTE = "execute";
    String MODAL = "modal";
    String OK = "ok";
    String REFRESH = "refresh";
    String RESET = "reset";
    String SAVE = "save";
    String SEARCH = "search";
}
