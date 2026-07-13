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

import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.pipeline.ResolvedAttribute;

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.patternfly.component.form.FormGroupControl;
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
import static org.jboss.hal.ui.resource.form.InputMode.EXPRESSION;
import static org.jboss.hal.ui.resource.form.InputMode.NATIVE;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormGroupControl.formGroupControl;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.number;
import static org.patternfly.component.help.HelperText.helperText;
import static org.patternfly.component.inputgroup.InputGroup.inputGroup;
import static org.patternfly.component.inputgroup.InputGroupItem.inputGroupItem;

/** Form item for editing numeric attributes (INT, LONG, DOUBLE), with min/max validation or allowed-values select. */
public class NumberFormItem extends AbstractFormItem {

    private static final long MIN_SAFE_LONG = -9007199254740991L;
    private static final long MAX_SAFE_LONG = 9007199254740991L;

    private FormSelect allowedValuesControl;
    private TextInput minMaxControl;

    public NumberFormItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
        defaultSetup();
    }

    @Override
    FormGroupControl readOnlyGroup() {
        TextInput tc = readOnlyTextControl();
        if (attribute.expression()) {
            return formGroupControl()
                    .addInputGroup(inputGroup()
                            .addItem(inputGroupItem().fill().addControl(tc))
                            .addItem(inputGroupItem().addButton(resolveExpressionButton()))
                            .run(ig -> {
                                if (attribute.description().unit() != null) {
                                    ig.addText(unitInputGroupText());
                                }
                            }));
        } else {
            if (attribute.description().unit() != null) {
                return formGroupControl()
                        .addInputGroup(inputGroup()
                                .addItem(inputGroupItem().fill().addControl(tc))
                                .addText(unitInputGroupText()));
            } else {
                return formGroupControl().addControl(tc);
            }
        }
    }

    @Override
    FormGroupControl nativeGroup() {
        if (attribute.description().hasDefined(ALLOWED)) {
            if (attribute.description().unit() != null) {
                return formGroupControl()
                        .addInputGroup(inputGroup()
                                .addItem(inputGroupItem().fill().addControl(allowedValuesControl()))
                                .addText(unitInputGroupText()));
            } else {
                return formGroupControl().addControl(allowedValuesControl());
            }
        } else {
            if (attribute.description().unit() != null) {
                return formGroupControl()
                        .addInputGroup(inputGroup()
                                .addItem(inputGroupItem().fill().addControl(minMaxControl()))
                                .addText(unitInputGroupText()));
            } else {
                return formGroupControl().addControl(minMaxControl());
            }
        }
    }

    @Override
    HTMLElement nativeContainer() {
        if (nativeContainer == null) {
            InputGroup ig = inputGroup().addItem(inputGroupItem().addButton(switchToExpressionModeButton()));
            if (attribute.description().hasDefined(ALLOWED)) {
                ig.addItem(inputGroupItem().fill().addControl(allowedValuesControl()));
            } else {
                ig.addItem(inputGroupItem().fill().addControl(minMaxControl()));
            }
            if (attribute.description().unit() != null) {
                ig.addText(unitInputGroupText());
            }
            nativeContainer = ig.element();
        }
        return nativeContainer;
    }

    private FormSelect allowedValuesControl() {
        List<Long> allowedValues = attribute.description().get(ALLOWED).asList().stream()
                .map(ModelNode::asLong).collect(toList());
        allowedValuesControl = formSelect(identifier)
                .run(fs -> {
                    fs.selectElement().attr("autocomplete", "off");
                    if (attribute.description().nillable()) {
                        fs.addOption(formSelectOption(UNDEFINED));
                    }
                })
                .addOptions(allowedValues, n -> formSelectOption(String.valueOf(n)))
                .run(fs -> {
                    if (attribute.value().isDefined()) {
                        fs.value(attribute.value().asString());
                    } else if (attribute.description().hasDefault()) {
                        fs.value(attribute.description().get(DEFAULT).asString());
                    } else if (attribute.description().nillable()) {
                        fs.value(UNDEFINED);
                    }
                });
        return allowedValuesControl;
    }

    private TextInput minMaxControl() {
        minMaxControl = textInput(number, identifier)
                .run(ti -> {
                    ti.input().autocomplete("off");
                    if (attribute.value().isDefined()) {
                        ti.value(attribute.value().asString());
                    }
                    applyPlaceholder(ti.input());
                });
        ModelType type = attribute.description().get(TYPE).asType();
        if (type == ModelType.INT) {
            int mn = max(attribute.description().get(MIN).asInt(Integer.MIN_VALUE), Integer.MIN_VALUE);
            int mx = min(attribute.description().get(MAX).asInt(Integer.MAX_VALUE), Integer.MAX_VALUE);
            minMaxControl.input().min(mn).max(mx).apply(e -> e.step = "1");
        } else if (type == ModelType.LONG) {
            String mn = String.valueOf(max(attribute.description().get(MIN).asLong(MIN_SAFE_LONG), MIN_SAFE_LONG));
            String mx = String.valueOf(min(attribute.description().get(MAX).asLong(MAX_SAFE_LONG), MAX_SAFE_LONG));
            minMaxControl.input().min(mn).max(mx).apply(e -> e.step = "1");
        } else if (type == ModelType.DOUBLE) {
            minMaxControl.input().apply(e -> e.step = "any");
        }
        return minMaxControl;
    }

    // ------------------------------------------------------ validation

    @Override
    public void resetValidation() {
        super.resetValidation();
        if (allowedValuesControl != null) {
            allowedValuesControl.resetValidation();
        }
        if (minMaxControl != null) {
            minMaxControl.resetValidation();
        }
    }

    @Override
    public boolean validate() {
        if (inputMode == NATIVE) {
            if (allowedValuesControl != null) {
                if (requiredOnItsOwn() && UNDEFINED.equals(allowedValuesControl.value())) {
                    allowedValuesControl.validated(error);
                    formGroupControl.addHelperText(requiredHelperText());
                    return false;
                }
            } else if (minMaxControl != null) {
                String value = minMaxControl.value();
                if (requiredOnItsOwn() && value.isEmpty()) {
                    minMaxControl.validated(error);
                    formGroupControl.addHelperText(requiredHelperText());
                    return false;
                } else if (!value.isEmpty()) {
                    ModelType type = attribute.description().get(TYPE).asType();
                    if (!isNumeric(value, type)) {
                        minMaxControl.validated(error);
                        formGroupControl.addHelperText(helperText(
                                "The value is not a number. Only values of type " + type.name() + " are allowed.", error));
                        return false;
                    }
                    String rangeError = checkRange(value, type);
                    if (rangeError != null) {
                        minMaxControl.validated(error);
                        formGroupControl.addHelperText(helperText(rangeError, error));
                        return false;
                    }
                }
            }
        } else if (inputMode == EXPRESSION) {
            return validateExpressionMode();
        }
        return true;
    }

    private boolean isNumeric(String value, ModelType type) {
        try {
            if (type == ModelType.INT) {
                Integer.parseInt(value);
            } else if (type == ModelType.LONG) {
                Long.parseLong(value);
            } else if (type == ModelType.DOUBLE) {
                Double.parseDouble(value);
            } else {
                return false;
            }
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private String checkRange(String value, ModelType type) {
        if (type == ModelType.INT) {
            int mn = attribute.description().hasDefined(MIN) ? attribute.description().get(MIN).asInt() : Integer.MIN_VALUE;
            int mx = attribute.description().hasDefined(MAX) ? attribute.description().get(MAX).asInt() : Integer.MAX_VALUE;
            int v = Integer.parseInt(value);
            if (v < mn || v > mx) {
                return "The value is out of range. The value must be >= " + mn + " and <= " + mx + ".";
            }
        } else if (type == ModelType.LONG) {
            long mn = attribute.description().hasDefined(MIN) ? attribute.description().get(MIN).asLong() : MIN_SAFE_LONG;
            long mx = attribute.description().hasDefined(MAX) ? attribute.description().get(MAX).asLong() : MAX_SAFE_LONG;
            long v = Long.parseLong(value);
            if (v < mn || v > mx) {
                return "The value is out of range. The value must be >= " + mn + " and <= " + mx + ".";
            }
        } else if (type == ModelType.DOUBLE) {
            double mn = attribute.description().hasDefined(MIN) ? attribute.description().get(MIN).asDouble() : 5e-324;
            double mx = attribute.description().hasDefined(MAX) ? attribute.description().get(MAX).asDouble() : 1.7976931348623157e+308;
            double v = Double.parseDouble(value);
            if (v < mn || v > mx) {
                return "The value is out of range. The value must be >= " + mn + " and <= " + mx + ".";
            }
        }
        return null;
    }

    // ------------------------------------------------------ data

    @Override
    boolean isNativeModifiedForNew() {
        if (allowedValuesControl != null) {
            String selectedValue = allowedValuesControl.value();
            if (attribute.description().hasDefault()) {
                return !attribute.description().get(DEFAULT).asString().equals(selectedValue);
            } else {
                return !UNDEFINED.equals(selectedValue);
            }
        } else if (minMaxControl != null) {
            if (attribute.description().hasDefault()) {
                return !attribute.description().get(DEFAULT).asString().equals(minMaxControl.value());
            } else {
                return !minMaxControl.value().isEmpty();
            }
        }
        return false;
    }

    @Override
    boolean isNativeModifiedForExisting(boolean wasDefined) {
        if (allowedValuesControl != null) {
            String selectedValue = allowedValuesControl.value();
            if (wasDefined) {
                return attribute.expression() || !attribute.value().asString().equals(selectedValue);
            } else {
                return !UNDEFINED.equals(selectedValue);
            }
        } else if (minMaxControl != null) {
            if (wasDefined) {
                return attribute.expression() || !attribute.value().asString().equals(minMaxControl.value());
            } else {
                return !minMaxControl.value().isEmpty();
            }
        }
        return false;
    }

    @Override
    public ModelNode modelNode() {
        if (inputMode == NATIVE) {
            if (allowedValuesControl != null) {
                String selectedValue = allowedValuesControl.value();
                if (UNDEFINED.equals(selectedValue)) {
                    return new ModelNode();
                } else {
                    return numericModelNode(selectedValue);
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
        ModelType type = attribute.description().get(TYPE).asType();
        if (type == ModelType.INT) {
            return new ModelNode().set(Integer.parseInt(value));
        } else if (type == ModelType.LONG) {
            return new ModelNode().set(Long.parseLong(value));
        } else if (type == ModelType.DOUBLE) {
            return new ModelNode().set(Double.parseDouble(value));
        }
        return new ModelNode();
    }

    // ------------------------------------------------------ events

    @Override
    void afterSwitchedToNativeMode() {
        boolean wasDefined = attribute.value().isDefined();
        if (allowedValuesControl != null) {
            if (wasDefined && !attribute.expression()) {
                failSafeSelectValue(attribute.value().asString());
            } else {
                if (attribute.description().hasDefault()) {
                    failSafeSelectValue(attribute.description().get(DEFAULT).asString());
                } else if (attribute.description().nillable()) {
                    failSafeSelectValue(UNDEFINED);
                } else {
                    allowedValuesControl.selectFirstValue(false);
                }
            }
        } else if (minMaxControl != null) {
            if (wasDefined && !attribute.expression()) {
                minMaxControl.value(attribute.value().asString());
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
