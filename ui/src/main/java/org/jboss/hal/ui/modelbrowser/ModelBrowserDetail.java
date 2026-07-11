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
import java.util.List;

import org.jboss.elemento.IsElement;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.AddResource;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.DeleteResource;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.jboss.hal.ui.resource.ResourceList.ChildResource;
import org.jboss.hal.ui.resource.ResourceShell;
import org.patternfly.core.OuiaSupport;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.HalClasses.content;
import static org.jboss.hal.resources.HalClasses.detail;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.resource.ResourceBreadcrumb.resourceBreadcrumb;
import static org.jboss.hal.ui.resource.ResourceHeader.resourceHeader;
import static org.jboss.hal.ui.resource.ResourceList.resourceList;
import static org.jboss.hal.ui.resource.ResourceShell.resourceShell;
import static org.jboss.hal.ui.resource.ResourceTabs.resourceTabs;

/**
 * Right-side detail panel of the model browser showing information about the selected resource.
 * <p>
 * For folder nodes, displays a {@link org.jboss.hal.ui.resource.ResourceList} of child resources. For resource nodes, displays
 * a tabbed view with data, attributes, operations, and capabilities tabs via {@link org.jboss.hal.ui.resource.ResourceTabs}.
 * The panel includes a breadcrumb trail for navigation and a copy-to-clipboard button for the resource address.
 * <p>
 * Delegates to the reusable resource components ({@link ResourceShell}, {@link org.jboss.hal.ui.resource.ResourceBreadcrumb},
 * {@link org.jboss.hal.ui.resource.ResourceHeader}, {@link org.jboss.hal.ui.resource.ResourceTabs},
 * {@link org.jboss.hal.ui.resource.ResourceList}) for rendering.
 */
class ModelBrowserDetail implements IsElement<HTMLElement>, OuiaSupport<HTMLElement, ModelBrowserDetail> {

    static String lastTab = null;
    private final ModelBrowser modelBrowser;
    private final HTMLElement root;

    ModelBrowserDetail(ModelBrowser modelBrowser) {
        this.modelBrowser = modelBrowser;
        this.root = div().css(halComponent(HalClasses.modelBrowser, detail)).element();
        initOuia();
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/ModelBrowserDetail";
    }

    @Override
    public ModelBrowserDetail that() {
        return this;
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    void show(ModelBrowserNode mbn) {
        removeChildrenFrom(root);
        uic().metadataRepository().lookup(mbn.template, metadata -> {
            int rootSize = modelBrowser.root.size();
            ResourceShell shell = resourceShell(mbn.template, metadata)
                    .contentCss(halComponent(HalClasses.modelBrowser, detail, content))
                    .addBreadcrumb(resourceBreadcrumb(mbn.template, metadata)
                            .onSegmentClick((item, template, depth) -> {
                                if (depth < rootSize) {
                                    modelBrowser.home();
                                } else {
                                    modelBrowser.tree.select(item.identifier());
                                }
                            }))
                    .addHeader(resourceHeader(mbn.template, metadata)
                            .customTitle(titleFor(mbn))
                            .showStability(mbn.type != ModelBrowserNode.Type.SINGLETON_FOLDER)
                            .showDescription(mbn.type != ModelBrowserNode.Type.SINGLETON_FOLDER));

            switch (mbn.type) {
                case SINGLETON_FOLDER:
                case FOLDER:
                    shell.addResourceList(resourceList(mbn.template, metadata)
                            .singletonFolder(mbn.type == ModelBrowserNode.Type.SINGLETON_FOLDER)
                            .missingChildren(missingChildrenFor(mbn))
                            .onSelect(template -> SelectInTree.dispatch(root, template))
                            .onAdd((parent, child, singleton) -> AddResource.dispatch(root, parent, child, singleton))
                            .onDelete(template -> DeleteResource.dispatch(root, template)));
                    break;
                case SINGLETON_RESOURCE:
                case RESOURCE:
                    shell.addTabs(resourceTabs(mbn.template, metadata)
                            .initialSelection(lastTab)
                            .onSelect((tabId, selected) -> lastTab = tabId));
                    break;
            }
            root.appendChild(shell.element());
        });
    }

    private List<ChildResource> missingChildrenFor(ModelBrowserNode parent) {
        if (parent.type != ModelBrowserNode.Type.SINGLETON_FOLDER || parent.children.isEmpty()) {
            return new ArrayList<>();
        }
        AddressTemplate parentTemplate = parent.template;
        List<ChildResource> missing = new ArrayList<>();
        for (ModelBrowserNode child : parent.children) {
            AddressTemplate childTemplate = parentTemplate.parent()
                    .append(parentTemplate.last().key, child.name);
            missing.add(new ChildResource(child.name, childTemplate, true, false));
        }
        return missing;
    }

    private HTMLElement titleFor(ModelBrowserNode mbn) {
        return switch (mbn.type) {
            case SINGLETON_FOLDER -> span().add("Singleton child resources of ").add(code().text(mbn.name)).element();
            case FOLDER -> span().add("Child resources of ").add(code().text(mbn.name)).element();
            default -> span().text(mbn.name).element();
        };
    }
}
