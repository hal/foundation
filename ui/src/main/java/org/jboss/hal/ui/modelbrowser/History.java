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

import java.util.Stack;

/**
 * Stack-based navigation history supporting back, forward, and current item tracking.
 * <p>
 * Used by the model browser tree to enable browser-like back/forward navigation between previously visited tree view items.
 * Navigating to a new item clears the forward stack.
 *
 * @param <T> the type of items tracked in the history
 */
class History<T> {

    private final Stack<T> backStack;
    private final Stack<T> forwardStack;
    private boolean back;
    private boolean forward;
    private T current;

    History() {
        backStack = new Stack<>();
        forwardStack = new Stack<>();
        back = false;
        forward = false;
        current = null;
    }

    /** Records a new navigation, pushing the current item onto the back stack and clearing the forward stack. */
    void navigate(T item) {
        if (current != null) {
            backStack.push(current);
        }
        current = item;
        forwardStack.clear();
        back = !backStack.isEmpty();
        forward = false;
    }

    /** Returns {@code true} if there are items in the back stack. */
    boolean canGoBack() {
        return back;
    }

    /** Moves back one step, pushing the current item onto the forward stack. Returns the new current item. */
    T back() {
        if (!backStack.isEmpty()) {
            if (current != null) {
                forwardStack.push(current);
            }
            current = backStack.pop();
            back = !backStack.isEmpty();
            forward = true;
        } else {
            back = false;
            forward = !forwardStack.isEmpty();
        }
        return current;
    }

    /** Returns the top of the back stack without modifying the history, or the current item if the stack is empty. */
    T peekBack() {
        if (!backStack.isEmpty()) {
            return backStack.peek();
        }
        return current;
    }

    /** Returns {@code true} if there are items in the forward stack. */
    boolean canGoForward() {
        return forward;
    }

    /** Moves forward one step, pushing the current item onto the back stack. Returns the new current item. */
    T forward() {
        if (!forwardStack.isEmpty()) {
            if (current != null) {
                backStack.push(current);
            }
            current = forwardStack.pop();
            back = true;
            forward = !forwardStack.isEmpty();
        } else {
            back = !backStack.isEmpty();
            forward = false;
        }
        return current;
    }

    /** Returns the top of the forward stack without modifying the history, or the current item if the stack is empty. */
    T peekForward() {
        if (!forwardStack.isEmpty()) {
            return forwardStack.peek();
        }
        return current;
    }

    /** Returns the current item, or {@code null} if nothing has been navigated to yet. */
    T current() {
        return current;
    }
}
