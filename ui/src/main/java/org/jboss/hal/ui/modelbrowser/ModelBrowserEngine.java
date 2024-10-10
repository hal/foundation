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
package org.jboss.hal.ui.modelbrowser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jboss.elemento.By;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.patternfly.component.tree.TreeViewItem;
import org.patternfly.style.Classes;

import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.FOLDER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.RESOURCE;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.SINGLETON_FOLDER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.SINGLETON_RESOURCE;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.component.tree.TreeViewItem.treeViewItem;
import static org.patternfly.core.Dataset.identifier;
import static org.patternfly.style.Classes.disabled;
import static org.patternfly.style.Classes.modifier;

/**
 * Contains code to build read children operations, parse the result, and turn it into {@link ModelBrowserNode}s and
 * {@link org.patternfly.component.tree.TreeViewItem}s.
 */
class ModelBrowserEngine {

    static final String MODEL_BROWSER_NODE = "model-browser-node";
    private static final Logger logger = Logger.getLogger(ModelBrowserEngine.class.getName());

    /**
     * Returns a function that returns a promise to read the child resources of the selected tree view item. Uses
     * {@link #parseChildren(ModelBrowserNode, ModelNode, boolean)} and {@link #mbn2tvi(Dispatcher)}.
     */
    static Function<TreeViewItem, Promise<Iterable<TreeViewItem>>> readChildrenOperation(Dispatcher dispatcher) {
        return tvi -> {
            ModelBrowserNode mbn = tvi.get(MODEL_BROWSER_NODE);
            if (mbn != null) {
                Operation operation = null;
                if (mbn.type == SINGLETON_FOLDER || mbn.type == FOLDER) {
                    operation = new Operation.Builder(mbn.template.parent().resolve(), READ_CHILDREN_NAMES_OPERATION)
                            .param(CHILD_TYPE, mbn.name)
                            .build();
                } else if (mbn.type == SINGLETON_RESOURCE || mbn.type == RESOURCE) {
                    operation = new Operation.Builder(mbn.template.resolve(), READ_CHILDREN_TYPES_OPERATION)
                            .param(INCLUDE_SINGLETONS, true)
                            .build();
                }
                if (operation != null) {
                    return dispatcher.execute(operation)
                            .then(result -> Promise.resolve(parseChildren(mbn, result, true)
                                    .stream()
                                    .map(mbn2tvi(dispatcher))
                                    .collect(toList())));
                } else {
                    logger.error("Unable to read child resources of tree view item %o - %s: Wrong node type %s",
                            tvi.element(), tvi.identifier(), mbn.type.name());
                    return Promise.resolve(emptyList());
                }
            } else {
                logger.error(
                        "Unable to read child resources of tree view item %o - %s: No model browser node found in tree view item context!",
                        tvi.element(), tvi.identifier());
                return Promise.resolve(emptyList());
            }
        };
    }

    /**
     * Parses the result of {@link #readChildrenOperation(Dispatcher)} and turns it into a list of {@link ModelBrowserNode}s.
     */
    static List<ModelBrowserNode> parseChildren(ModelBrowserNode parent, ModelNode result,
            boolean nonExistingSingletons) {
        AddressTemplate template = parent.template;
        Map<String, ModelBrowserNode> mbns = new LinkedHashMap<>();
        for (ModelNode modelNode : result.asList()) {
            String name = modelNode.asString();
            if (parent.type == RESOURCE) {
                int index = name.indexOf("=");
                if (index != -1 && !name.equals("=")) {
                    // This adds *all* children: those who exist and those who do not.
                    // When this method is called with parent.type == SINGLETON_FOLDER,
                    // we can detect the difference (see below)
                    String singleton = name.substring(0, index);
                    String child = name.substring(index + 1);
                    ModelBrowserNode mbn = mbns.computeIfAbsent(singleton,
                            key -> new ModelBrowserNode(template.append(key, "*"), key, SINGLETON_FOLDER));
                    mbn.children.add(new ModelBrowserNode(template.append(name), child, SINGLETON_RESOURCE));
                } else {
                    if (template.template.endsWith("*")) {
                        mbns.put(name,
                                new ModelBrowserNode(template.parent().append(template.last().key, name), name, RESOURCE));
                    } else {
                        mbns.put(name, new ModelBrowserNode(template.append(name, "*"), name, FOLDER));
                    }
                }
            } else {
                if (template.template.endsWith("*")) {
                    mbns.put(name, new ModelBrowserNode(template.parent().append(template.last().key, name), name, RESOURCE));
                } else {
                    mbns.put(name, new ModelBrowserNode(template.append(name, "*"), name, FOLDER));
                }
            }
        }
        if (nonExistingSingletons &&
                parent.type == SINGLETON_FOLDER &&
                !parent.children.isEmpty() &&
                parent.children.size() > mbns.size()) {
            // Add non-existing singleton resources
            for (ModelBrowserNode child : parent.children) {
                if (!mbns.containsKey(child.name)) {
                    mbns.put(child.name, child.copy(mbn -> mbn.exists = false));
                }
            }
        }
        return new ArrayList<>(mbns.values());
    }

    /**
     * Returns a function that turns a {@link ModelBrowserNode} into a {@link TreeViewItem}.
     */
    static Function<ModelBrowserNode, TreeViewItem> mbn2tvi(Dispatcher dispatcher) {
        return mbn -> treeViewItem(mbn.identifier)
                .text(mbn.name)
                .icon(mbn.type.icon.get())
                .store(MODEL_BROWSER_NODE, mbn)
                .run(tvi -> {
                    if (mbn.exists) {
                        tvi.addItems(readChildrenOperation(dispatcher));
                        if (mbn.type.expandedIcon != null) {
                            tvi.expandedIcon(mbn.type.expandedIcon.get());
                        }
                    } else {
                        tvi.css(modifier(disabled));
                        // This popover is used for the initial load. It is removed together with the
                        // tree view item when the parent is collapsed.
                        tvi.add(popover(By.data(identifier, mbn.identifier))
                                .addHeader(mbn.name)
                                .addBody("Non-existing singleton resource"));
                    }
                })
                .onToggle((event, tvi, expanded) -> {
                    if (expanded) {
                        for (TreeViewItem item : tvi.items()) {
                            if (item.element().classList.contains(modifier(Classes.disabled))) {
                                ModelBrowserNode m = item.get(MODEL_BROWSER_NODE);
                                if (m != null) {
                                    // This popover is used when the paren item is collapsed/expanded
                                    // This is necessary because the initial popover has been removed
                                    // on collapsed. It is removed automatically when the parent is collapsed.
                                    item.add(popover(By.data(identifier, m.identifier))
                                            .addHeader(m.name)
                                            .addBody("Non-existing singleton resource"));
                                }
                            }
                        }
                    }
                });
    }
}
