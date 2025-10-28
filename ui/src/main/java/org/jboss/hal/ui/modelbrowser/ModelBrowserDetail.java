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

import org.jboss.elemento.By;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.Id;
import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.Segment;
import org.jboss.hal.resources.HalClasses;
import org.patternfly.component.breadcrumb.Breadcrumb;
import org.patternfly.component.icon.Icon;
import org.patternfly.component.icon.IconSize;
import org.patternfly.component.page.PageBreadcrumb;
import org.patternfly.component.page.PageSection;
import org.patternfly.component.title.Title;
import org.patternfly.component.tooltip.Tooltip;
import org.patternfly.layout.flex.FlexItem;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLParagraphElement;

import static elemental2.dom.DomGlobal.navigator;
import static org.jboss.elemento.Elements.code;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.resources.HalClasses.content;
import static org.jboss.hal.resources.HalClasses.copy;
import static org.jboss.hal.resources.HalClasses.detail;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.ui.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.ROOT_ID;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.FOLDER;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.RESOURCE;
import static org.jboss.hal.ui.modelbrowser.ModelBrowserNode.Type.SINGLETON_RESOURCE;
import static org.patternfly.component.breadcrumb.Breadcrumb.breadcrumb;
import static org.patternfly.component.breadcrumb.BreadcrumbItem.breadcrumbItem;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.icon.Icon.icon;
import static org.patternfly.component.page.PageBreadcrumb.pageBreadcrumb;
import static org.patternfly.component.page.PageGroup.pageGroup;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.icon.IconSets.fas.copy;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.popper.Placement.auto;
import static org.patternfly.style.Size._3xl;
import static org.patternfly.style.Sticky.top;

class ModelBrowserDetail implements IsElement<HTMLElement> {

    static String lastTab = null;
    private final ModelBrowser modelBrowser;
    private final HTMLElement root;
    private final PageBreadcrumb pageBreadcrumb;
    private final Title header;
    private final FlexItem stabilityContainer;
    private final HTMLContainerBuilder<HTMLParagraphElement> description;
    private final PageSection pageSection;

    ModelBrowserDetail(ModelBrowser modelBrowser) {
        this.modelBrowser = modelBrowser;
        this.root = div().css(halComponent(HalClasses.modelBrowser, detail))
                .add(pageGroup()
                        .sticky(top)
                        .addSection(pageBreadcrumb = pageBreadcrumb())
                        .addSection(pageSection()
                                .add(content()
                                        .add(flex().alignItems(center)
                                                .addItem(flexItem().add(header = title(1, _3xl, "")))
                                                .addItem(stabilityContainer = flexItem()))
                                        .add(description = p().text("")))))
                .add(pageSection = pageSection().css(halComponent(HalClasses.modelBrowser, detail, content)))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    void show(ModelBrowserNode mbn) {
        clear();
        uic().metadataRepository().lookup(mbn.template, metadata -> {
            fillBreadcrumb(mbn);
            adjustHeader(mbn, metadata);
            switch (mbn.type) {
                case SINGLETON_FOLDER:
                case FOLDER:
                    pageSection.add(new ResourceList(mbn, metadata));
                    break;
                case SINGLETON_RESOURCE:
                case RESOURCE:
                    pageSection.add(new ResourceDetails(mbn, metadata));
                    break;
            }
        });
    }

    private void fillBreadcrumb(ModelBrowserNode mbn) {
        Breadcrumb breadcrumb = breadcrumb();
        if (mbn.template.isEmpty()) {
            breadcrumb.addItem(breadcrumbItem(ROOT_ID, "/"));
        } else {
            breadcrumb.addItem(breadcrumbItem(ROOT_ID, "/")
                    .onClick((event, breadcrumbItem) -> {
                        event.preventDefault();
                        event.stopPropagation();
                        modelBrowser.home();
                    }));
            AddressTemplate current = AddressTemplate.root();
            for (Segment segment : mbn.template) {
                current = current.append(segment.key, segment.value);
                final AddressTemplate finalTemplate = current;
                boolean last = current.last().equals(mbn.template.last());
                breadcrumb.addItem(breadcrumbItem(current.identifier(), segment.key + "=" + segment.value)
                        .active(last)
                        .run(bci -> {
                            if (!last) {
                                bci.onClick((event, breadcrumbItem) -> {
                                    event.preventDefault();
                                    event.stopPropagation();
                                    modelBrowser.tree.select(breadcrumbItem.identifier());
                                });
                            } else {
                                bci.add(copyToClipboard(finalTemplate));
                            }
                        }));
            }
        }
        pageBreadcrumb.addBreadcrumb(breadcrumb);
    }

    private void adjustHeader(ModelBrowserNode mbn, Metadata metadata) {
        switch (mbn.type) {
            case SINGLETON_FOLDER:
                header.add("Singleton child resources of ").add(code().text(mbn.name));
                break;
            case FOLDER:
                header.add("Child resources of ").add(code().text(mbn.name));
                break;
            case SINGLETON_RESOURCE:
            case RESOURCE:
                header.add(mbn.name);
                break;
        }
        if (mbn.type == FOLDER || mbn.type == SINGLETON_RESOURCE || mbn.type == RESOURCE) {
            Stability stability = metadata.resourceDescription().stability();
            if (uic().environment().highlightStability(stability)) {
                stabilityContainer.add(stabilityLabel(stability));
            }
            description.text(metadata.resourceDescription().description());
        }
    }

    private void clear() {
        removeChildrenFrom(pageBreadcrumb);
        removeChildrenFrom(stabilityContainer);
        removeChildrenFrom(header);
        removeChildrenFrom(description);
        removeChildrenFrom(pageSection);
    }

    private HTMLElement copyToClipboard(AddressTemplate template) {
        String copyToClipboardId = Id.unique("address", "copy");
        String copyToClipboardText = "Copy address to clipboard";
        Tooltip tooltip = tooltip(By.id(copyToClipboardId), copyToClipboardText)
                .placement(auto)
                .onClose((e, t) -> t.text(copyToClipboardText)); // restore text
        Icon icon = icon(copy()).size(IconSize.sm).id(copyToClipboardId).on(click, e -> {
            navigator.clipboard.writeText(template.toString());
            tooltip.text("Address copied");
        });
        return span().css(halComponent(HalClasses.modelBrowser, copy))
                .add(icon)
                .add(tooltip)
                .element();
    }
}
