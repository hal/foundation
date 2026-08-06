package org.jboss.hal.ui.resource.event;

import java.util.function.Consumer;

import org.jboss.hal.event.UIEvent;
import org.jboss.hal.meta.AddressTemplate;

import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.HTMLElement;

/**
 * Umbrella interface for custom resource shell events.
 * <p>
 * <strong>Please note</strong>
 * <br/> The events must only be triggered by elements which are part of the resource shell DOM.
 */
public interface ResourceEvents {

    // ------------------------------------------------------ modified

    /** Event dispatched when a resource was modified. */
    interface Modified extends UIEvent {

        String TYPE = UIEvent.type("resource-shell", "modified");

        /** Event payload carrying an optional identifier, parent identifier, and/or address template. */
        class Details {

            public AddressTemplate template;
        }

        /** Dispatches a select-in-tree event targeting the given address template. */
        static void dispatch(HTMLElement source, AddressTemplate template) {
            Details details = new Details();
            details.template = template;
            dispatch(source, details, true);
        }

        /** Dispatches a select-in-tree event targeting the given address template. */
        static void dispatch(HTMLElement source, AddressTemplate template, boolean bubbles) {
            Details details = new Details();
            details.template = template;
            dispatch(source, details, bubbles);
        }

        private static void dispatch(HTMLElement source, Details details, boolean bubbles) {
            CustomEventInit<Details> init = CustomEventInit.create();
            init.setBubbles(bubbles);
            init.setCancelable(true);
            init.setDetail(details);
            CustomEvent<Details> event = new CustomEvent<>(TYPE, init);
            source.dispatchEvent(event);
        }

        /** Registers a listener for select-in-tree events on the given element. */
        @SuppressWarnings("unchecked")
        static void listen(HTMLElement element, Consumer<Details> listener) {
            element.addEventListener(TYPE, event -> {
                CustomEvent<Details> customEvent = (CustomEvent<Details>) event;
                listener.accept(customEvent.detail);
            });
        }
    }
}
