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
package org.jboss.hal.op.task;

import java.util.SortedMap;
import java.util.TreeMap;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.Route;
import org.jboss.hal.task.Task;
import org.patternfly.component.content.Content;
import org.patternfly.component.drawer.Drawer;
import org.patternfly.component.drawer.DrawerBody;
import org.patternfly.layout.gallery.Gallery;
import org.patternfly.layout.gallery.GalleryItem;
import org.patternfly.style.Classes;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardFooter.cardFooter;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.h2;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.drawer.Drawer.drawer;
import static org.patternfly.component.drawer.DrawerBody.drawerBody;
import static org.patternfly.component.drawer.DrawerCloseButton.drawerCloseButton;
import static org.patternfly.component.drawer.DrawerContent.drawerContent;
import static org.patternfly.component.drawer.DrawerPanel.drawerPanel;
import static org.patternfly.component.drawer.DrawerPanelHead.drawerPanelHead;
import static org.patternfly.component.icon.Icon.icon;
import static org.patternfly.component.icon.IconSize.xl;
import static org.patternfly.component.page.PageGroup.pageGroup;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Direction.row;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.gallery.Gallery.gallery;
import static org.patternfly.layout.gallery.GalleryItem.galleryItem;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Size._3xl;

@Dependent
@Route("/tasks")
public class TasksPage implements Page {

    private final Instance<Task> tasks;
    private final Drawer moreInfo;
    private final Gallery gallery;
    private final Content moreInfoHeader;
    private final DrawerBody moreInfoBody;

    @Inject
    public TasksPage(Instance<Task> tasks) {
        this.tasks = tasks;
        this.moreInfo = drawer();
        this.gallery = gallery();
        this.moreInfoHeader = content(h2);
        this.moreInfoBody = drawerBody();
    }

    @Override
    public Iterable<HTMLElement> elements(Place place, Parameter parameter, LoadedData data) {
        return singletonList(pageGroup()
                .add(pageSection().limitWidth()
                        .add(content()
                                .add(title(1, _3xl, "Tasks")))
                        .add(content(p)
                                .editorial()
                                .text("Tasks enable you to complete complex tasks quickly and easily. They combine multiple steps  that involve configuring different subsystems and resources.")))
                .add(pageSection().fill()
                        .add(moreInfo.inline()
                                .onToggle((event, component, expanded) ->
                                        gallery.classList().toggle(Classes.util("pr-md"), expanded))
                                .addContent(drawerContent()
                                        .addBody(drawerBody()
                                                .add(gallery.gutter().addItems(tasksSortedByTitle(), this::taskItem))))
                                .addPanel(drawerPanel()
                                        .addHead(drawerPanelHead()
                                                .add(moreInfoHeader)
                                                .addCloseButton(drawerCloseButton()))
                                        .addBody(moreInfoBody))))
                .element());
    }

    private Iterable<Task> tasksSortedByTitle() {
        SortedMap<String, Task> sortedTasks = new TreeMap<>();
        for (Task task : this.tasks) {
            sortedTasks.put(task.title(), task);
        }
        return sortedTasks.values();
    }

    private GalleryItem taskItem(Task task) {
        return galleryItem()
                .add(card().fullHeight()
                        .addHeader(cardHeader()
                                .add(flex().direction(row).alignItems(center)
                                        .addItem(flexItem().add(icon(task.icon()).size(xl)))
                                        .addItem(flexItem().add(content(h2).text(task.title())))))
                        .addBody(cardBody()
                                .add(task.summary()))
                        .addFooter(cardFooter()
                                .add(button().css(util("mr-md")).secondary().text("Launch")
                                        .disabled(!task.enabled())
                                        .onClick((e, c) -> task.run()))
                                .add(button().link().text("Learn more")
                                        .onClick((e, c) -> {
                                            moreInfoHeader.text(task.title());
                                            removeChildrenFrom(moreInfoBody);
                                            moreInfoBody.add(task.moreInfo());
                                            if (!moreInfo.expanded()) {
                                                moreInfo.expand();
                                            }
                                        }))));
    }
}
