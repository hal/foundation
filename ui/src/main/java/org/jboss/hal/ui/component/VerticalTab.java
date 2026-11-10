package org.jboss.hal.ui.component;

import java.util.HashMap;
import java.util.Map;

import org.jboss.elemento.ElementAttributeMethods;
import org.jboss.elemento.ElementClassListMethods;
import org.jboss.elemento.ElementConsumerMethods;
import org.jboss.elemento.ElementContainerMethods;
import org.jboss.elemento.ElementEventMethods;
import org.jboss.elemento.ElementIdMethods;
import org.jboss.elemento.ElementQueryMethods;
import org.jboss.elemento.ElementTextDelegate;
import org.jboss.elemento.HTMLContainerBuilder;
import org.jboss.elemento.HTMLElementAttributeMethods;
import org.jboss.elemento.HTMLElementDataMethods;
import org.jboss.elemento.HTMLElementStyleMethods;
import org.jboss.elemento.HTMLElementVisibilityMethods;
import org.patternfly.core.ComponentContext;
import org.patternfly.core.HasIdentifier;
import org.patternfly.core.OuiaSupport;

import elemental2.dom.Element;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLLIElement;

import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.li;
import static org.jboss.elemento.EventType.click;

public class VerticalTab implements
        ComponentContext<HTMLElement, VerticalTab>,
        ElementAttributeMethods<HTMLElement, VerticalTab>,
        ElementClassListMethods<HTMLElement, VerticalTab>,
        ElementConsumerMethods<HTMLElement, VerticalTab>,
        ElementContainerMethods<HTMLElement, VerticalTab>,
        ElementEventMethods<HTMLElement, VerticalTab>,
        ElementIdMethods<HTMLElement, VerticalTab>,
        ElementQueryMethods<HTMLElement>,
        ElementTextDelegate<HTMLElement, VerticalTab>,
        HTMLElementAttributeMethods<HTMLElement, VerticalTab>,
        HTMLElementDataMethods<HTMLElement, VerticalTab>,
        HTMLElementStyleMethods<HTMLElement, VerticalTab>,
        HTMLElementVisibilityMethods<HTMLElement, VerticalTab>,
        HasIdentifier<HTMLElement, VerticalTab>,
        OuiaSupport<HTMLElement, VerticalTab> {

    // ------------------------------------------------------ factory

    public static VerticalTab verticalTab(String identifier) {
        return new VerticalTab(identifier);
    }

    public static VerticalTab verticalTab(String identifier, String text) {
        return new VerticalTab(identifier).text(text);
    }

    // ------------------------------------------------------ instance

    // VerticalTabs and VerticalTab are no real components
    // (they do not inherit from org.patternfly.component.BaseComponent resp. org.patternfly.component.SubComponent)
    // and cannot use the component store to store a reference to the parent.
    // That's why we use a direct reference that must be set in VerticalTabs.add()
    VerticalTabs verticalTabs;
    private final String identifier;
    private final Map<String, Object> data;
    private final HTMLContainerBuilder<HTMLLIElement> root;
    private final HTMLContainerBuilder<HTMLAnchorElement> link;

    VerticalTab(String identifier) {
        this.identifier = identifier;
        this.data = new HashMap<>();
        this.link = a().on(click, e -> {
            e.preventDefault();
            e.stopPropagation();
            verticalTabs.select(this);
        });
        this.root = li().css("vertical-tabs-pf-tab");
        root.add(link);
    }

    @Override
    public String ouiaComponentType() {
        return "hal/VerticalTab";
    }

    @Override
    public HTMLElement element() {
        return root.element();
    }

    @Override
    public Element textDelegate() {
        return link.element();
    }

    // ------------------------------------------------------ builder

    @Override
    public VerticalTab that() {
        return this;
    }

    // ------------------------------------------------------ api

    @Override
    public <T> VerticalTab store(String key, T value) {
        data.put(key, value);
        return this;
    }

    @Override
    public boolean has(String key) {
        return data.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    @Override
    public String identifier() {
        return identifier;
    }

    // ------------------------------------------------------ internal

    void markSelected(boolean selected) {
        root.classList().toggle("active", selected);
    }
}
