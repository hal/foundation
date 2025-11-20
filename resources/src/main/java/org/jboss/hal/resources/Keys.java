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
 * Keys used to store data in map-like structures like {@code org.patternfly.core.ComponentContext} and
 * {@code org.jboss.elemento.flow.FlowContext}.
 */
public interface Keys {

    // ------------------------------------------------------ IDs (a-z)

    /** org.jboss.hal.ui.filter.AccessTypeValue */
    String ACCESS_TYPE_VALUE = "access-type-value";

    /** List<AddressTemplate> */
    String ADDRESS_TEMPLATES = "address-templates";

    /** org.jboss.hal.meta.description.AttributeDescription */
    String ATTRIBUTE_DESCRIPTION = "attribute-description";

    /** org.jboss.hal.op.endpoint.Endpoint */
    String ENDPOINT = "endpoint";

    /** List<String> */
    String HOSTS = "hosts";

    /** org.jboss.hal.ui.modelbrowser.ModelBrowserNode */
    String MODEL_BROWSER_NODE = "model-browser-node";

    /** org.jboss.hal.core.Notification */
    String NOTIFICATION = "notification";

    /** String (not double to prevent cast issues when reading from the context) */
    String NOTIFICATION_TIMESTAMP = "notification-timestamp";

    /** org.jboss.hal.meta.description.OperationDescription */
    String OPERATION_DESCRIPTION = "operation-description";

    /** List<String> */
    String PROVIDER_POINTS = "provider-points";

    /** org.jboss.hal.ui.resource.ResourceAttribute */
    String RESOURCE_ATTRIBUTE = "resource-attribute";

    /** org.jboss.hal.ui.filter.StorageValue */
    String STORAGE_VALUE = "storage-value";

    /** org.jboss.hal.ui.filter.TypeValues */
    String TYPE_VALUES = "type-values";
}
