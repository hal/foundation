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
 * Static OUIA IDs used for QA element identification. These IDs follow the {@code hal-op-<component>} naming convention and are
 * synchronized with the dave test suite.
 * <p>
 * Use {@link #ouia(String, String...)} to compose dynamic OUIA IDs from {@link OuiaContexts} and {@link OuiaSuffixes}.
 *
 * @see OuiaContexts
 * @see OuiaSuffixes
 */
public interface OuiaIds {

    String BOOTSTRAP_SELECT_BTN = "hal-op-bootstrap-select-btn";
    String DASHBOARD_DEPLOYMENT_CARD = "hal-op-dashboard-deployment-card";
    String DASHBOARD_DOCUMENTATION_CARD = "hal-op-dashboard-documentation-card";
    String DASHBOARD_HEALTH_CARD = "hal-op-dashboard-health-card";
    String DASHBOARD_LOG_CARD = "hal-op-dashboard-log-card";
    String DASHBOARD_OVERVIEW_CARD = "hal-op-dashboard-overview-card";
    String DASHBOARD_RUNTIME_CARD = "hal-op-dashboard-runtime-card";
    String DASHBOARD_STATUS_CARD = "hal-op-dashboard-status-card";
    String ENDPOINT_ADD_BTN = "hal-op-endpoint-add-btn";
    String ENDPOINT_CANCEL_BTN = "hal-op-endpoint-cancel-btn";
    String ENDPOINT_CONNECT_BTN = "hal-op-endpoint-connect-btn";
    String ENDPOINT_MODAL = "hal-op-endpoint-modal";
    String ENDPOINT_PING_BTN = "hal-op-endpoint-ping-btn";
    String ENDPOINT_SELECT_BTN = "hal-op-endpoint-select-btn";
    String ENDPOINT_TABLE_ADD_BTN = "hal-op-endpoint-table-add-btn";
    String EXPRESSION_CANCEL_BTN = "hal-op-expression-cancel-btn";
    String EXPRESSION_MODAL = "hal-op-expression-modal";
    String EXPRESSION_OK_BTN = "hal-op-expression-ok-btn";
    String FIND_RESOURCE_CANCEL_BTN = "hal-op-find-resource-cancel-btn";
    String FIND_RESOURCE_MODAL = "hal-op-find-resource-modal";
    String FIND_RESOURCE_SEARCH_BTN = "hal-op-find-resource-search-btn";
    String LOG_CHOOSE_BTN = "hal-op-log-choose-btn";
    String LOG_SHOW_BTN = "hal-op-log-show-btn";
    String MASTHEAD = "hal-op-masthead";
    String MASTHEAD_LOGO = "hal-op-masthead-logo";
    String MASTHEAD_TOOLBAR = "hal-op-masthead-toolbar";
    String MODEL_BROWSER_ATTRIBUTES_FILTER = "hal-op-model-browser-attributes-filter";
    String MODEL_BROWSER_BACK_BTN = "hal-op-model-browser-back-btn";
    String MODEL_BROWSER_BREADCRUMB = "hal-op-model-browser-breadcrumb";
    String MODEL_BROWSER_COLLAPSE_BTN = "hal-op-model-browser-collapse-btn";
    String MODEL_BROWSER_FIND_BTN = "hal-op-model-browser-find-btn";
    String MODEL_BROWSER_FORWARD_BTN = "hal-op-model-browser-forward-btn";
    String MODEL_BROWSER_GLOBAL_OPS_SWITCH = "hal-op-model-browser-global-ops-switch";
    String MODEL_BROWSER_HOME_BTN = "hal-op-model-browser-home-btn";
    String MODEL_BROWSER_OPERATIONS_FILTER = "hal-op-model-browser-operations-filter";
    String MODEL_BROWSER_REFRESH_BTN = "hal-op-model-browser-refresh-btn";
    String MODEL_BROWSER_RESOURCE_HEADING = "hal-op-model-browser-resource-heading";
    String MODEL_BROWSER_TAB_ATTRIBUTES = "hal-op-model-browser-tab-attributes";
    String MODEL_BROWSER_TAB_CAPABILITIES = "hal-op-model-browser-tab-capabilities";
    String MODEL_BROWSER_TAB_DATA = "hal-op-model-browser-tab-data";
    String MODEL_BROWSER_TAB_OPERATIONS = "hal-op-model-browser-tab-operations";
    String MODEL_BROWSER_TABS = "hal-op-model-browser-tabs";
    String MODEL_BROWSER_TREE = "hal-op-model-browser-tree";
    String NAV = "hal-op-nav";
    String NAV_CONFIGURATION = "hal-op-nav-configuration";
    String NAV_DASHBOARD = "hal-op-nav-dashboard";
    String NAV_DEPLOYMENTS = "hal-op-nav-deployments";
    String NAV_MODEL_BROWSER = "hal-op-nav-model-browser";
    String NAV_RUNTIME = "hal-op-nav-runtime";
    String NAV_TASKS = "hal-op-nav-tasks";
    String NOTIFICATION_CLEAR_ALL = "hal-op-notification-clear-all";
    String NOTIFICATION_MARK_ALL_READ = "hal-op-notification-mark-all-read";
    String NOTIFICATION_UNCLEAR_LAST = "hal-op-notification-unclear-last";
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
    String STABILITY_DISMISS_BTN = "hal-op-stability-dismiss-btn";

    /**
     * Composes an OUIA ID with the {@code hal-op} prefix.
     *
     * @param first the first segment after the prefix (typically from {@link OuiaContexts})
     * @param rest  additional segments (typically from {@link OuiaSuffixes})
     * @return a composite OUIA ID like {@code hal-op-<first>-<rest[0]>-<rest[1]>-...}
     */
    static String ouia(String first, String... rest) {
        String[] parts = new String[1 + rest.length];
        parts[0] = first;
        System.arraycopy(rest, 0, parts, 1, rest.length);
        return Id.build("hal-op", parts);
    }
}
