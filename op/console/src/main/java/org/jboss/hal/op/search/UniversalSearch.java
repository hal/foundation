package org.jboss.hal.op.search;

import elemental2.dom.KeyboardEvent;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.elemento.EventType.keydown;
import static org.jboss.hal.op.search.UniversalSearchBox.universalSearchBox;

public class UniversalSearch {

    public static void registerShortcut() {
        document.addEventListener(keydown.name, event -> {
            KeyboardEvent ke = (KeyboardEvent) event;
            if ("k".equals(ke.key) && (ke.metaKey || ke.ctrlKey)) {
                String tagName = document.activeElement != null ? document.activeElement.tagName : "";
                if (!"INPUT".equals(tagName) && !"TEXTAREA".equals(tagName)) {
                    ke.preventDefault();
                    universalSearchBox().show();
                }
            }
        });
    }
}
