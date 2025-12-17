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
package org.jboss.hal.op.notification;

import org.jboss.hal.core.Notifications;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.notification.NotificationBadge;
import org.patternfly.component.notification.NotificationDrawer;
import org.patternfly.component.notification.NotificationDrawerBody;
import org.patternfly.component.notification.NotificationDrawerItem;
import org.patternfly.component.notification.NotificationDrawerList;

import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.emptystate.EmptyStateBody.emptyStateBody;
import static org.patternfly.component.menu.Dropdown.dropdown;
import static org.patternfly.component.menu.DropdownMenu.dropdownMenu;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.notification.NotificationBadge.notificationBadge;
import static org.patternfly.component.notification.NotificationDrawer.notificationDrawer;
import static org.patternfly.component.notification.NotificationDrawerBody.notificationDrawerBody;
import static org.patternfly.component.notification.NotificationDrawerHeader.notificationDrawerHeader;
import static org.patternfly.component.notification.NotificationDrawerList.notificationDrawerList;
import static org.patternfly.icon.IconSets.fas.ellipsisV;
import static org.patternfly.icon.IconSets.fas.search;

public class NotificationElements {

    public static NotificationElements notificationElements(Notifications notifications) {
        return new NotificationElements(notifications);
    }

    private final NotificationBadge badge;
    private final NotificationDrawer drawer;

    NotificationElements(Notifications notifications) {
        this.badge = notificationBadge().registerComponent();
        this.drawer = notificationDrawer();

        NotificationDrawerList notificationDrawerList = notificationDrawerList().registerSubComponent();
        NotificationDrawerBody notificationDrawerBody = notificationDrawerBody().registerSubComponent()
                .addList(notificationDrawerList)
                .addEmptyState(emptyState()
                        .icon(search())
                        .text("No notifications found")
                        .addBody(emptyStateBody()
                                .text("There are currently no notifications.")));

        drawer.addHeader(notificationDrawerHeader().registerSubComponent()
                        .addAction(dropdown(ellipsisV(), "notification drawer actions")
                                .addMenu(dropdownMenu()
                                        .addContent(menuContent()
                                                .addList(menuList()
                                                        .addItem(menuItem("mark-all-read", "Mark all read")
                                                                .onClick((e, c) -> notifications.markAllAsRead()))
                                                        .addItem(menuItem("clear-all", "Clear all")
                                                                .onClick((e, c) -> notifications.clearAll()))
                                                        .addItem(menuItem("unclear-last", "Unclear last")
                                                                .onClick((e, c) -> notifications.unclearLast())))))))
                .addBody(notificationDrawerBody);
        notificationDrawerBody.markEmpty(true);

        badge.onToggle((event, component, expanded) -> {
            if (expanded) {
                for (NotificationDrawerItem item : notificationDrawerList.items()) {
                    org.jboss.hal.core.Notification notification = item.get(Keys.NOTIFICATION);
                    if (notification != null) {
                        if (notification.age() < Notifications.RELATIVE_TIME_THRESHOLD) {
                            item.timestamp(notifications.timestamp(notification.id));
                        }
                    }
                }
            }
        });
    }

    public NotificationBadge badge() {
        return badge;
    }

    public NotificationDrawer drawer() {
        return drawer;
    }
}
