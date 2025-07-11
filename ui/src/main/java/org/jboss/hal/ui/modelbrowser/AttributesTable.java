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

import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.resources.HalClasses;
import org.jboss.hal.resources.Keys;
import org.patternfly.component.emptystate.EmptyState;
import org.patternfly.component.table.TableType;
import org.patternfly.component.table.Tbody;
import org.patternfly.component.table.Tr;
import org.patternfly.core.ObservableValue;
import org.patternfly.filter.Filter;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.isAttached;
import static org.jboss.hal.resources.HalClasses.halModifier;
import static org.jboss.hal.ui.BuildingBlocks.emptyRow;
import static org.jboss.hal.ui.modelbrowser.AttributesToolbar.attributesToolbar;
import static org.patternfly.component.table.Table.table;
import static org.patternfly.component.table.Tbody.tbody;
import static org.patternfly.component.table.Th.th;
import static org.patternfly.component.table.Thead.thead;
import static org.patternfly.component.table.Tr.tr;
import static org.patternfly.core.ObservableValue.ov;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Width.width10;
import static org.patternfly.style.Width.width20;
import static org.patternfly.style.Width.width60;

class AttributesTable implements IsElement<HTMLElement> {

    private static final Logger logger = Logger.getLogger(AttributesTable.class.getName());
    private final Filter<AttributeDescription> filter;
    private final ObservableValue<Integer> visible;
    private final ObservableValue<Integer> total;
    private final Tbody tbody;
    private final HTMLElement root;
    private EmptyState noAttributes;

    AttributesTable(Metadata metadata) {
        filter = new AttributesFilter().onChange(this::onFilterChanged);
        visible = ov(metadata.resourceDescription().attributes().size());
        total = ov(metadata.resourceDescription().attributes().size());
        boolean anyComplexAttributes = metadata.resourceDescription()
                .attributes()
                .stream()
                .anyMatch(AttributeDescription::listOrObjectValueType);
        root = div()
                .add(attributesToolbar(filter, visible, total))
                .add(table(anyComplexAttributes ? TableType.treeTable : TableType.table)
                        .addHead(thead().css(util("mt-sm"))
                                .addRow(tr("attributes-head")
                                        .addItem(th("name").width(width60).text("Name"))
                                        .addItem(th("type").width(width20).text("Type"))
                                        .addItem(th("storage").width(width10).text("Storage"))
                                        .addItem(th("access-type").width(width10).text("Access"))))
                        .addBody(tbody = tbody()
                                .addRows(metadata.resourceDescription().attributes(), attribute ->
                                        new AttributeRow(metadata.resourceDescription(), anyComplexAttributes)
                                                .apply(attribute)
                                                .store(Keys.ATTRIBUTE_DESCRIPTION, attribute))))
                .element();
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    private void noAttributes() {
        if (noAttributes == null) {
            noAttributes = emptyRow(filter);
        }
        if (!isAttached(noAttributes)) {
            tbody.empty(4, noAttributes);
        }
    }

    private void onFilterChanged(Filter<AttributeDescription> filter, String origin) {
        logger.debug("Filter attributes: %s", filter);
        int matchingItems;
        if (filter.defined()) {
            matchingItems = 0;
            for (Tr tr : tbody.items()) {
                AttributeDescription ad = tr.get(Keys.ATTRIBUTE_DESCRIPTION);
                if (ad != null) {
                    boolean match = filter.match(ad);
                    tr.classList().toggle(halModifier(HalClasses.filtered), !match);
                    if (match) {
                        matchingItems++;
                    }
                }
            }
            if (matchingItems == 0) {
                noAttributes();
            } else {
                tbody.clearEmpty();
            }
        } else {
            matchingItems = total.get();
            tbody.clearEmpty();
            tbody.items().forEach(dlg -> dlg.classList().remove(halModifier(HalClasses.filtered)));
        }
        visible.set(matchingItems);
    }
}
