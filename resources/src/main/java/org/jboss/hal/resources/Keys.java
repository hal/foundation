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

    // ------------------------------------------------------ keys (a-z)

    /**
     * {@code org.jboss.hal.ui.filter.AccessTypeValue} enum stored in filter menu items to tag each option with an access-type
     * filter value.
     */
    String ACCESS_TYPE_VALUE = "access-type-value";

    /**
     * {@code List<org.jboss.hal.meta.AddressTemplate>} carrying resolved address templates through a flow context during
     * capability lookup.
     */
    String ADDRESS_TEMPLATES = "address-templates";

    /**
     * {@code org.jboss.hal.meta.description.AttributeDescription} stored in model browser table rows to associate each row with
     * its attribute metadata.
     */
    String ATTRIBUTE_DESCRIPTION = "attribute-description";

    /**
     * {@code org.jboss.hal.op.endpoint.Endpoint} stored in endpoint selector cards during bootstrap endpoint selection.
     */
    String ENDPOINT = "endpoint";

    /**
     * {@code org.jboss.hal.meta.AddressTemplate} stored in each finder item to hold the resolved address template for that
     * resource.
     */
    String FINDER_TEMPLATE = "template";

    /**
     * {@code List<String>} of host names set during bootstrap by {@code org.jboss.hal.op.bootstrap.ReadHostNames}, consumed by
     * {@code org.jboss.hal.op.bootstrap.FindDomainController}.
     */
    String HOSTS = "hosts";

    /**
     * {@code org.jboss.hal.meta.Metadata} stored in wizard context during the add-resource dialog to pass fetched metadata
     * between wizard steps.
     */
    String METADATA = "metadata";

    /**
     * {@code org.jboss.hal.ui.modelbrowser.ModelBrowserNode} stored in tree-view and list items to associate each UI node with
     * its model browser domain object.
     */
    String MODEL_BROWSER_NODE = "model-browser-node";

    /**
     * {@code org.jboss.hal.dmr.ModelNode} stored in wizard context during the add-resource dialog to hold the DMR payload built
     * from form input.
     */
    String MODEL_NODE = "modelNode";

    /**
     * {@code org.jboss.hal.core.Notification} stored in notification list items for rendering the notification drawer.
     */
    String NOTIFICATION = "notification";

    /**
     * {@code String} timestamp stored alongside each notification item for sorting and filtering. String type avoids cast
     * issues.
     */
    String NOTIFICATION_TIMESTAMP = "notification-timestamp";

    /**
     * {@code org.jboss.hal.meta.description.OperationDescription} stored in model browser table rows to associate each row with
     * its operation metadata.
     */
    String OPERATION_DESCRIPTION = "operation-description";

    /**
     * {@code List<String>} of provider-point strings carried through a flow context when resolving WildFly capabilities.
     */
    String PROVIDER_POINTS = "provider-points";

    /**
     * {@code org.jboss.hal.ui.resource.ResourceAttribute} stored in form items and view items to associate each UI element with
     * its resource attribute.
     */
    String RESOURCE_ATTRIBUTE = "resource-attribute";

    /**
     * {@code org.jboss.hal.ui.resource.form.ResourceForm} stored in wizard context during the add-resource dialog to pass the
     * form between wizard steps.
     */
    String RESOURCE_FORM = "resourceForm";

    /**
     * {@code String} resource name stored in each finder item to hold the resource's display name.
     */
    String RESOURCE_NAME = "resource-name";

    /**
     * {@code org.jboss.hal.ui.filter.StorageValue} enum stored in filter menu items to tag each option with a storage filter
     * value.
     */
    String STORAGE_VALUE = "storage-value";

    /**
     * {@code org.jboss.hal.ui.filter.TypeValues} enum stored in filter menu items to tag each option with an attribute-type
     * filter value.
     */
    String TYPE_VALUES = "type-values";
}
