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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.menu.SingleTypeahead;
import org.patternfly.style.Modifiers.FullWidth;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.UNDEFINED;
import static org.jboss.hal.ui.resource.form.CapabilityReferenceSupport.capabilityItems;
import static org.jboss.hal.ui.resource.form.CapabilityReferenceSupport.newItem;
import static org.jboss.hal.ui.resource.form.SearchReloadInput.searchReloadInput;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.menu.MenuContent.menuContent;
import static org.patternfly.component.menu.MenuList.menuList;
import static org.patternfly.component.menu.SingleSelectMenu.singleSelectMenu;
import static org.patternfly.component.menu.SingleTypeahead.singleTypeahead;

/**
 * {@link NativeControl} for single STRING attributes with a capability reference, rendered as a typeahead select.
 */
public final class CapabilityReferenceControl implements NativeControl<SingleTypeahead> {

    private String capability;

    @Override
    public SingleTypeahead create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        capability = attribute.description().get(CAPABILITY_REFERENCE).asString();
        SearchReloadInput searchReloadInput = searchReloadInput(identifier)
                .plain()
                .placeholder("");
        SingleTypeahead typeahead = singleTypeahead(searchReloadInput)
                .applyToMenuToggle(FullWidth::fullWidth)
                .allowNewItems(value -> "Add \"" + value + "\"...", value -> newItem(value, capability))
                .addMenu(singleSelectMenu()
                        .addContent(menuContent()
                                .addList(menuList()
                                        .addItems(capabilityItems(context.template(), capability)))));
        searchReloadInput.onReload((e, c) -> typeahead.menu().reload());

        if (attribute.value().isDefined()) {
            failSafeSelectValue(typeahead, attribute.value().asString());
        } else if (attribute.description().hasDefault()) {
            typeahead.menuToggle().searchInput().placeholder(attribute.description().get(DEFAULT).asString());
        } else if (attribute.description().nillable()) {
            typeahead.menuToggle().searchInput().placeholder(UNDEFINED);
        }
        return typeahead;
    }

    @Override
    public HTMLElement element(SingleTypeahead control) {
        return control.element();
    }

    @Override
    public ModelNode modelNode(SingleTypeahead control, ResolvedAttribute attribute) {
        String v = value(control);
        if (v == null || v.isEmpty()) {
            return new ModelNode();
        }
        return new ModelNode().set(v);
    }

    @Override
    public boolean isModifiedForNew(SingleTypeahead control, ResolvedAttribute attribute) {
        String v = value(control);
        if (attribute.description().hasDefault()) {
            return !attribute.description().get(DEFAULT).asString().equals(v);
        }
        return v != null && !v.isEmpty();
    }

    @Override
    public boolean isModifiedForExisting(SingleTypeahead control, ResolvedAttribute attribute, boolean wasDefined) {
        String v = value(control);
        if (wasDefined) {
            return attribute.expression() || !attribute.value().asString().equals(v);
        }
        return v != null && !v.isEmpty();
    }

    @Override
    public boolean validate(SingleTypeahead control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        if (FormItemBricks.requiredOnItsOwn(attribute) && value(control).isEmpty()) {
            control.menuToggle().validated(error);
            formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
            return false;
        }
        return true;
    }

    @Override
    public void resetValidation(SingleTypeahead control) {
        control.menuToggle().resetValidation();
    }

    @Override
    public void afterSwitchedToNativeMode(SingleTypeahead control, ResolvedAttribute attribute) {
        if (attribute.value().isDefined() && !attribute.expression()) {
            failSafeSelectValue(control, attribute.value().asString());
        } else {
            if (attribute.description().hasDefault()) {
                failSafeSelectValue(control, attribute.description().get(DEFAULT).asString());
            } else if (attribute.description().nillable()) {
                control.menuToggle().searchInput().placeholder(UNDEFINED);
            }
        }
    }

    private void failSafeSelectValue(SingleTypeahead typeahead, String value) {
        if (typeahead.menu().hasAsyncItems()) {
            typeahead.menuToggle().text(value);
            typeahead.onLoaded((__, st) -> st.select(value));
        } else {
            typeahead.select(value);
        }
    }

    private String value(SingleTypeahead control) {
        return control != null ? control.menuToggle().searchInput().value() : "";
    }
}
