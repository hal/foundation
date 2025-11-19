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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.jboss.elemento.Elements;
import org.jboss.hal.core.Notification;
import org.jboss.hal.core.NotificationAddEvent;
import org.jboss.hal.core.NotificationModification;
import org.jboss.hal.core.NotificationModificationEvent;
import org.jboss.hal.core.Notifications;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.ComponentType;
import org.patternfly.component.Severity;
import org.patternfly.component.alert.Alert;
import org.patternfly.component.menu.Dropdown;
import org.patternfly.component.notification.NotificationBadge;
import org.patternfly.component.notification.NotificationDrawerBody;
import org.patternfly.component.notification.NotificationDrawerHeader;
import org.patternfly.component.notification.NotificationDrawerItem;
import org.patternfly.component.notification.NotificationDrawerList;
import org.patternfly.style.NotificationStatus;

import elemental2.dom.Element;

import static java.lang.Double.parseDouble;
import static org.patternfly.component.ComponentRegistry.componentRegistry;
import static org.patternfly.component.alert.AlertDescription.alertDescription;
import static org.patternfly.component.alert.AlertGroup.toastAlertGroup;
import static org.patternfly.component.menu.Dropdown.dropdown;
import static org.patternfly.component.menu.DropdownMenu.dropdownMenu;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuItem.menuItem;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.notification.NotificationDrawerItemBody.notificationDrawerItemBody;
import static org.patternfly.icon.IconSets.fas.ellipsisV;

@ApplicationScoped
public class NotificationListener {

    @Inject Notifications notifications;

    public void onNotificationAdded(@Observes NotificationAddEvent event) {
        toastAlertGroup().addItem(alert(event.notification));

        NotificationBadge notificationBadge = notificationBadge();
        if (notificationBadge != null) {
            notificationBadge.triggerNotification();
            if (event.notification.severity() == Severity.danger) {
                notificationBadge.status(NotificationStatus.attention);
            } else {
                notificationBadge.status(NotificationStatus.unread);
            }
        }

        NotificationDrawerList drawerList = notificationDrawerList();
        if (drawerList != null) {
            drawerList.addItem(notificationDrawerItem(event.notification).read(false));
        }

        // execute last after notifications have been added/removed!
        updateState();
    }

    public void onNotificationModification(@Observes NotificationModificationEvent event) {
        NotificationDrawerList drawerList = notificationDrawerList();
        if (drawerList != null) {
            if (event.modification == NotificationModification.READ) {
                for (String id : event.ids) {
                    NotificationDrawerItem item = drawerList.item(id);
                    if (item != null) {
                        item.read();
                    }
                }

            } else if (event.modification == NotificationModification.CLEAR || event.modification == NotificationModification.REMOVE) {
                for (String id : event.ids) {
                    drawerList.removeItem(id);
                }

            } else if (event.modification == NotificationModification.UNCLEAR) {
                for (String id : event.ids) {
                    Notification unclear = notifications.get(id);
                    NotificationDrawerItem previousItem = findPreviousItem(drawerList, unclear.timestamp);
                    drawerList.insertAfter(notificationDrawerItem(unclear).read(), previousItem);
                }
            }
        }

        // execute last after notifications have been added/removed!
        updateState();
    }

    // ------------------------------------------------------ internal

    private void updateState() {
        int unread = notifications.countUnread();
        int unreadDanger = notifications.countUnreadDanger();
        NotificationBadge notificationBadge = notificationBadge();
        if (notificationBadge != null) {
            notificationBadge.count(unread);
            if (unreadDanger > 0) {
                notificationBadge.status(NotificationStatus.attention);
            } else if (unread > 0) {
                notificationBadge.status(NotificationStatus.unread);
            } else {
                notificationBadge.status(NotificationStatus.read);
            }
        }

        NotificationDrawerHeader drawerHeader = notificationDrawerHeader();
        if (drawerHeader != null) {
            if (unread > 0) {
                drawerHeader.status(unread + " unread");
            } else {
                drawerHeader.status("");
            }
        }

        NotificationDrawerBody drawerBody = notificationDrawerBody();
        NotificationDrawerList drawerList = notificationDrawerList();
        if (drawerBody != null && drawerList != null) {
            drawerBody.markEmpty(drawerList.isEmpty());
            if (!drawerList.isEmpty()) {
                for (NotificationDrawerItem item : drawerList.items()) {
                    Notification notification = item.get(Keys.NOTIFICATION);
                    if (notification != null) {
                        if (notification.age() < Notifications.RELATIVE_TIME_THRESHOLD) {
                            item.timestamp(notifications.timestamp(notification.id));
                        }
                    }
                }
            }
        }
    }

    private NotificationDrawerItem findPreviousItem(NotificationDrawerList drawerList, double timestamp) {
        for (NotificationDrawerItem item : drawerList.items()) {
            double itemTimestamp = parseDouble(item.get(Keys.NOTIFICATION_TIMESTAMP, "0"));
            if (itemTimestamp > timestamp) {
                return item;
            }
        }
        return null;
    }

    // ------------------------------------------------------ internal lookup

    private NotificationBadge notificationBadge() {
        return componentRegistry().lookupComponent(ComponentType.NotificationBadge);
    }

    private NotificationDrawerHeader notificationDrawerHeader() {
        return componentRegistry().lookupSubComponent(ComponentType.NotificationDrawer,
                NotificationDrawerHeader.SUB_COMPONENT_NAME);
    }

    private NotificationDrawerBody notificationDrawerBody() {
        return componentRegistry().lookupSubComponent(ComponentType.NotificationDrawer,
                NotificationDrawerBody.SUB_COMPONENT_NAME);
    }

    private NotificationDrawerList notificationDrawerList() {
        return componentRegistry().lookupSubComponent(ComponentType.NotificationDrawer,
                NotificationDrawerList.SUB_COMPONENT_NAME);
    }

    // ------------------------------------------------------ internal factory methods

    private Alert alert(Notification notification) {
        return Alert.alert(notification.severity(), notification.id,
                        notification.id.substring(0, 8) + ": " + notification.title)
                .addDescription(alertDescription(notification.description));
    }

    private NotificationDrawerItem notificationDrawerItem(Notification notification) {
        Dropdown actions = dropdown(ellipsisV(), "notification item action for " + notification.title)
                .addMenu(dropdownMenu()
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItem(menuItem("mark-all-read", "Mark as read")
                                                .onClick((e, c) -> notifications.markAsRead(notification.id)))
                                        .addItem(menuItem("clear-all", "Clear")
                                                .onClick((e, c) -> notifications.clear(notification.id))))));
        return NotificationDrawerItem.notificationDrawerItem(notification.severity(), notification.id,
                        notification.id.substring(0, 8) + ": " + notification.title)
                .hoverable()
                .timestamp(notifications.timestamp(notification.id))
                .store(Keys.NOTIFICATION, notification)
                .store(Keys.NOTIFICATION_TIMESTAMP, String.valueOf(notification.timestamp))
                .onClick((event, component) -> {
                    Element target = (Element) event.target;
                    if (target != actions.element() && !actions.element().contains(target)) {
                        notifications.markAsRead(notification.id);
                    }
                })
                .addAction(actions)
                .addBody(notificationDrawerItemBody(notification.description));
    }
}
