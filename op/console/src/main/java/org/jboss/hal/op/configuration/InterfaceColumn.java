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

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.jboss.hal.core.CrudOperations;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.op.finder.ColumnProvider;
import org.jboss.hal.ui.resource.FinderSupport;
import org.jboss.hal.ui.resource.ResourceView;
import org.patternfly.extension.finder.FinderColumn;
import org.patternfly.extension.finder.FinderItem;
import org.patternfly.extension.finder.FinderPreview;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.LabelBuilder.labelBuilder;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT_INTERFACE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.QUERY_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SELECT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SOCKET_BINDING_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.WHERE;
import static org.jboss.hal.ui.BuildingBlocks.crudColumn;
import static org.jboss.hal.ui.BuildingBlocks.stackPreview;
import static org.jboss.hal.ui.resource.ResourceView.resourceView;
import static org.jboss.hal.ui.resource.ViewItem.viewItem;
import static org.patternfly.component.list.DescriptionListTerm.descriptionListTerm;
import static org.patternfly.layout.stack.StackItem.stackItem;

@Dependent
public class InterfaceColumn implements ColumnProvider {

    public static final String ID = "interface-column";
    private static final AddressTemplate TEMPLATE = AddressTemplate.of("interface=*");

    private final Dispatcher dispatcher;
    private final CrudOperations crud;
    private final StatementContext statementContext;

    @Inject
    public InterfaceColumn(Dispatcher dispatcher, CrudOperations crud, StatementContext statementContext) {
        this.dispatcher = dispatcher;
        this.crud = crud;
        this.statementContext = statementContext;
    }

    @Override
    public String identifier() {
        return ID;
    }

    @Override
    public FinderColumn get() {
        return crudColumn(ID, "Interface", emptyList(), __ -> TEMPLATE, null).onPreview(this::preview);
    }

    private void preview(FinderItem item, FinderPreview preview) {
        String name = item.text();
        stackPreview(preview, name, stack -> {
            AddressTemplate template = item.get(FinderSupport.TEMPLATE_KEY);
            crud.readWithMetadata(template).then(tuple -> {
                ResourceView resourceView = resourceView(template, tuple.key, tuple.value,
                        asList("inet-address", "loopback", "loopback-address", "multicast", "nic", "nic-match",
                                "resolved-address"));
                stack.addItem(stackItem().add(resourceView));
                addSocketBindingGroup(resourceView, name);
                return null;
            });
        });
    }

    private void addSocketBindingGroup(ResourceView resourceView, String interface_) {
        AddressTemplate template = AddressTemplate.of("{selected.profile}/socket-binding-group=*");
        Operation operation = new Operation.Builder(template.resolve(statementContext), QUERY_OPERATION)
                .param(SELECT, new ModelNode().add(NAME))
                .param(WHERE, new ModelNode().set(DEFAULT_INTERFACE, interface_))
                .build();
        dispatcher.execute(operation, result -> {
            List<String> socketBindingGroups = result.asList().stream()
                    .filter(modelNode -> !modelNode.isFailure())
                    .map(modelNode -> ModelNodeHelper.nested(modelNode, RESULT + "." + NAME))
                    .filter(ModelNode::isDefined)
                    .map(ModelNode::asString)
                    .sorted()
                    .collect(toList());
            if (!socketBindingGroups.isEmpty()) {
                // TODO Add link to socket binding group
                resourceView.add(viewItem("sbg",
                        descriptionListTerm(labelBuilder(SOCKET_BINDING_GROUP)),
                        span().text(socketBindingGroups.stream().collect(joining(", "))).element()));
            }
        });
    }
}
