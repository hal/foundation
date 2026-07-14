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
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MultiTypeahead;
import org.patternfly.style.Modifiers.FullWidth;

import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.form.CapabilityReferenceSupport.capabilityItems;
import static org.jboss.hal.ui.resource.form.CapabilityReferenceSupport.newItem;
import static org.jboss.hal.ui.resource.form.FilterReloadInput.filterReloadInput;
import static org.jboss.hal.ui.resource.form.StringListSupport.defaultValues;
import static org.jboss.hal.ui.resource.form.StringListSupport.isExistingModified;
import static org.jboss.hal.ui.resource.form.StringListSupport.isNewModified;
import static org.jboss.hal.ui.resource.form.StringListSupport.modelValues;
import static org.jboss.hal.ui.resource.form.StringListSupport.valuesModelNode;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.MultiSelectMenu.multiSelectMenu;
import static org.patternfly.component.menu.MultiTypeahead.multiTypeahead;

/**
 * {@link NativeControl} for LIST-of-STRING attributes with a capability reference, rendered as a multi-select typeahead.
 */
public final class MultiTypeaheadControl implements NativeControl<MultiTypeahead> {

    private String capability;

    @Override
    public MultiTypeahead create(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        capability = attribute.description().get(CAPABILITY_REFERENCE).asString();
        FilterReloadInput fri = filterReloadInput(identifier)
                .plain()
                .placeholder("");
        MultiTypeahead typeahead = multiTypeahead(fri)
                .applyToMenuToggle(FullWidth::fullWidth)
                .allowNewItems(value -> "Add \"" + value + "\"...", value -> newItem(value, capability))
                .addMenu(multiSelectMenu()
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItems(capabilityItems(context.template(), capability)))));
        fri.onReload((e, c) -> typeahead.menu().reload());

        if (attribute.value().isDefined()) {
            setValues(typeahead, modelValues(attribute));
        } else if (attribute.description().hasDefault()) {
            typeahead.menuToggle().filterInput().placeholder(attribute.description().get(DEFAULT).asString());
        } else if (attribute.description().nillable()) {
            typeahead.menuToggle().filterInput().placeholder(UNDEFINED);
        }
        return typeahead;
    }

    @Override
    public HTMLElement element(MultiTypeahead control) {
        return control.element();
    }

    @Override
    public ModelNode modelNode(MultiTypeahead control, ResolvedAttribute attribute) {
        return valuesModelNode(getValues(control));
    }

    @Override
    public boolean isModifiedForNew(MultiTypeahead control, ResolvedAttribute attribute) {
        return isNewModified(attribute, getValues(control));
    }

    @Override
    public boolean isModifiedForExisting(MultiTypeahead control, ResolvedAttribute attribute, boolean wasDefined) {
        return isExistingModified(attribute, getValues(control), wasDefined);
    }

    @Override
    public boolean validate(MultiTypeahead control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        if (FormItemBricks.requiredOnItsOwn(attribute) && getValues(control).isEmpty()) {
            control.menuToggle().validated(error);
            formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
            return false;
        }
        return true;
    }

    @Override
    public void resetValidation(MultiTypeahead control) {
        control.menuToggle().resetValidation();
    }

    @Override
    public void afterSwitchedToNativeMode(MultiTypeahead control, ResolvedAttribute attribute) {
        if (attribute.value().isDefined() && !attribute.expression()) {
            setValues(control, modelValues(attribute));
        } else {
            if (attribute.description().hasDefault()) {
                List<String> dv = defaultValues(attribute);
                control.menuToggle().searchInput().placeholder(String.join(" ", dv));
                setValues(control, dv);
            } else if (attribute.description().nillable()) {
                control.menuToggle().searchInput().placeholder(UNDEFINED);
            }
        }
    }

    private void setValues(MultiTypeahead typeahead, List<String> values) {
        if (typeahead.menu().hasAsyncItems()) {
            typeahead.menu().load().then(__ -> {
                typeahead.selectIdentifiers(values);
                return null;
            });
            typeahead.onLoaded((__, mt) -> mt.selectIdentifiers(values));
        } else {
            typeahead.selectIdentifiers(values);
        }
    }

    private List<String> getValues(MultiTypeahead control) {
        return control.menu().items().stream().map(MenuItem::text).collect(toList());
    }
}
