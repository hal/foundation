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

    // ------------------------------------------------------ OUIA IDs - masthead (a-z)

    String MASTHEAD = "hal-op-masthead";
    String MASTHEAD_LOGO = "hal-op-masthead-logo";
    String MASTHEAD_TOOLBAR = "hal-op-masthead-toolbar";

    // ------------------------------------------------------ OUIA IDs - navigation (a-z)

    String NAV = "hal-op-nav";
    String NAV_CONFIGURATION = "hal-op-nav-configuration";
    String NAV_DASHBOARD = "hal-op-nav-dashboard";
    String NAV_DEPLOYMENTS = "hal-op-nav-deployments";
    String NAV_MODEL_BROWSER = "hal-op-nav-model-browser";
    String NAV_RUNTIME = "hal-op-nav-runtime";
    String NAV_TASKS = "hal-op-nav-tasks";

    // ------------------------------------------------------ OUIA IDs - pages (a-z)

    String PAGE_CONFIGURATION = "hal-op-page-configuration";
    String PAGE_DASHBOARD = "hal-op-page-dashboard";
    String PAGE_DASHBOARD_HEADER = "hal-op-page-dashboard-header";
    String PAGE_DEPLOYMENTS = "hal-op-page-deployments";
    String PAGE_ERROR = "hal-op-page-error";
    String PAGE_MODEL_BROWSER = "hal-op-page-model-browser";
    String PAGE_NO_DATA = "hal-op-page-no-data";
    String PAGE_NOT_FOUND = "hal-op-page-not-found";
    String PAGE_RUNTIME = "hal-op-page-runtime";
    String PAGE_TASKS = "hal-op-page-tasks";
    String PAGE_TASKS_HEADER = "hal-op-page-tasks-header";

    // ------------------------------------------------------ OUIA IDs - bootstrap (a-z)

    String BOOTSTRAP_SELECT_BTN = "hal-op-bootstrap-select-btn";

    // ------------------------------------------------------ OUIA IDs - endpoint (a-z)

    String ENDPOINT_ADD_BTN = "hal-op-endpoint-add-btn";
    String ENDPOINT_CANCEL_BTN = "hal-op-endpoint-cancel-btn";
    String ENDPOINT_CONNECT_BTN = "hal-op-endpoint-connect-btn";
    String ENDPOINT_MODAL = "hal-op-endpoint-modal";
    String ENDPOINT_PING_BTN = "hal-op-endpoint-ping-btn";
    String ENDPOINT_SELECT_BTN = "hal-op-endpoint-select-btn";
    String ENDPOINT_TABLE_ADD_BTN = "hal-op-endpoint-table-add-btn";

    // ------------------------------------------------------ OUIA IDs - expression (a-z)

    String EXPRESSION_CANCEL_BTN = "hal-op-expression-cancel-btn";
    String EXPRESSION_MODAL = "hal-op-expression-modal";
    String EXPRESSION_OK_BTN = "hal-op-expression-ok-btn";

    // ------------------------------------------------------ OUIA IDs - find resource (a-z)

    String FIND_RESOURCE_CANCEL_BTN = "hal-op-find-resource-cancel-btn";
    String FIND_RESOURCE_MODAL = "hal-op-find-resource-modal";
    String FIND_RESOURCE_SEARCH_BTN = "hal-op-find-resource-search-btn";

    // ------------------------------------------------------ OUIA IDs - log (a-z)

    String LOG_CHOOSE_BTN = "hal-op-log-choose-btn";
    String LOG_SHOW_BTN = "hal-op-log-show-btn";

    // ------------------------------------------------------ OUIA IDs - model browser (a-z)

    String MODEL_BROWSER_BACK_BTN = "hal-op-model-browser-back-btn";
    String MODEL_BROWSER_COLLAPSE_BTN = "hal-op-model-browser-collapse-btn";
    String MODEL_BROWSER_FIND_BTN = "hal-op-model-browser-find-btn";
    String MODEL_BROWSER_FORWARD_BTN = "hal-op-model-browser-forward-btn";
    String MODEL_BROWSER_HOME_BTN = "hal-op-model-browser-home-btn";
    String MODEL_BROWSER_REFRESH_BTN = "hal-op-model-browser-refresh-btn";

    // ------------------------------------------------------ OUIA IDs - notification (a-z)

    String NOTIFICATION_CLEAR_ALL = "hal-op-notification-clear-all";
    String NOTIFICATION_MARK_ALL_READ = "hal-op-notification-mark-all-read";
    String NOTIFICATION_UNCLEAR_LAST = "hal-op-notification-unclear-last";

    // ------------------------------------------------------ OUIA IDs - stability (a-z)

    String STABILITY_DISMISS_BTN = "hal-op-stability-dismiss-btn";

    // ------------------------------------------------------ OUIA suffixes for dynamic composition (a-z)

    String _ADD = "add";
    String _BTN = "btn";
    String _CANCEL = "cancel";
    String _CLOSE = "close";
    String _CONNECT = "connect";
    String _DELETE = "delete";
    String _EDIT = "edit";
    String _EXECUTE = "execute";
    String _MODAL = "modal";
    String _OK = "ok";
    String _REFRESH = "refresh";
    String _RESET = "reset";
    String _SAVE = "save";
    String _SEARCH = "search";

    // ------------------------------------------------------ static IDs (a-z)

    String COOKIE = "hal-cookie";
    String MAIN_ID = "hal-main-id";
    String STANDALONE_HOST = "hal-standalone-host";
    String STANDALONE_SERVER = "hal-standalone-server";

    // ------------------------------------------------------ dynamic IDs (a-z)

    /**
     * Generates a unique HTML ID for a host-server pair.
     *
     * @param host   the host name
     * @param server the server name
     * @return a composite ID combining the host and server names
     */
    static String hostServer(String host, String server) {
        return Id.build(host, server);
    }

    /**
     * Composes an OUIA ID with the {@code hal-op} prefix.
     *
     * @param first the first segment after the prefix
     * @param rest  additional segments
     * @return a composite OUIA ID like {@code hal-op-<first>-<rest[0]>-<rest[1]>-...}
     */
    static String ouia(String first, String... rest) {
        String[] parts = new String[1 + rest.length];
        parts[0] = first;
        System.arraycopy(rest, 0, parts, 1, rest.length);
        return Id.build("hal-op", parts);
    }
}
