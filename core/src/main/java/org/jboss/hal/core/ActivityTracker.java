package org.jboss.hal.core;

import org.jboss.elemento.logger.Logger;

import elemental2.dom.EventListener;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.setTimeout;
import static elemental2.dom.DomGlobal.window;

/**
 * ActivityTracker is a utility class designed to track the active or inactive state of a browser tab. It listens for events
 * related to focus, blur, and visibility changes in the document or window and allows users to define custom behavior during
 * transitions between active and inactive states.
 */
public class ActivityTracker {

    private static final Logger logger = Logger.getLogger(ActivityTracker.class.getName());

    private final Runnable onActive;
    private final Runnable onInactive;
    private final EventListener eventListener;
    private boolean isActive;

    public ActivityTracker(Runnable onActive, Runnable onInactive) {
        this.onActive = onActive;
        this.onInactive = onInactive;
        this.eventListener = evt -> updateState();
        this.isActive = computeIsActive();
    }

    private void updateState() {
        // Edge Case: Defer evaluation using setTimeout(0) to allow
        // document.hidden and document.hasFocus() to settle after blur/focus events
        setTimeout(__ -> {
            boolean newState = computeIsActive();

            // Edge Case: Deduplicate redundant triggers from focus + visibilitychange
            if (newState != isActive) {
                isActive = newState;
                if (isActive) {
                    if (onActive != null) {
                        logger.debug("Application is active");
                        onActive.run();
                    }
                } else {
                    if (onInactive != null) {
                        logger.debug("Application is inactive");
                        onInactive.run();
                    }
                }
            }
        }, 0);
    }

    private boolean computeIsActive() {
        // Tab is hidden/minimized
        if (document.hidden) {
            return false;
        }

        // Edge Case: The active element is an iframe
        if (document.activeElement != null
                && "IFRAME".equalsIgnoreCase(document.activeElement.tagName)) {
            return true;
        }

        // Fall back to native document focus
        return document.hasFocus();
    }

    public void start() {
        window.addEventListener("focus", eventListener);
        window.addEventListener("blur", eventListener);
        document.addEventListener("visibilitychange", eventListener);
    }

    public void stop() {
        window.removeEventListener("focus", eventListener);
        window.removeEventListener("blur", eventListener);
        document.removeEventListener("visibilitychange", eventListener);
    }
}