package org.jboss.hal.op.search;

import org.jboss.elemento.Callback;
import org.jboss.hal.event.UIEvent;

import elemental2.dom.CustomEvent;
import elemental2.dom.CustomEventInit;
import elemental2.dom.HTMLElement;

public interface UniversalSearchEvent extends UIEvent {

    String TYPE = UIEvent.type("universal-search");

    static void dispatch(HTMLElement source) {
        dispatch(source, true);
    }

    static void dispatch(HTMLElement source, boolean bubbles) {
        CustomEventInit<UniversalSearchEvent> init = CustomEventInit.create();
        init.setBubbles(bubbles);
        init.setCancelable(true);
        CustomEvent<UniversalSearchEvent> event = new CustomEvent<>(TYPE, init);
        source.dispatchEvent(event);
    }

    static void listen(HTMLElement element, Callback listener) {
        element.addEventListener(TYPE, event -> listener.call());
    }
}
