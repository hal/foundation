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

import jakarta.enterprise.context.Dependent;

import org.gwtproject.safehtml.shared.SafeHtml;
import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.jboss.elemento.router.LoadedData;
import org.jboss.elemento.router.Page;
import org.jboss.elemento.router.Parameter;
import org.jboss.elemento.router.Place;
import org.jboss.elemento.router.Route;
import org.jboss.hal.core.Notification;
import org.jboss.hal.ui.Marked;
import org.patternfly.component.content.ContentType;
import org.patternfly.layout.gallery.GalleryItem;

import elemental2.dom.HTMLElement;

import static java.util.Collections.singletonList;
import static org.jboss.hal.core.Notification.nyi;
import static org.jboss.hal.op.task.TaskDefinition.taskDefinitions;
import static org.jboss.hal.ui.MarkedOptions.markedOptions;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.card.Card.card;
import static org.patternfly.component.card.CardBody.cardBody;
import static org.patternfly.component.card.CardFooter.cardFooter;
import static org.patternfly.component.card.CardHeader.cardHeader;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.icon.Icon.icon;
import static org.patternfly.component.icon.IconSize.xl;
import static org.patternfly.component.page.PageGroup.pageGroup;
import static org.patternfly.component.page.PageSection.pageSection;
import static org.patternfly.component.title.Title.title;
import static org.patternfly.icon.PredefinedIcon.predefinedIcon;
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
                        .add(gallery().gutter().addItems(taskDefinitions(), this::tdGalleryItem)))
                .element());
    }

    private GalleryItem tdGalleryItem(TaskDefinition td) {
        String parsedAndPurified = Marked.parseInlineAndPurify(td.summary, markedOptions().gfm(true));
        SafeHtml safeSummary = SafeHtmlUtils.fromSafeConstant(parsedAndPurified);
        return galleryItem()
                .add(card().fullHeight()
                        .addHeader(cardHeader()
                                .add(flex().direction(row).alignItems(center)
                                        .addItem(flexItem().add(icon(predefinedIcon(td.icon)).size(xl)))
                                        .addItem(flexItem().add(content(ContentType.h2).text(td.title)))))
                        .addBody(cardBody()
                                .add(content(p).html(safeSummary)))
                        .addFooter(cardFooter()
                                .add(button().css(util("mr-md")).secondary().text("Launch")
                                        .onClick((e, c) -> uic().notifications().send(nyi())))
                                .add(button().link().text("Learn more")
                                        .onClick((e, c) -> uic().notifications().send(Notification.nyi())))));
    }
}
