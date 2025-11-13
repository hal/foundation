package org.jboss.hal.op.skeleton;

import org.jboss.elemento.IsElement;
import org.patternfly.component.inputgroup.InputGroup;
import org.patternfly.component.inputgroup.InputGroupItem;

import elemental2.dom.HTMLElement;

import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;
import static org.patternfly.icon.IconSets.fas.search;

class GlobalSearch implements IsElement<HTMLElement> {

    static GlobalSearch globalSearch() {
        return new GlobalSearch();
    }

    private final InputGroup inputGroup;
    private final InputGroupItem searchButtonItem;

    GlobalSearch() {
        inputGroup = inputGroup();
        inputGroup.addItem(searchButtonItem = inputGroupItem().addButton(button(search()).plain()));
    }

    @Override
    public HTMLElement element() {
        return inputGroup.element();
    }
}
