package org.jboss.hal.ui.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.elemento.ElementAttributeMethods;
import org.jboss.elemento.ElementClassListMethods;
import org.jboss.elemento.ElementConsumerMethods;
import org.jboss.elemento.ElementContainerMethods;
import org.jboss.elemento.ElementEventMethods;
import org.jboss.elemento.ElementIdMethods;
import org.jboss.elemento.ElementQueryMethods;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.HTMLElementAttributeMethods;
import org.jboss.elemento.HTMLElementDataMethods;
import org.jboss.elemento.HTMLElementStyleMethods;
import org.jboss.elemento.HTMLElementVisibilityMethods;
import org.patternfly.core.AddItemHandler;
import org.patternfly.core.AurHandler;
import org.patternfly.core.HasItems;
import org.patternfly.core.OuiaSupport;
import org.patternfly.core.RemoveItemHandler;
import org.patternfly.core.UpdateItemHandler;
import org.patternfly.handler.SelectHandler;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLUListElement;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.elemento.Elements.ul;

public class VerticalTabs implements
        ElementAttributeMethods<HTMLElement, VerticalTabs>,
        ElementClassListMethods<HTMLElement, VerticalTabs>,
        ElementConsumerMethods<HTMLElement, VerticalTabs>,
        ElementContainerMethods<HTMLElement, VerticalTabs>,
        ElementEventMethods<HTMLElement, VerticalTabs>,
        ElementIdMethods<HTMLElement, VerticalTabs>,
        ElementQueryMethods<HTMLElement>,
        HTMLElementAttributeMethods<HTMLElement, VerticalTabs>,
        HTMLElementDataMethods<HTMLElement, VerticalTabs>,
        HTMLElementStyleMethods<HTMLElement, VerticalTabs>,
        HTMLElementVisibilityMethods<HTMLElement, VerticalTabs>,
        HasItems<HTMLElement, VerticalTabs, VerticalTab>,
        OuiaSupport<HTMLElement, VerticalTabs> {

    // ------------------------------------------------------ factory

    public static VerticalTabs verticalTabs() {
        return new VerticalTabs();
    }

    // ------------------------------------------------------ instance

    private final HTMLContainerBuilder<HTMLUListElement> root;
    private final Map<String, VerticalTab> items;
    private final AurHandler<VerticalTabs, VerticalTab> aur;
    private final List<SelectHandler<VerticalTab>> selectHandler;

    VerticalTabs() {
        this.root = ul().css("vertical-tabs-pf");
        this.items = new LinkedHashMap<>();
        this.selectHandler = new ArrayList<>();
        this.aur = new AurHandler<>(this);
    }

    @Override
    public String ouiaComponentType() {
        return "hal/VerticalTabs";
    }

    @Override
    public HTMLElement element() {
        return root.element();
    }

    // ------------------------------------------------------ add

    @Override
    public VerticalTabs add(VerticalTab item) {
        item.verticalTabs = this;
        items.put(item.identifier(), item);
        root.add(item);
        return aur.added(item);
    }

    // ------------------------------------------------------ builder

    @Override
    public VerticalTabs that() {
        return this;
    }

    // ------------------------------------------------------ events

    @Override
    public VerticalTabs onAdd(AddItemHandler<VerticalTabs, VerticalTab> onAdd) {
        return aur.onAdd(onAdd);
    }

    @Override
    public VerticalTabs onUpdate(UpdateItemHandler<VerticalTabs, VerticalTab> onUpdate) {
        return aur.onUpdate(onUpdate);
    }

    @Override
    public VerticalTabs onRemove(RemoveItemHandler<VerticalTabs, VerticalTab> onRemove) {
        return aur.onRemove(onRemove);
    }

    public VerticalTabs onSelect(SelectHandler<VerticalTab> selectHandler) {
        this.selectHandler.add(selectHandler);
        return this;
    }

    // ------------------------------------------------------ api

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains(String identifier) {
        return items.containsKey(identifier);
    }

    @Override
    public VerticalTab item(String identifier) {
        return items.get(identifier);
    }

    @Override
    public void updateItem(VerticalTab item) {
        replaceItemElement(item, (oldItem, newItem) -> {
            items.put(newItem.identifier(), newItem);
            aur.updated(oldItem, newItem);
        });
    }

    @Override
    public void removeItem(String identifier) {
        VerticalTab item = items.remove(identifier);
        failSafeRemoveFromParent(item);
        aur.removed(item);

    }

    @Override
    public void clear() {
        removeChildrenFrom(root);
        Iterator<VerticalTab> iterator = items.values().iterator();
        while (iterator.hasNext()) {
            VerticalTab item = iterator.next();
            iterator.remove();
            aur.removed(item);
        }
    }

    @Override
    public Iterator<VerticalTab> iterator() {
        return items.values().iterator();
    }

    public void select(String identifier) {
        select(item(identifier), true, true);
    }

    public void select(String identifier, boolean selected) {
        select(item(identifier), selected, true);
    }

    public void select(String identifier, boolean selected, boolean fireEvent) {
        select(item(identifier), selected, fireEvent);
    }

    public void select(VerticalTab item) {
        select(item, true, true);
    }

    public void select(VerticalTab item, boolean selected) {
        select(item, selected, true);
    }

    public void select(VerticalTab item, boolean selected, boolean fireEvent) {
        if (item != null) {
            unselectAllItems();
            item.markSelected(selected);
            if (fireEvent) {
                selectHandler.forEach(selectHandler -> selectHandler.onSelect(new Event(""), item, selected));
            }
        }
    }

    // ------------------------------------------------------ internal

    private void unselectAllItems() {
        items.values().forEach(item -> item.classList().remove("active"));
    }
}
