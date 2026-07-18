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

import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.form.FormSelect;
import org.patternfly.component.form.TextInput;

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
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.form.FormSelect.formSelect;
import static org.patternfly.component.form.FormSelectOption.formSelectOption;
import static org.patternfly.component.form.TextInput.textInput;
import static org.patternfly.component.form.TextInputType.number;
import org.patternfly.component.help.HelperText;

/**
 * {@link NativeControl} for numeric attributes (INT, LONG, DOUBLE). Uses either a {@link FormSelect} for allowed-values
 * or a {@link TextInput} with min/max validation.
 * <p>
 * Wraps both variants as a single HTMLElement, since the outer container differs only in input type.
 */
public final class NumberInputControl implements NativeControl<HTMLElement> {

    private static final long MIN_SAFE_LONG = -9007199254740991L;
    private static final long MAX_SAFE_LONG = 9007199254740991L;

    private FormSelect allowedValuesControl;
    private TextInput minMaxControl;

    @Override
    public HTMLElement create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        AttributeDescription desc = attribute.description();
        if (desc.hasDefined(ALLOWED)) {
            return createAllowedValues(identifier, attribute).element();
        } else {
            return createMinMax(identifier, attribute, context).element();
        }
    }

    @Override
    public HTMLElement element(HTMLElement control) {
        return control;
    }

    @Override
    public ModelNode modelNode(HTMLElement control, ResolvedAttribute attribute) {
        if (allowedValuesControl != null) {
            String value = allowedValuesControl.value();
            if (UNDEFINED.equals(value)) {
                return new ModelNode();
            }
            return numericModelNode(value, attribute);
        } else if (minMaxControl != null) {
            String value = minMaxControl.value();
            if (value.isEmpty()) {
                return new ModelNode();
            }
            return numericModelNode(value, attribute);
        }
        return new ModelNode();
    }

    @Override
    public boolean isModifiedForNew(HTMLElement control, ResolvedAttribute attribute) {
        if (allowedValuesControl != null) {
            String value = allowedValuesControl.value();
            if (attribute.description().hasDefault()) {
                return !attribute.description().get(DEFAULT).asString().equals(value);
            }
            return !UNDEFINED.equals(value);
        } else if (minMaxControl != null) {
            if (attribute.description().hasDefault()) {
                return !attribute.description().get(DEFAULT).asString().equals(minMaxControl.value());
            }
            return !minMaxControl.value().isEmpty();
        }
        return false;
    }

    @Override
    public boolean isModifiedForExisting(HTMLElement control, ResolvedAttribute attribute, boolean wasDefined) {
        if (allowedValuesControl != null) {
            String value = allowedValuesControl.value();
            if (wasDefined) {
                return attribute.expression() || !attribute.value().asString().equals(value);
            }
            return !UNDEFINED.equals(value);
        } else if (minMaxControl != null) {
            if (wasDefined) {
                return attribute.expression() || !attribute.value().asString().equals(minMaxControl.value());
            }
            return !minMaxControl.value().isEmpty();
        }
        return false;
    }

    @Override
    public boolean validate(HTMLElement control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        if (allowedValuesControl != null) {
            if (FormItemBricks.requiredOnItsOwn(attribute) && UNDEFINED.equals(allowedValuesControl.value())) {
                allowedValuesControl.validated(error);
                formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
                return false;
            }
        } else if (minMaxControl != null) {
            String value = minMaxControl.value();
            if (FormItemBricks.requiredOnItsOwn(attribute) && value.isEmpty()) {
                minMaxControl.validated(error);
                formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
                return false;
            } else if (!value.isEmpty()) {
                ModelType type = attribute.description().get(TYPE).asType();
                if (!isNumeric(value, type)) {
                    minMaxControl.validated(error);
                    formGroupControl.addHelperText(HelperText.helperText(
                            "The value is not a number. Only values of type " + type.name() + " are allowed.", error));
                    return false;
                }
                String rangeError = checkRange(value, type, attribute.description());
                if (rangeError != null) {
                    minMaxControl.validated(error);
                    formGroupControl.addHelperText(HelperText.helperText(rangeError, error));
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void resetValidation(HTMLElement control) {
        if (allowedValuesControl != null) {
            allowedValuesControl.resetValidation();
        }
        if (minMaxControl != null) {
            minMaxControl.resetValidation();
        }
    }

    @Override
    public void afterSwitchedToNativeMode(HTMLElement control, ResolvedAttribute attribute) {
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

    // ------------------------------------------------------ internal

    private FormSelect createAllowedValues(String identifier, ResolvedAttribute attribute) {
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

    private TextInput createMinMax(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        minMaxControl = textInput(number, identifier)
                .run(ti -> {
                    ti.input().autocomplete("off");
                    if (attribute.value().isDefined()) {
                        ti.value(attribute.value().asString());
                    }
                    FormItemBricks.applyPlaceholder(ti.input(), attribute, context.flags());
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

    private String checkRange(String value, ModelType type, AttributeDescription desc) {
        if (type == ModelType.INT) {
            int mn = desc.hasDefined(MIN) ? desc.get(MIN).asInt() : Integer.MIN_VALUE;
            int mx = desc.hasDefined(MAX) ? desc.get(MAX).asInt() : Integer.MAX_VALUE;
            int v = Integer.parseInt(value);
            if (v < mn || v > mx) {
                return "The value is out of range. The value must be >= " + mn + " and <= " + mx + ".";
            }
        } else if (type == ModelType.LONG) {
            long mn = desc.hasDefined(MIN) ? desc.get(MIN).asLong() : MIN_SAFE_LONG;
            long mx = desc.hasDefined(MAX) ? desc.get(MAX).asLong() : MAX_SAFE_LONG;
            long v = Long.parseLong(value);
            if (v < mn || v > mx) {
                return "The value is out of range. The value must be >= " + mn + " and <= " + mx + ".";
            }
        } else if (type == ModelType.DOUBLE) {
            double mn = desc.hasDefined(MIN) ? desc.get(MIN).asDouble() : 5e-324;
            double mx = desc.hasDefined(MAX) ? desc.get(MAX).asDouble() : 1.7976931348623157e+308;
            double v = Double.parseDouble(value);
            if (v < mn || v > mx) {
                return "The value is out of range. The value must be >= " + mn + " and <= " + mx + ".";
            }
        }
        return null;
    }

    private ModelNode numericModelNode(String value, ResolvedAttribute attribute) {
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

    private void failSafeSelectValue(String value) {
        if (allowedValuesControl.containsValue(value)) {
            allowedValuesControl.value(value, false);
        } else {
            allowedValuesControl.selectFirstValue(false);
        }
    }
}
