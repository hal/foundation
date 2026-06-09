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
package org.jboss.hal.ui.resource.form;
import org.jboss.hal.ui.resource.ResourceAttribute;

import org.jboss.hal.dmr.ModelType;
import org.patternfly.component.help.HelperText;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.core.Humanize.sentenceCase;
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

/** Factory methods for validation helper texts displayed below form items when validation fails. */
public class HelperTexts {

    /** Returns a warning helper text indicating that the attribute type is not yet supported. */
    static HelperText unsupportedType(String type) {
        return helperText("The type of this attribute is not yet supported: " + type, warning);
    }

    /** Returns an error helper text indicating that the required attribute must not be empty. */
    static HelperText required(ResourceAttribute ra) {
        String label = sentenceCase(ra.name);
        return helperText(label + " is a required attribute.", error);
    }

    /** Returns an error helper text indicating that the value is not a valid number for the given type. */
    static HelperText notNumeric(ModelType type) {
        return helperText("The value is not a number. Only values of type " + type.name() + " are allowed.", error);
    }

    /** Returns an error helper text indicating that the value is outside the allowed range. */
    static HelperText notInRange(String min, String max) {
        return helperText("The value is out of range. The value must be >= " + min + " and <= " + max + ".", error);
    }

    /** Returns an error helper text with a pattern hint indicating that the value is not a valid expression. */
    static HelperText noExpression() {
        return helperText()
                .addItem(helperTextItem("The value is not a valid expression.", error))
                .addItem(helperTextItem()
                        .add(flex().display(inlineFlex).spaceItems(sm)
                                .addItem(flexItem().add("Expressions must follow the pattern:"))
                                .addItem(flexItem()
                        // ExpressionBricks.renderExpression() won't work because of '<>' and '[]' in
                                        // [<prefix>][${<system-property-name>[:<default-value>]}][<suffix>]*
                                        .add(span().css(halComponent(expression))
                                                .add(span().css(halComponent(expression, defaultValue))
                                                        .text("[<prefix>]"))
                                                .add(span().css(halComponent(expression, dollar)))
                                                .add(span().css(halComponent(expression, curlyBraces, start)))
                                                .add(span().css(halComponent(expression, name))
                                                        .text("<system-property-name>"))
                                                .add(span().css(halComponent(expression, defaultValue))
                                                        .text("[:<default-value>]"))
                                                .add(span().css(halComponent(expression, curlyBraces, end)))
                                                .add(span().css(halComponent(expression, defaultValue))
                                                        .text("[<suffix>]*"))))));
    }
}
