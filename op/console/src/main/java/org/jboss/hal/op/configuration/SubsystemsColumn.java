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
package org.jboss.hal.op.configuration;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.elemento.Id;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.MetadataRepository;
import org.jboss.hal.op.finder.ColumnProvider;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.FinderItem;

import elemental2.promise.Promise;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.core.LabelBuilder.labelBuilderAllWords;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.h1;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.textinputgroup.SearchInput.searchInput;
import static org.patternfly.extension.finder.FinderColumn.finderColumn;
import static org.patternfly.extension.finder.FinderColumnHeader.finderColumnHeader;
import static org.patternfly.extension.finder.FinderColumnSearch.finderColumnSearch;
import static org.patternfly.extension.finder.FinderItem.finderItem;
import static org.patternfly.icon.IconSets.fas.search;
import static org.patternfly.style.Classes.filtered;
import static org.patternfly.style.Classes.modifier;

@Dependent
public class SubsystemsColumn implements ColumnProvider {

    public static final String ID = "subsystems-column";

    private final Dispatcher dispatcher;
    private final MetadataRepository metadataRepository;

    @Inject
    public SubsystemsColumn(Dispatcher dispatcher, MetadataRepository metadataRepository) {
        this.dispatcher = dispatcher;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        Operation operation = new Operation.Builder(ResourceAddress.root(), READ_CHILDREN_NAMES_OPERATION)
                .param(CHILD_TYPE, SUBSYSTEM)
                .param(INCLUDE_SINGLETONS, true)
                .build();
        FinderColumn column = finderColumn(ID);
        return column
                .addHeader(finderColumnHeader("Subsystems"))
                .addSearch(finderColumnSearch()
                        .addSearchInput(searchInput(Id.build("subsystem", "search"))
                                .icon(search())
                                .placeholder("Filter by subsystem name")
                                .onInput((e, si, value) -> {
                                    String lcv = value.toLowerCase();
                                    for (FinderItem item : column.items()) {
                                        item.classList().toggle(modifier(filtered),
                                                !value.isEmpty() && !item.text()
                                                        .toLowerCase()
                                                        .contains(lcv));
                                    }
                                })
                                .onClear((e, si) ->
                                        column.items().forEach(item ->
                                                item.classList().remove(modifier(filtered))))
                        ))
                .addItems(__ -> dispatcher.execute(operation).then(result ->
                        Promise.resolve(result.asList().stream()
                                .map(node -> finderItem(Id.build(node.asString()))
                                        .text(labelBuilderAllWords(node.asString()))
                                        .store("subsystem", node.asString()))
                                .collect(toList()))))
                .onPreview((item, preview) -> {
                    String subsystem = item.get("subsystem");
                    AddressTemplate address = AddressTemplate.of("subsystem=" + subsystem);
                    metadataRepository.lookup(address).then(metadata -> {
                        preview.add(content(h1).text(labelBuilderAllWords(subsystem)))
                                .add(content(p).editorial().text(metadata.resourceDescription().description()));
                        return null;
                    });
                });
    }
}
