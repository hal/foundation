package org.jboss.hal.ui.resource.shell;

import org.jboss.elemento.ElementAttributeMethods;
import org.jboss.elemento.ElementClassListMethods;
import org.jboss.elemento.ElementConsumerMethods;
import org.jboss.elemento.ElementContainerMethods;
import org.jboss.elemento.ElementEventMethods;
import org.jboss.elemento.ElementIdMethods;
import org.jboss.elemento.ElementQueryMethods;
import org.jboss.elemento.ElementTextDelegate;
import org.jboss.elemento.HTMLElementAttributeMethods;
import org.jboss.elemento.HTMLElementDataMethods;
import org.jboss.elemento.HTMLElementStyleMethods;
import org.jboss.elemento.HTMLElementVisibilityMethods;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.patternfly.component.ComponentIconAndText;
import org.patternfly.component.IconPosition;
import org.patternfly.component.content.Content;
import org.patternfly.component.title.Title;
import org.patternfly.core.OuiaSupport;
import org.patternfly.layout.flex.FlexItem;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.removeChildrenFrom;
import static org.jboss.hal.ui.brick.StabilityLabel.stabilityLabel;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.Gap.sm;
import static org.patternfly.style.Size._3xl;

/**
 * Displays the name, stability label, and description of a WildFly management resource.
 * <p>
 * By default, the title is derived from the template's last segment value, the stability label is shown when the environment
 * requires highlighting, and the description is taken from the resource metadata.
 */
public class ResourceHeader implements
        ComponentIconAndText<HTMLElement, ResourceHeader>,
        ElementAttributeMethods<HTMLElement, ResourceHeader>,
        ElementClassListMethods<HTMLElement, ResourceHeader>,
        ElementConsumerMethods<HTMLElement, ResourceHeader>,
        ElementContainerMethods<HTMLElement, ResourceHeader>,
        ElementEventMethods<HTMLElement, ResourceHeader>,
        ElementIdMethods<HTMLElement, ResourceHeader>,
        ElementQueryMethods<HTMLElement>,
        ElementTextDelegate<HTMLElement, ResourceHeader>,
        HTMLElementAttributeMethods<HTMLElement, ResourceHeader>,
        HTMLElementDataMethods<HTMLElement, ResourceHeader>,
        HTMLElementStyleMethods<HTMLElement, ResourceHeader>,
        HTMLElementVisibilityMethods<HTMLElement, ResourceHeader>,
        OuiaSupport<HTMLElement, ResourceHeader> {

    // ------------------------------------------------------ factory

    public static ResourceHeader resourceHeader(AddressTemplate template, Metadata metadata) {
        return new ResourceHeader(template, metadata);
    }

    // ------------------------------------------------------ instance

    private final AddressTemplate template;
    private final Metadata metadata;
    private final Content root;
    private HTMLElement textElement;

    ResourceHeader(AddressTemplate template, Metadata metadata) {
        this.template = template;
        this.metadata = metadata;

        Stability stability = metadata.resourceDescription().stability();
        this.root = content()
                .add(flex().alignItems(center)
                        .addItem(flexItem().add(textElement = Title.title(1, _3xl).element()))
                        .run(f -> {
                            if (uic().environment().highlightStability(stability)) {
                                f.addItem(flexItem().add(stabilityLabel(stability)));
                            }
                        }));
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/ResourceHeader";
    }

    @Override
    public Element textDelegate() {
        return textElement;
    }

    @Override
    public HTMLElement element() {
        return root.element();
    }

    // ------------------------------------------------------ add

    public ResourceHeader addDescription() {
        return addDescription(metadata.resourceDescription().description());
    }

    public ResourceHeader addDescription(String text) {
        return addDescription(p().text(text).element());
    }

    public ResourceHeader addDescription(HTMLElement element) {
        return add(element);
    }

    // ------------------------------------------------------ builder

    public ResourceHeader defaultText() {
        return text(template.isEmpty() ? "Management Model" : template.last().value);
    }

    @Override
    public ResourceHeader iconAndText(Element icon, String text, IconPosition iconPosition) {
        removeChildrenFrom(textElement);
        FlexItem iconFi = flexItem().add(icon);
        FlexItem textFi = flexItem().add(text);
        this.textElement.appendChild(flex().alignItems(center).gap(sm)
                .run(f -> {
                    switch (iconPosition) {
                        case start -> {
                            f.addItem(iconFi);
                            f.addItem(textFi);
                        }
                        case end -> {
                            f.addItem(textFi);
                            f.addItem(iconFi);
                        }
                    }
                })
                .element());
        this.textElement = textFi.element();
        return this;
    }

    @Override
    public ResourceHeader that() {
        return this;
    }
}
