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
package org.jboss.hal.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

import org.jboss.elemento.logger.Logger;
import org.jboss.hal.db.LRUCache;

import static org.jboss.hal.core.NotificationModification.CLEAR;
import static org.jboss.hal.core.NotificationModification.READ;
import static org.jboss.hal.core.NotificationModification.REMOVE;
import static org.jboss.hal.core.NotificationModification.UNCLEAR;

@ApplicationScoped
public class Notifications {

    // TODO Add support for 2nd level cache!
    private static final int FIRST_LEVEL_CACHE_SIZE = 500;
    private static final Logger logger = Logger.getLogger(Notifications.class.getName());

    @Inject Event<NotificationAddEvent> addEvent;
    @Inject Event<NotificationModificationEvent> modificationEvent;
    private final LRUCache<String, Notification> cache;

    public Notifications() {
        cache = new LRUCache<>(FIRST_LEVEL_CACHE_SIZE);
        cache.addRemovalHandler((id, __) -> {
            modificationEvent.fire(new NotificationModificationEvent(REMOVE, List.of(id)));
            logger.debug("LRU notification for %s has been removed", id);
        });
    }

    // ------------------------------------------------------ api

    public Notification get(String id) {
        return cache.get(id);
    }

    public void send(Notification notification) {
        cache.put(notification.id, notification);
        addEvent.fire(new NotificationAddEvent(notification));
    }

    public void read(String id) {
        readInternal(id);
        modificationEvent.fire(new NotificationModificationEvent(READ, List.of(id)));
    }

    public void read(List<String> ids) {
        for (String id : ids) {
            readInternal(id);
        }
        modificationEvent.fire(new NotificationModificationEvent(READ, ids));
    }

    public void readAll() {
        read(new ArrayList<>(cache.keys()));
    }

    public void clear(String id) {
        clearInternal(id);
        modificationEvent.fire(new NotificationModificationEvent(CLEAR, List.of(id)));
    }

    public void clear(List<String> ids) {
        for (String id : ids) {
            clearInternal(id);
        }
        modificationEvent.fire(new NotificationModificationEvent(CLEAR, ids));
    }

    public void clearAll() {
        clear(new ArrayList<>(cache.keys()));
    }

    public void unclearLast() {
        List<String> uncleared = new ArrayList<>();
        for (Map.Entry<String, LRUCache.Node<String, Notification>> entry : cache.entries()) {
            if (entry.getValue().value.cleared) {
                uncleared.add(entry.getKey());
                entry.getValue().value.cleared = false;
            }
        }
        if (!uncleared.isEmpty()) {
            modificationEvent.fire(new NotificationModificationEvent(UNCLEAR, uncleared));
        }
    }

    public void remove(String id) {
        cache.remove(id);
        modificationEvent.fire(new NotificationModificationEvent(REMOVE, List.of(id)));
    }

    public void remove(List<String> ids) {
        for (String id : ids) {
            cache.remove(id);
        }
        modificationEvent.fire(new NotificationModificationEvent(REMOVE, ids));
    }

    public int unread() {
        return (int) cache.values().stream().filter(n -> !n.read).count();
    }

    // ------------------------------------------------------ internal

    private void clearInternal(String id) {
        Notification notification = cache.get(id);
        if (notification != null) {
            notification.cleared = true;
        }
    }

    private void readInternal(String id) {
        Notification notification = cache.get(id);
        if (notification != null) {
            notification.read = true;
        }
    }
}
