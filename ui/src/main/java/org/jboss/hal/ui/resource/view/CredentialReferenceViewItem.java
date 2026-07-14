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
package org.jboss.hal.ui.resource.view;

import org.jboss.hal.ui.resource.pipeline.PipelineContext;
import org.jboss.hal.ui.resource.ResolvedAttribute;

import org.jboss.hal.ui.resource.pipeline.CredentialReferenceProvider;
import org.jboss.hal.ui.resource.pipeline.CredentialReferenceProvider.Mode;

import elemental2.dom.HTMLElement;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.resources.HalClasses.credentialReference;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.value;
import static org.jboss.hal.resources.HalClasses.view;
import static org.patternfly.component.Severity.success;
import static org.patternfly.component.Severity.warning;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.Gap.sm;

/**
 * View item for credential reference composite attributes. Shows mode-dependent display: credential store reference with
 * store + alias, clear text with masked password, or "undefined".
 */
public class CredentialReferenceViewItem extends AbstractViewItem {

    private static final String MASKED = "••••••••";

    public CredentialReferenceViewItem(String identifier, ResolvedAttribute attribute, PipelineContext context) {
        super(identifier, attribute, context);
    }

    @Override
    protected HTMLElement definedValue() {
        Mode mode = CredentialReferenceProvider.mode(attribute.value());
        HTMLElement root = flex().css(halComponent(resource, view, credentialReference))
                .alignItems(center).columnGap(sm)
                .element();

        switch (mode) {
            case STORE_REFERENCE:
                buildStoreReference(root);
                break;
            case CLEAR_TEXT:
                buildClearText(root);
                break;
            default:
                root.appendChild(span().text("Not configured").element());
                break;
        }
        return root;
    }

    private void buildStoreReference(HTMLElement root) {
        String storeValue = attribute.value().get(STORE).asString();
        String aliasValue = attribute.value().hasDefined(ALIAS) ? attribute.value().get(ALIAS).asString() : null;

        root.appendChild(label("Credential store").status(success).element());
        root.appendChild(span().css(halComponent(resource, view, credentialReference, value))
                .text(storeValue).element());
        if (aliasValue != null) {
            root.appendChild(span().css(halComponent(resource, view, credentialReference))
                    .text("/").element());
            root.appendChild(span().css(halComponent(resource, view, credentialReference, value))
                    .text(aliasValue).element());
        }
    }

    private void buildClearText(HTMLElement root) {
        String clearTextValue = attribute.value().get(CLEAR_TEXT).asString();
        HTMLElement maskedElement = span().text(MASKED).element();
        HTMLElement showButton = button("Show").link().inline()
                .onClick((e, btn) -> {
                    if (MASKED.equals(maskedElement.textContent)) {
                        maskedElement.textContent = clearTextValue;
                        btn.text("Hide");
                    } else {
                        maskedElement.textContent = MASKED;
                        btn.text("Show");
                    }
                })
                .element();

        root.appendChild(label("Clear text").status(warning).element());
        root.appendChild(maskedElement);
        root.appendChild(showButton);
    }
}
