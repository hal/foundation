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

import java.util.function.Consumer;

import org.jboss.hal.event.UIEvent;
import org.jboss.hal.meta.AddressTemplate;

import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.HTMLElement;

/**
 * Umbrella interface for custom model browser UI events.
 * <p>
 * <strong>Please note</strong>
 * <br/> The events must only be triggered by elements which are part of the model browser DOM.
 */
public interface ModelBrowserEvents {

    // ------------------------------------------------------ add

    interface AddResource extends UIEvent {

        String TYPE = UIEvent.type("model-browser", "add");

        class Details {

            AddressTemplate parent;
            String child;
            boolean singleton;
        }

        /**
         * Creates and returns a custom event to add a singleton resource.
         *
         * @param source the source element used to dispatch the event.
         * @param parent the parent address used to create the event's detail.
         * @param child  the address used to create the event's detail.
         */
        static void dispatch(HTMLElement source, AddressTemplate parent, String child, boolean singleton) {
            Details details = new Details();
            details.parent = parent;
            details.child = child;
            details.singleton = singleton;
            //noinspection DuplicatedCode
            CustomEventInit<Details> init = CustomEventInit.create();
            init.setBubbles(true);
            init.setCancelable(true);
            init.setDetail(details);
            CustomEvent<Details> event = new CustomEvent<>(TYPE, init);
            source.dispatchEvent(event);
        }

        @SuppressWarnings("unchecked")
        static void listen(HTMLElement element, Consumer<Details> listener) {
            element.addEventListener(TYPE, event -> {
                CustomEvent<Details> customEvent = (CustomEvent<Details>) event;
                listener.accept(customEvent.detail);
            });
        }
    }

    interface DeleteResource extends UIEvent {

        String TYPE = UIEvent.type("model-browser", "delete");

        class Details {

            AddressTemplate template;
        }

        /**
         * Creates and returns a custom event to delete a singleton resource.
         *
         * @param source   the source element used to dispatch the event.
         * @param template the address template to delete.
         */
        static void dispatch(HTMLElement source, AddressTemplate template) {
            Details details = new Details();
            details.template = template;
            //noinspection DuplicatedCode
            CustomEventInit<Details> init = CustomEventInit.create();
            init.setBubbles(true);
            init.setCancelable(true);
            init.setDetail(details);
            CustomEvent<Details> event = new CustomEvent<>(TYPE, init);
            source.dispatchEvent(event);
        }

        @SuppressWarnings("unchecked")
        static void listen(HTMLElement element, Consumer<Details> listener) {
            element.addEventListener(TYPE, event -> {
                CustomEvent<Details> customEvent = (CustomEvent<Details>) event;
                listener.accept(customEvent.detail);
            });
        }
    }

    // ------------------------------------------------------ select

    interface SelectInTree extends UIEvent {

        String TYPE = UIEvent.type("model-browser", "select-in-tree");

        class Details {

            String identifier;
            String parentIdentifier;
            AddressTemplate template;
        }

        static void dispatch(HTMLElement source, AddressTemplate template) {
            Details details = new Details();
            details.template = template;
            dispatch(source, details);
        }

        static void dispatch(HTMLElement source, String identifier) {
            Details details = new Details();
            details.identifier = identifier;
            dispatch(source, details);
        }

        static void dispatch(HTMLElement source, String parentIdentifier, String childIdentifier) {
            Details details = new Details();
            details.parentIdentifier = parentIdentifier;
            details.identifier = childIdentifier;
            dispatch(source, details);
        }

        private static void dispatch(HTMLElement source, Details details) {
            //noinspection DuplicatedCode
            CustomEventInit<Details> init = CustomEventInit.create();
            init.setBubbles(true);
            init.setCancelable(true);
            init.setDetail(details);
            CustomEvent<Details> event = new CustomEvent<>(TYPE, init);
            source.dispatchEvent(event);
        }

        @SuppressWarnings("unchecked")
        static void listen(HTMLElement element, Consumer<Details> listener) {
            element.addEventListener(TYPE, event -> {
                CustomEvent<Details> customEvent = (CustomEvent<Details>) event;
                listener.accept(customEvent.detail);
            });
        }
    }
}
