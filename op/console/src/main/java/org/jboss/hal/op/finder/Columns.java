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
package org.jboss.hal.op.finder;

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
import static org.patternfly.component.button.Button.button;
import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnActions.finderColumnActions;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.icon.IconSets.fas.redo;

public class Columns {

    public static final String RESOURCE_NAME_KEY = "resource-name";
    public static final String TEMPLATE_KEY = "template";

    @FunctionalInterface
    public interface MetadataPreviewBuilder {

        void onPreview(String name, Metadata metadata, FinderPreview preview);
    }

    /**
     * Creates a finder column with the specified ID and header and configures it to include a button for reloading the column.
     *
     * @param id     The unique identifier for the finder column.
     * @param header The header text to display for the column.
     * @return A configured {@link FinderColumn} instance with the specified ID and header.
     */
    public static FinderColumn resourceColumn(String id, String header) {
        return finderColumn(id).run(column ->
                column.addHeader(finderColumnHeader(header)
                        .addActions(finderColumnActions()
                                .addButton(button(redo()).small().onClick((e, b) -> column.reload())))));
    }

    /**
     * Retrieves child resources using a {@value ModelDescriptionConstants#READ_CHILDREN_NAMES_OPERATION} operation and maps
     * them into finder items.
     * <p>
     * The {@code templateFn} gets the current {@link FinderPath} as parameter and must return a wildcard address template like
     * {@code subsystem=datasources/data-source=*}. This address template is used for the
     * {@value ModelDescriptionConstants#READ_CHILDREN_NAMES_OPERATION} operation to read the child resources.
     * <p>
     * The items are built using {@code itemFn}. In addition, the following key/values are stored in the finder item
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
}
