package org.jboss.hal.ui.resource;

import org.jboss.hal.core.LabelBuilder;
import org.jboss.hal.dmr.ModelType;
import org.patternfly.component.help.HelperText;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.resources.HalClasses.curlyBraces;
import static org.jboss.hal.resources.HalClasses.defaultValue;
import static org.jboss.hal.resources.HalClasses.dollar;
import static org.jboss.hal.resources.HalClasses.expression;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.name;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.ValidationStatus.warning;
import static org.patternfly.component.help.HelperText.helperText;
import static org.patternfly.component.help.HelperTextItem.helperTextItem;
import static org.patternfly.layout.flex.Display.inlineFlex;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.FlexItem.flexItem;
import static org.patternfly.layout.flex.SpaceItems.sm;
import static org.patternfly.style.Classes.end;
import static org.patternfly.style.Classes.start;

class HelperTexts {

    static HelperText unsupported() {
        return helperText().addItem(helperTextItem("The type of this attribute type is not supported.", warning));
    }

    static HelperText required(ResourceAttribute ra) {
        String label = new LabelBuilder().label(ra.name);
        return helperText(label + " is a required attribute.", error);
    }

    static HelperText notNumeric(ModelType type) {
        return helperText("The value is not a number. Only values of type " + type.name() + " are allowed.", error);
    }

    static HelperText notInRange(String min, String max) {
        return helperText("The value is out of range. The value must be >= " + min + " and <= " + max + ".", error);
    }

    static HelperText noExpression() {
        return helperText()
                .addItem(helperTextItem("The value is not a valid expression.", error))
                .addItem(helperTextItem()
                        .add(flex().display(inlineFlex).spaceItems(sm)
                                .addItem(flexItem().add("Expressions must follow the pattern:"))
                                .addItem(flexItem()
                                        // BuildingBlocks.renderExpression() won't work because of '<>' and '[]' in
                                        // [<prefix>][${<system-property-name>[:<default-value>]}][<suffix>]*
                                        .add(span().css(halComponent(expression))
                                                .add(span().css(halComponent(expression, defaultValue))
                                                        .textContent("[prefix]"))
                                                .add(span().css(halComponent(expression, dollar)))
                                                .add(span().css(halComponent(expression, curlyBraces, start)))
                                                .add(span().css(halComponent(expression, name))
                                                        .textContent("<system-property-name>"))
                                                .add(span().css(halComponent(expression, defaultValue))
                                                        .textContent("[:<default-value>]"))
                                                .add(span().css(halComponent(expression, curlyBraces, end)))
                                                .add(span().css(halComponent(expression, defaultValue))
                                                        .textContent("[<suffix>]*"))))));
    }
}
