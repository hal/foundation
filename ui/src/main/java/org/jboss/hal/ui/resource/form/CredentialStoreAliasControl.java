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
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.ui.resource.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;
import org.patternfly.async.AsyncItems;
import org.patternfly.component.form.FormGroupControl;
import org.patternfly.component.menu.MenuItem;
import org.patternfly.component.menu.MenuList;
import org.patternfly.component.menu.SingleSelectTypeahead;

import elemental2.dom.HTMLElement;

import static elemental2.promise.Promise.resolve;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DEFAULT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_ALIASES_OPERATION;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.ValidationStatus.error;
import static org.patternfly.component.menu.MenuItem.menuItem;

/**
 * {@link NativeControl} used only as part of the {@link CredentialReferenceControl} to lookup the aliases of a credential
 * store.
 */
final class CredentialStoreAliasControl implements NativeControl<SingleSelectTypeahead> {

    private static final String CAPABILITY = "org.wildfly.security.credential-store";

    private String credentialStore;
    private SingleSelectTypeahead aliasTypeahead;

    @Override
    public SingleSelectTypeahead create(PipelineContext context, String identifier, ResolvedAttribute attribute) {
        aliasTypeahead = FormItemBricks.singleTypeahead(identifier, attribute, null, aliases());
        return aliasTypeahead;
    }

    @Override
    public HTMLElement element(SingleSelectTypeahead control) {
        return control.element();
    }

    @Override
    public ModelNode modelNode(SingleSelectTypeahead control, ResolvedAttribute attribute) {
        String v = value(control);
        if (v == null || v.isEmpty()) {
            return new ModelNode();
        }
        return new ModelNode().set(v);
    }

    @Override
    public boolean isModifiedForNew(SingleSelectTypeahead control, ResolvedAttribute attribute) {
        String v = value(control);
        if (attribute.description().hasDefault()) {
            return !attribute.description().get(DEFAULT).asString().equals(v);
        }
        return v != null && !v.isEmpty();
    }

    @Override
    public boolean isModifiedForExisting(SingleSelectTypeahead control, ResolvedAttribute attribute, boolean wasDefined) {
        String v = value(control);
        if (wasDefined) {
            return attribute.expression() || !attribute.value().asString().equals(v);
        }
        return v != null && !v.isEmpty();
    }

    @Override
    public boolean validate(SingleSelectTypeahead control, ResolvedAttribute attribute, FormGroupControl formGroupControl) {
        if (FormItemBricks.requiredOnItsOwn(attribute) && value(control).isEmpty()) {
            markInvalid(control);
            formGroupControl.addHelperText(FormItemBricks.requiredHelperText(attribute));
            return false;
        }
        return true;
    }

    @Override
    public void markInvalid(SingleSelectTypeahead control) {
        control.menuToggle().validated(error);
    }

    @Override
    public void resetValidation(SingleSelectTypeahead control) {
        control.menuToggle().resetValidation();
    }

    @Override
    public void disable(SingleSelectTypeahead control) {
        control.disabled();
    }

    @Override
    public void afterSwitchedToNativeMode(SingleSelectTypeahead control, ResolvedAttribute attribute) {
        FormItemBricks.afterSwitchedToSingleSelectTypeahead(control, attribute);
    }

    private String value(SingleSelectTypeahead control) {
        return control != null ? control.menuToggle().searchInput().value() : "";
    }

    void update(String credentialStore) {
        this.credentialStore = credentialStore;
        aliasTypeahead.menu().refresh();
    }

    private AsyncItems<MenuList, MenuItem> aliases() {
        return menuList -> {
            if (credentialStore != null) {
                return uic().capabilityRegistry().findResources(CAPABILITY, credentialStore)
                        .then(templates -> {
                            // Results must be unique!
                            if (templates.size() == 1) {
                                Operation operation = new Operation.Builder(templates.get(0).resolve(),
                                        READ_ALIASES_OPERATION).build();
                                return uic().dispatcher().execute(operation).then(result ->
                                        resolve(result.asList().stream()
                                                .map(ModelNode::asString)
                                                .sorted()
                                                .map(c -> menuItem(c, c))
                                                .collect(toList())));
                            }
                            return resolve(emptyList());
                        });
            }
            return resolve(emptyList());
        };
    }
}
