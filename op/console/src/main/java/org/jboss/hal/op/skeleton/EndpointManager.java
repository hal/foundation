package org.jboss.hal.op.skeleton;

import org.jboss.elemento.IsElement;
import org.patternfly.component.menu.Dropdown;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.menu.Dropdown.dropdown;

class EndpointManager implements IsElement<HTMLElement> {

    static EndpointManager endpointManager() {
        return new EndpointManager();
    }

    private final Dropdown dropdown;

    EndpointManager() {
        dropdown = dropdown("NYI");
    }

    @Override
    public HTMLElement element() {
        return dropdown.element();
    }
}
