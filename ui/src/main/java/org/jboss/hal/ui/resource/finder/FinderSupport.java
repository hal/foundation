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
package org.jboss.hal.ui.resource.finder;

import java.util.function.Function;

import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.WildcardResolver;
import org.patternfly.component.AsyncItems;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.FinderItem;
import org.patternfly.extension.finder.FinderPath;
import org.patternfly.extension.finder.FinderPreview;
import org.patternfly.extension.finder.PreviewHandler;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.meta.WildcardResolver.Direction.LTR;
import static org.jboss.hal.ui.UIContext.uic;

/**
 * Utility for building finder-style navigation paths from management resource addresses.
 * <p>
 * Provides helper methods for loading child resources asynchronously and creating metadata-based previews for finder columns.
 * Integrates with PatternFly's finder extension to provide consistent navigation patterns.
 */
public class FinderSupport {

    /** Component context key under which the resource name ({@link String}) is stored in each finder item. */
    public static final String RESOURCE_NAME_KEY = "resource-name";

    /** Component context key under which the resolved {@link AddressTemplate} is stored in each finder item. */
    public static final String TEMPLATE_KEY = "template";

    /**
     * Callback for building a metadata-driven preview panel for a finder item.
     */
    @FunctionalInterface
    public interface MetadataPreviewBuilder {

        /**
         * Populates the preview panel with content derived from the resource metadata.
         *
         * @param name     the resource name of the selected finder item
         * @param metadata the resolved management model metadata for the resource
         * @param preview  the empty preview panel to populate
         */
        void onPreview(String name, Metadata metadata, FinderPreview preview);
    }

    /**
     * Retrieves child resources using a {@value ModelDescriptionConstants#READ_CHILDREN_NAMES_OPERATION} operation and maps
     * them into finder items.
     * <p>
     * The {@code templateFn} gets the current {@link FinderPath} as parameter and must return a wildcard address template like
     * {@code subsystem=datasources/data-source=*}. This address template is used for the
     * {@value ModelDescriptionConstants#READ_CHILDREN_NAMES_OPERATION} operation to read the child resources.
     * <p>
     * The items are built using {@code itemFn}. In addition, the following key/values are stored in the finder item's
     * {@linkplain org.patternfly.core.ComponentContext component context}:
     * <ul>
     *     <li>{@link #RESOURCE_NAME_KEY}: The resource name ({@link String})</li>
     *     <li>{@link #TEMPLATE_KEY}: The address template of the item ({@link AddressTemplate})</li>
     * </ul>
     *
     * @param templateFn A function that takes a {@link FinderPath} as input and produces an {@link AddressTemplate}. The
     *                   template is used to determine the address of the resource whose children are being retrieved.
     * @param itemFn     A function that takes a {@link ModelNode} representing a resource and maps it into a
     *                   {@link FinderItem}. This function is used to customize the properties of the items displayed in the
     *                   UI.
     * @return A function that takes a {@link FinderColumn} as input and returns an {@link AsyncItems} for asynchronously
     * loading the child resources. The items are populated and returned as a collection of {@link FinderItem}.
     */
    public static AsyncItems<FinderColumn, FinderItem> childResources(
            Function<FinderPath, AddressTemplate> templateFn,
            Function<ModelNode, FinderItem> itemFn) {
        return column -> {
            AddressTemplate template = templateFn.apply(column.finder().path());
            if (template != null) {
                if ("*".equals(template.last().value)) {
                    String resource = template.last().key;
                    // parent(), because we read the children of `resource`!
                    ResourceAddress address = template.parent().resolve(uic().statementContext());
                    Operation operation = new Operation.Builder(address, READ_CHILDREN_NAMES_OPERATION)
                            .param(CHILD_TYPE, resource)
                            .param(INCLUDE_SINGLETONS, true)
                            .build();
                    return uic().dispatcher().execute(operation).then(result ->
                            Promise.resolve(result.asList().stream()
                                    .map(node -> itemFn.apply(node)
                                            .store(RESOURCE_NAME_KEY, node.asString())
                                            .store(TEMPLATE_KEY, new WildcardResolver(LTR, node.asString()).resolve(template)))
                                    .collect(toList())));
                } else {
                    return Promise.reject("Unable to read child resources: No wildcard in " + template);
                }
            } else {
                return Promise.reject("Unable to read child resources: No template found for " + column.finder().path());
            }
        };
    }

    /**
     * Creates a {@link PreviewHandler} that generates a metadata preview for a specific resource item.
     *
     * @param previewBuilder A callback interface used to construct and handle the preview logic. The {@code onPreview()} method
     *                       of the builder is invoked with the resource name, its metadata, and an empty preview element.
     * @return A configured {@link PreviewHandler} that resolves the metadata for a resource and passes it to the specified
     * {@link MetadataPreviewBuilder} for preview generation.
     */
    public static PreviewHandler metadataPreview(MetadataPreviewBuilder previewBuilder) {
        return (item, preview) -> {
            String name = item.get(RESOURCE_NAME_KEY);
            AddressTemplate template = item.get(TEMPLATE_KEY);
            uic().metadataRepository().lookup(template).then(metadata -> {
                previewBuilder.onPreview(name, metadata, preview);
                return null;
            });
        };
    }

    /**
     * Constructs a route string based on the provided base route and the FinderItem's address template. If the FinderItem
     * contains an address template (retrieved using the {@code TEMPLATE_KEY}), the method combines the base route with the
     * encoded address template. Otherwise, it returns the base route unchanged.
     *
     * @param base The base route used as the starting point for the route (must start and end with a forward slash).
     * @param item The FinderItem containing metadata, including the address template used for constructing the route.
     * @return A string representing the constructed route. If the FinderItem has a valid address template, the base route is
     * combined with the encoded template. Otherwise, just the base route is returned.
     */
    public static String itemRoute(String base, FinderItem item) {
        AddressTemplate template = item.get(TEMPLATE_KEY);
        if (template != null) {
            return base + AddressRouting.encode(template);
        }
        return base;
    }
}
