package org.jboss.hal.op.configuration.security;

import java.util.List;

import org.jboss.elemento.IsElement;
import org.patternfly.component.list.DataList;
import org.patternfly.component.list.DataListAction;
import org.patternfly.icon.IconSets;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.emptystate.EmptyState.emptyState;
import static org.patternfly.component.list.DataList.dataList;
import static org.patternfly.component.list.DataListAction.dataListAction;
import static org.patternfly.component.list.DataListCell.dataListCell;
import static org.patternfly.component.list.DataListItem.dataListItem;
import static org.patternfly.icon.IconSets.fas.penToSquare;
import static org.patternfly.icon.IconSets.fas.trash;
import static org.patternfly.layout.bullseye.Bullseye.bullseye;

class AliasList implements IsElement<HTMLElement> {

    // ------------------------------------------------------ factory

    static AliasList aliasList(boolean supportsSetSecret) {
        return new AliasList(supportsSetSecret);
    }

    // ------------------------------------------------------ instance

    private final boolean supportsSetSecret;
    private final HTMLElement root;
    private DataList dataList;

    AliasList(boolean supportsSetSecret) {
        this.supportsSetSecret = supportsSetSecret;
        this.root = div().element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    void show(List<String> aliases) {
        removeChildrenFrom(root);
        if (aliases.isEmpty()) {
            root.appendChild(bullseye()
                    .add(emptyState()
                            .icon(IconSets.fas.key())
                            .text("No aliases found")
                            .headingLevel(2))
                    .element());
        } else {
            dataList = dataList();
            for (String alias : aliases) {
                dataList.add(dataListItem(alias)
                        .addCell(dataListCell().text(alias))
                        .addAction(actions()));
            }
            root.appendChild(dataList.element());
        }
    }

    // ------------------------------------------------------ internal

    private DataListAction actions() {
        DataListAction action = dataListAction(true);
        if (supportsSetSecret) {
            action.add(button().plain().icon(penToSquare()));
        }
        action.add(button().plain().icon(trash()));
        return action;
    }
}
