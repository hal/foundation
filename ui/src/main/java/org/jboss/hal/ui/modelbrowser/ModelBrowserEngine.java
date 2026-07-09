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
import org.jboss.hal.resources.Keys;
import org.patternfly.component.AsyncItems;
import org.patternfly.component.tree.TreeViewItem;

import elemental2.promise.Promise;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.elemento.Elements.div;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CHILD_TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.INCLUDE_SINGLETONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_CHILDREN_TYPES_OPERATION;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.FOLDER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.RESOURCE;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.SINGLETON_FOLDER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.SINGLETON_RESOURCE;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.popover.Popover.popover;
import static org.patternfly.component.popover.PopoverBody.popoverBody;
import static org.patternfly.component.tree.TreeViewItem.treeViewItem;
import static org.patternfly.core.Dataset.identifier;
import static org.patternfly.style.Classes.disabled;
import static org.patternfly.style.Classes.modifier;

/**
 * Contains code to build read children operations, parse the result, and turn it into {@link ModelBrowserNode}s and
 * {@link org.patternfly.component.tree.TreeViewItem}s.
 */
class ModelBrowserEngine {

    private static final Logger logger = Logger.getLogger(ModelBrowserEngine.class.getName());

    // The DMR operation selection and result parsing below mirrors the logic in
    // ResourceList.load() / parseChildNames() / parseChildTypes().
    // Both use the same two-mode pattern based on the template shape:
    //   - wildcard (=*): read-children-names on the parent address
    //   - non-wildcard:  read-children-types with include-singletons on the template address
    // The duplication is intentional: this class builds a hierarchical ModelBrowserNode tree
    // (with SINGLETON_FOLDER grouping and non-existing singleton detection) for the TreeView,
    // while ResourceList builds a flat ChildResource list for a DataList.
    // The shared logic is small (~40 lines) and unlikely to diverge, so extracting it into a
    // shared utility would add indirection without meaningful benefit.

    /**
     * Returns a function that returns a promise to read the child resources of the selected tree view item. Uses
     * {@link #parseChildren(ModelBrowserNode, ModelNode, boolean)} and {@link #mbn2tvi(Dispatcher)}.
     */
    static AsyncItems<TreeViewItem, TreeViewItem> readChildrenOperation(Dispatcher dispatcher) {
        return tvi -> {
            ModelBrowserNode mbn = tvi.get(Keys.MODEL_BROWSER_NODE);
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
                    mbn.children.add(new ModelBrowserNode(template.appendTrusted(name), child, SINGLETON_RESOURCE));
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
                .store(Keys.MODEL_BROWSER_NODE, mbn)
                .run(tvi -> {
                    if (mbn.exists) {
                        tvi.addItems(readChildrenOperation(dispatcher));
                        if (mbn.type.expandedIcon != null) {
                            tvi.expandedIcon(mbn.type.expandedIcon.get());
                        }
                    } else {
                        tvi.css(modifier(disabled));
                        nonExistingSingletonPopover(mbn, tvi);
                    }
                });
    }

    private static void nonExistingSingletonPopover(ModelBrowserNode mbn, TreeViewItem tvi) {
        logger.debug("Add popover for non-existing singleton resource %s", mbn.identifier);
        AddressTemplate anonymous = mbn.template.anonymiseLast(); // /a=b/c=d -> /a=b/c=*
        tvi.add(popover(By.data(identifier, mbn.identifier))
                .addHeader(mbn.name)
                .addBody(popoverBody()
                        .add(div().add("This resource does not yet exist."))
                        .add(div().add(button("Add resource").link().inline()
                                .onClick((e, b) -> ModelBrowserEvents.AddResource.dispatch(b.element(),
                                        anonymous, mbn.name, true))))));
    }
}
