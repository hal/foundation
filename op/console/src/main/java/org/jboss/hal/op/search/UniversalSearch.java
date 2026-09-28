package org.jboss.hal.op.search;

import elemental2.dom.KeyboardEvent;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.EventType.keydown;
import static org.jboss.hal.op.search.UniversalSearchBox.universalSearchBox;

public class UniversalSearch {

    // ------------------------------------------------------ api

    public static void registerUniversalSearch() {
        UniversalSearchEvent.listen(document.body, () -> universalSearchBox().show());
        document.addEventListener(keydown.name, event -> {
            KeyboardEvent keyboardEvent = (KeyboardEvent) event;
            if ("k".equals(keyboardEvent.key) && (keyboardEvent.metaKey || keyboardEvent.ctrlKey)) {
                String tagName = document.activeElement != null ? document.activeElement.tagName : "";
                if (!"INPUT".equals(tagName) && !"TEXTAREA".equals(tagName)) {
                    keyboardEvent.preventDefault();
                    UniversalSearchEvent.dispatch(document.body);
                }
            }
        });
    }

    // ------------------------------------------------------ internal

    static void search(String value) {

    }
}
