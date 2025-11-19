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

import org.gwtproject.safehtml.shared.SafeHtmlUtils;
import org.patternfly.component.Severity;

import elemental2.core.JsArray;
import elemental2.core.JsDate;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

import static org.jboss.elemento.Id.uuid;
import static org.jboss.hal.core.NotificationDetails.notificationDetails;
import static org.patternfly.component.Severity.danger;
import static org.patternfly.component.Severity.info;
import static org.patternfly.component.Severity.success;
import static org.patternfly.component.Severity.warning;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class Notification {

    // ------------------------------------------------------ factory

    @JsOverlay
    public static Notification info(String title, String description) {
        return notification(info, title, description);
    }

    @JsOverlay
    public static Notification success(String title, String description) {
        return notification(success, title, description);
    }

    @JsOverlay
    public static Notification warning(String title, String description) {
        return notification(warning, title, description);
    }

    @JsOverlay
    public static Notification error(String title, String description) {
        return notification(danger, title, description);
    }

    @JsOverlay
    public static Notification nyi() {
        return notification(info, "Not yet implemented", "This feature is not yet implemented");
    }

    @JsOverlay
    static Notification notification(Severity severity, String title, String description) {
        Notification notification = new Notification();
        notification.id = uuid();
        notification.read = false;
        notification.cleared = false;
        notification.timestamp = JsDate.now();
        notification.details = new JsArray<>();
        notification.severity = severity.name();
        notification.title = SafeHtmlUtils.htmlEscape(title);
        notification.description = SafeHtmlUtils.htmlEscape(description);
        return notification;
    }

    // ------------------------------------------------------ instance

    public String id;
    public String severity; // String instead of Severity to make this a native JavaScript object
    public String title;
    public String description;
    public JsArray<NotificationDetails> details;
    public double timestamp;
    public boolean read;
    public boolean cleared;

    // ------------------------------------------------------ builder

    @JsOverlay
    public final Notification details(String details) {
        this.details.push(notificationDetails(details, false));
        return this;
    }

    @JsOverlay
    public final Notification details(String details, boolean preformatted) {
        this.details.push(notificationDetails(details, preformatted));
        return this;
    }

    // ------------------------------------------------------ api

    @JsOverlay
    public final Severity severity() {
        return Severity.of(severity);
    }

    @JsOverlay
    public final double age() {
        return JsDate.now() - timestamp;
    }
}
