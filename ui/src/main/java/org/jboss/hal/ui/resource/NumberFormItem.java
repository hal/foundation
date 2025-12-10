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
package org.jboss.hal.ui.resource;

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormGroupLabel;
import org.patternfly.component.form.FormSelect;
import org.patternfly.component.form.TextInput;
import org.patternfly.component.inputgroup.InputGroup;

import elemental2.dom.HTMLElement;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALLOWED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAX;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MIN;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.EXISTING_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemFlags.Scope.NEW_RESOURCE;
import static org.jboss.hal.ui.resource.FormItemInputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.FormItemInputMode.NATIVE;
import static org.jboss.hal.ui.resource.HelperTexts.notInRange;
import static org.jboss.hal.ui.resource.HelperTexts.notNumeric;
import static org.jboss.hal.ui.resource.HelperTexts.required;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.number;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;

// TODO Implement sensitive
//  Example: /subsystem=transactions, attribute "process-id-socket-max-ports"
class NumberFormItem extends FormItem {

    /**
     * @see <a
     * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MIN_SAFE_INTEGER">Number.MIN_SAFE_INTEGER</a>
     */
    private static final long MIN_SAFE_LONG = -9007199254740991L;

    /**
     * @see <a
     * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_SAFE_INTEGER">Number.MAX_SAFE_INTEGER</a>
     */
    private static final long MAX_SAFE_LONG = 9007199254740991L;

    /**
     * @see <a
     * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MIN_VALUE">Number.MIN_VALUE</a>
     */
    private static final double MIN_SAFE_DOUBLE = 5e-324;

    /**
     * @see <a
     * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/MAX_VALUE">Number.MAX_VALUE</a>
     */
    private static final double MAX_SAFE_DOUBLE = 1.7976931348623157e+308;

    static class NumericValidation {

        static NumericValidation valid(int intValue) {
            return new NumericValidation(true, intValue, 0, 0, ModelType.INT);
        }

        static NumericValidation valid(long longValue) {
            return new NumericValidation(true, 0, longValue, 0, ModelType.LONG);
        }

        static NumericValidation valid(double doubleValue) {
            return new NumericValidation(true, 0, 0, doubleValue, ModelType.DOUBLE);
        }

        static NumericValidation invalid() {
            return new NumericValidation(false, 0, 0, 0, ModelType.UNDEFINED);
        }

        boolean valid;
        int intValue;
        long longValue;
        double doubleValue;
        ModelType type;

        NumericValidation(boolean valid, int intValue, long longValue, double doubleValue, ModelType type) {
            this.valid = valid;
            this.intValue = intValue;
            this.longValue = longValue;
            this.doubleValue = doubleValue;
            this.type = type;
        }
    }

    static class RangeValidation {

        boolean valid;
        String min;
        String max;

        RangeValidation(boolean valid, String min, String max) {
            this.valid = valid;
            this.min = min;
            this.max = max;
        }
    }

    private FormSelect allowedValuesControl;
    private TextInput minMaxControl;

    NumberFormItem(String identifier, ResourceAttribute ra, FormGroupLabel label, FormItemFlags flags) {
        super(identifier, ra, label, flags);
        defaultSetup();
    }

    FormGroupControl readOnlyGroup() {
        TextInput textControl = readOnlyTextControl();
        if (ra.expression) {
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill().addControl(textControl))
                            .addItem(inputGroupItem().addButton(resolveExpressionButton()))
                            .run(ig -> {
                                if (ra.description.unit() != null) {
                                    ig.addText(unitInputGroupText());
                                }
                            }));
        } else {
            if (ra.description.unit() != null) {
                return formGroupControl()
                        .addInputGroup(inputGroup()
                                .addItem(inputGroupItem().fill().addControl(textControl))
                                .addText(unitInputGroupText()));
            } else {
                return formGroupControl().addControl(textControl);
            }
        }
    }

    FormGroupControl nativeGroup() {
        if (ra.description.hasDefined(ALLOWED)) {
            if (ra.description.unit() != null) {
                return formGroupControl()
                        .addInputGroup(inputGroup()
                                .addItem(inputGroupItem().fill().addControl(allowedValuesControl()))
                                .addText(unitInputGroupText()));
            } else {
                return formGroupControl().addControl(allowedValuesControl());
            }
        } else {
            if (ra.description.unit() != null) {
                return formGroupControl()
                        .addInputGroup(inputGroup()
                                .addItem(inputGroupItem().fill().addControl(minMaxControl()))
                                .addText(unitInputGroupText()));
            } else {
                return formGroupControl().addControl(minMaxControl());
            }
        }
    }

    HTMLElement nativeContainer() {
        if (nativeContainer == null) {
            InputGroup inputGroup = inputGroup().addItem(inputGroupItem().addButton(switchToExpressionModeButton()));
            if (ra.description.hasDefined(ALLOWED)) {
                inputGroup.addItem(inputGroupItem().fill().addControl(allowedValuesControl()));
            } else {
                inputGroup.addItem(inputGroupItem().fill().addControl(minMaxControl()));
            }
            if (ra.description.unit() != null) {
                inputGroup.addText(unitInputGroupText());
            }
            nativeContainer = inputGroup.element();
        }
        return nativeContainer;
    }

    private FormSelect allowedValuesControl() {
        List<Long> allowedValues = ra.description.get(ALLOWED).asList().stream().map(ModelNode::asLong).collect(toList());
        allowedValuesControl = formSelect(identifier)
                .run(fs -> {
                    if (ra.description.nillable()) {
                        fs.addOption(formSelectOption(UNDEFINED));
                    }
                })
                .addOptions(allowedValues, n -> formSelectOption(String.valueOf(n)))
                .run(fs -> {
                    if (ra.value.isDefined()) {
                        fs.value(ra.value.asString());
                    } else if (ra.description.hasDefault()) {
                        fs.value(ra.description.get(DEFAULT).asString());
                    } else if (ra.description.nillable()) {
                        fs.value(UNDEFINED);
                    }
                });
        return allowedValuesControl;
    }

    private TextInput minMaxControl() {
        minMaxControl = textInput(number, identifier)
                .run(ti -> {
                    if (ra.value.isDefined()) {
                        ti.value(ra.value.asString());
                    }
                    applyPlaceholder(ti.input());
                });
        ModelType type = ra.description.get(TYPE).asType();
        if (type == ModelType.INT) {
            int min = max(ra.description.get(MIN).asInt(Integer.MIN_VALUE), Integer.MIN_VALUE);
            int max = min(ra.description.get(MAX).asInt(Integer.MAX_VALUE), Integer.MAX_VALUE);
            minMaxControl.input().min(min).max(max).apply(e -> e.step = "1");
        } else if (type == ModelType.LONG) {
            String min = String.valueOf(max(ra.description.get(MIN).asLong(MIN_SAFE_LONG), MIN_SAFE_LONG));
            String max = String.valueOf(min(ra.description.get(MAX).asLong(MAX_SAFE_LONG), MAX_SAFE_LONG));
            minMaxControl.input().min(min).max(max).apply(e -> e.step = "1");
        } else if (type == ModelType.DOUBLE) {
            minMaxControl.input().apply(e -> e.step = "any");
        }
        return minMaxControl;
    }

    // ------------------------------------------------------ validation

    @Override
    void resetValidation() {
        super.resetValidation();
        if (allowedValuesControl != null) {
            allowedValuesControl.resetValidation();
        }
        if (minMaxControl != null) {
            minMaxControl.resetValidation();
        }
    }

    @Override
    boolean validate() {
        if (inputMode == NATIVE) {
            if (allowedValuesControl != null) {
                if (requiredOnItsOwn() && UNDEFINED.equals(allowedValuesControl.value())) {
                    allowedValuesControl.validated(error);
                    formGroupControl.addHelperText(required(ra));
                    return false;
                }
            } else if (minMaxControl != null) {
                String value = minMaxControl.value();
                if (requiredOnItsOwn() && value.isEmpty()) {
                    minMaxControl.validated(error);
                    formGroupControl.addHelperText(required(ra));
                    return false;
                } else if (!value.isEmpty()) {
                    NumericValidation numericValidation = isNumeric(value);
                    if (!numericValidation.valid) {
                        minMaxControl.validated(error);
                        formGroupControl.addHelperText(notNumeric(numericValidation.type));
                        return false;
                    }
                    RangeValidation rangeValidation = inRange(numericValidation);
                    if (!rangeValidation.valid) {
                        minMaxControl.validated(error);
                        formGroupControl.addHelperText(notInRange(rangeValidation.min, rangeValidation.max));
                        return false;
                    }
                }
            }
        } else if (inputMode == EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    private NumericValidation isNumeric(String value) {
        ModelType type = ra.description.get(TYPE).asType();
        if (type == ModelType.INT) {
            try {
                int intValue = Integer.parseInt(value);
                return NumericValidation.valid(intValue);
            } catch (NumberFormatException ignored) {
                return NumericValidation.invalid();
            }
        } else if (type == ModelType.LONG) {
            try {
                long longValue = Long.parseLong(value);
                return NumericValidation.valid(longValue);
            } catch (NumberFormatException ignored) {
                return NumericValidation.invalid();
            }
        } else if (type == ModelType.DOUBLE) {
            try {
                double doubleValue = Double.parseDouble(value);
                return NumericValidation.valid(doubleValue);
            } catch (NumberFormatException ignored) {
                return NumericValidation.invalid();
            }
        } else {
            return NumericValidation.invalid();
        }
    }

    private RangeValidation inRange(NumericValidation numericValidation) {
        ModelType type = numericValidation.type;
        if (type == ModelType.INT) {
            int min = ra.description.hasDefined(MIN) ? ra.description.get(MIN).asInt() : Integer.MIN_VALUE;
            int max = ra.description.hasDefined(MAX) ? ra.description.get(MAX).asInt() : Integer.MAX_VALUE;
            int intValue = numericValidation.intValue;
            return new RangeValidation(min <= intValue && intValue <= max, String.valueOf(min), String.valueOf(max));
        } else if (type == ModelType.LONG) {
            long min = ra.description.hasDefined(MIN) ? ra.description.get(MIN).asLong() : MIN_SAFE_LONG;
            long max = ra.description.hasDefined(MAX) ? ra.description.get(MAX).asLong() : MAX_SAFE_LONG;
            long longValue = numericValidation.longValue;
            return new RangeValidation(min <= longValue && longValue <= max, String.valueOf(min), String.valueOf(max));
        } else if (type == ModelType.DOUBLE) {
            double min = ra.description.hasDefined(MIN) ? ra.description.get(MIN).asDouble() : MIN_SAFE_DOUBLE;
            double max = ra.description.hasDefined(MAX) ? ra.description.get(MAX).asDouble() : MAX_SAFE_DOUBLE;
            double doubleValue = numericValidation.doubleValue;
            return new RangeValidation(min <= doubleValue && doubleValue <= max, String.valueOf(min), String.valueOf(max));
        } else {
            return new RangeValidation(false, "", "");
        }
    }

    // ------------------------------------------------------ data

    @Override
    boolean isModified() {
        if (ra.readable && !ra.description.readOnly()) {
            if (flags.scope == NEW_RESOURCE) {
                if (inputMode == NATIVE) {
                    if (allowedValuesControl != null) {
                        String selectedValue = allowedValuesControl.value();
                        if (ra.description.hasDefault()) {
                            return !ra.description.get(DEFAULT).asString().equals(selectedValue);
                        } else {
                            return !UNDEFINED.equals(selectedValue);
                        }
                    } else if (minMaxControl != null) {
                        if (ra.description.hasDefault()) {
                            return !ra.description.get(DEFAULT).asString().equals(minMaxControl.value());
                        } else {
                            return !minMaxControl.value().isEmpty();
                        }
                    }
                } else if (inputMode == EXPRESSION) {
                    return isExpressionModified();
                }
            } else if (flags.scope == EXISTING_RESOURCE) {
                boolean wasDefined = ra.value.isDefined();
                if (inputMode == NATIVE) {
                    if (allowedValuesControl != null) {
                        String selectedValue = allowedValuesControl.value();
                        if (wasDefined) {
                            // modified if the original value was an expression or is different from the current user input
                            String originalValue = ra.value.asString();
                            return ra.expression || !originalValue.equals(selectedValue);
                        } else {
                            return !UNDEFINED.equals(selectedValue);
                        }
                    } else if (minMaxControl != null) {
                        if (wasDefined) {
                            // modified if the original value was an expression or is different from the current user input
                            String originalValue = ra.value.asString();
                            return ra.expression || !originalValue.equals(minMaxControl.value());
                        } else {
                            return !minMaxControl.value().isEmpty();
                        }
                    }
                } else if (inputMode == EXPRESSION) {
                    if (wasDefined) {
                        String originalValue = ra.value.asString();
                        return !originalValue.equals(textControlValue());
                    } else {
                        return !textControlValue().isEmpty();
                    }
                }
            } else {
                unknownScope();
            }
        }
        return false;
    }

    @Override
    ModelNode modelNode() {
        if (inputMode == NATIVE) {
            if (allowedValuesControl != null) {
                String selectedValue = allowedValuesControl.value();
                if (UNDEFINED.equals(selectedValue)) {
                    return new ModelNode();
                } else {
                    return new ModelNode().set(numericModelNode(selectedValue));
                }
            } else if (minMaxControl != null) {
                String value = minMaxControl.value();
                if (value.isEmpty()) {
                    return new ModelNode();
                } else {
                    return numericModelNode(value);
                }
            }
        } else if (inputMode == EXPRESSION) {
            return expressionModelNode();
        }
        return new ModelNode();
    }

    private ModelNode numericModelNode(String value) {
        ModelType type = ra.description.get(TYPE).asType();
        if (type == ModelType.INT) {
            return new ModelNode().set(Integer.parseInt(value));
        } else if (type == ModelType.LONG) {
            return new ModelNode().set(Long.parseLong(value));
        } else if (type == ModelType.DOUBLE) {
            return new ModelNode().set(Double.parseDouble(value));
        } else {
            return new ModelNode();
        }
    }

    // ------------------------------------------------------ events

    @Override
    @SuppressWarnings("DuplicatedCode")
    void afterSwitchedToNativeMode() {
        boolean wasDefined = ra.value.isDefined();
        if (allowedValuesControl != null) {
            if (wasDefined && !ra.expression) {
                String originalValue = ra.value.asString();
                failSafeSelectValue(originalValue);
            } else {
                if (ra.description.hasDefault()) {
                    failSafeSelectValue(ra.description.get(DEFAULT).asString());
                } else if (ra.description.nillable()) {
                    failSafeSelectValue(UNDEFINED);
                } else {
                    allowedValuesControl.selectFirstValue(false);
                }
            }
        } else if (minMaxControl != null) {
            if (wasDefined && !ra.expression) {
                String originalValue = ra.value.asString();
                minMaxControl.value(originalValue);
            }
        }
    }

    private void failSafeSelectValue(String value) {
        if (allowedValuesControl.containsValue(value)) {
            allowedValuesControl.value(value, false);
        } else {
            allowedValuesControl.selectFirstValue(false);
        }
    }
}
