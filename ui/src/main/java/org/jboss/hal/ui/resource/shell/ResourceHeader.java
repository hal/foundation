package org.jboss.hal.ui.resource.shell;

import org.jboss.elemento.ElementAttributeMethods;
import org.jboss.elemento.ElementClassListMethods;
import org.jboss.elemento.ElementConsumerMethods;
import org.jboss.elemento.ElementContainerDelegate;
import org.jboss.elemento.ElementEventMethods;
import org.jboss.elemento.ElementIdMethods;
import org.jboss.elemento.ElementQueryMethods;
import org.jboss.elemento.HTMLElementAttributeMethods;
import org.jboss.elemento.HTMLElementDataMethods;
import org.jboss.elemento.HTMLElementStyleMethods;
import org.jboss.elemento.HTMLElementVisibilityMethods;
import org.jboss.hal.env.Stability;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.patternfly.component.ComponentIcon;
import org.patternfly.component.content.Content;
import org.patternfly.component.divider.Divider;
import org.patternfly.component.icon.Icon;
import org.patternfly.component.label.Label;
import org.patternfly.core.OuiaSupport;
import org.patternfly.layout.flex.Flex;
import org.patternfly.layout.flex.FlexItem;
import org.patternfly.layout.split.Split;
import org.patternfly.layout.split.SplitItem;

import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.failSafeRemoveFromParent;
import static org.jboss.elemento.Elements.insertFirst;
import static org.jboss.hal.ui.UIContext.uic;
import static org.jboss.hal.ui.brick.StabilityLabel.stabilityLabel;
import static org.patternfly.component.content.Content.content;
import static org.patternfly.component.content.ContentType.h1;
import static org.patternfly.component.content.ContentType.p;
import static org.patternfly.component.divider.Divider.divider;
import static org.patternfly.component.divider.DividerType.hr;
import static org.patternfly.component.icon.IconSize._2xl;
import static org.patternfly.layout.flex.AlignSelf.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.FlexShorthand._1;
import static org.patternfly.layout.split.Split.split;
import static org.patternfly.layout.split.SplitItem.splitItem;
import static org.patternfly.style.Classes.util;
import static org.patternfly.style.Orientation.vertical;

/**
 * Displays the name, stability label, and description of a WildFly management resource.
 * <p>
 * By default, the title is derived from the template's last segment value, the stability label is shown when the environment
 * requires highlighting, and the description is taken from the resource metadata.
 */
// TODO Replace with org.patternfly.componentgroup.pageheader.PageHeader
public class ResourceHeader implements
        ComponentIcon<HTMLElement, ResourceHeader>,
        ElementAttributeMethods<HTMLElement, ResourceHeader>,
        ElementClassListMethods<HTMLElement, ResourceHeader>,
        ElementConsumerMethods<HTMLElement, ResourceHeader>,
        ElementContainerDelegate<HTMLElement, ResourceHeader>,
        ElementEventMethods<HTMLElement, ResourceHeader>,
        ElementIdMethods<HTMLElement, ResourceHeader>,
        ElementQueryMethods<HTMLElement>,
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
    private final Flex root;
    private final FlexItem body;
    private final Split split;
    private final Content header;
    private FlexItem iconContainer;
    private Divider divider;
    private SplitItem labelsContainer;

    ResourceHeader(AddressTemplate template, Metadata metadata) {
        this.template = template;
        this.metadata = metadata;
        this.root = flex()
                .addItem(body = flexItem().flex(_1)
                        .add(split = split().gutter()
                                .addItem(splitItem().add(header = content(h1).css(util("mb-sm"))))));

        Stability stability = metadata.resourceDescription().stability();
        if (uic().environment().highlightStability(stability)) {
            addLabel(stabilityLabel(stability).element());
        }
    }

    @Override
    public String ouiaComponentType() {
        return "halOP/ResourceHeader";
    }

    @Override
    public Element containerDelegate() {
        return body.element();
    }

    @Override
    public HTMLElement element() {
        return root.element();
    }

    // ------------------------------------------------------ add

    public ResourceHeader addHeader(String header) {
        this.header.text(header);
        return this;
    }

    public ResourceHeader addHeader(HTMLElement header) {
        this.header.add(header);
        return this;
    }

    public ResourceHeader addDescription(String description) {
        return addDescription(content(p).text(description).element());
    }

    public ResourceHeader addDescription(HTMLElement description) {
        return add(description);
    }

    public ResourceHeader addLabel(Label label) {
        return addLabel(label.element());
    }

    public ResourceHeader addLabel(HTMLElement label) {
        if (labelsContainer == null) {
            labelsContainer = splitItem();
            split.addItem(labelsContainer);
        }
        labelsContainer.add(label);
        return this;
    }

    // ------------------------------------------------------ builder

    public ResourceHeader defaultHeader() {
        return addHeader(template.isEmpty() ? "Management Model" : template.last().value);
    }

    public ResourceHeader defaultDescription() {
        return addDescription(metadata.resourceDescription().description());
    }

    @Override
    public ResourceHeader icon(Element icon) {
        removeIcon();
        divider = divider(hr).orientation(vertical);
        iconContainer = flexItem().alignSelf(center)
                .add(Icon.icon(icon).size(_2xl));
        insertFirst(root.element(), divider);
        insertFirst(root.element(), iconContainer);
        return this;
    }

    @Override
    public ResourceHeader removeIcon() {
        failSafeRemoveFromParent(iconContainer);
        failSafeRemoveFromParent(divider);
        iconContainer = null;
        divider = null;
        return this;
    }

    @Override
    public ResourceHeader that() {
        return this;
    }
}
