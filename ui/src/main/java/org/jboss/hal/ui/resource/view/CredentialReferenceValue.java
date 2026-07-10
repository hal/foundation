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

import org.jboss.elemento.Attachable;
import org.jboss.elemento.IsElement;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.AttributeDescriptions;
import org.jboss.hal.ui.modelbrowser.ModelBrowserEvents.SelectInTree;
import org.jboss.hal.ui.resource.CredentialReference;
import org.jboss.hal.ui.resource.CredentialReference.Mode;
import org.jboss.hal.ui.resource.ResourceAttribute;

import elemental2.dom.HTMLElement;
import elemental2.dom.MutationRecord;

import static org.jboss.elemento.Elements.span;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CAPABILITY_REFERENCE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.resources.HalClasses.credentialReference;
import static org.jboss.hal.resources.HalClasses.halComponent;
import static org.jboss.hal.resources.HalClasses.resource;
import static org.jboss.hal.resources.HalClasses.undefined;
import static org.jboss.hal.resources.HalClasses.value;
import static org.jboss.hal.resources.HalClasses.view;
import static org.jboss.hal.ui.UIContext.uic;
import static org.patternfly.component.Severity.success;
import static org.patternfly.component.Severity.warning;
import static org.patternfly.component.button.Button.button;
import static org.patternfly.component.label.Label.label;
import static org.patternfly.component.tooltip.Tooltip.tooltip;
import static org.patternfly.layout.flex.AlignItems.center;
import static org.patternfly.layout.flex.Flex.flex;
import static org.patternfly.layout.flex.Gap.sm;

/**
 * Read-only view component for credential reference attributes. Renders a consolidated single-line display with a status label
 * badge indicating the mode and inline values:
 * <ul>
 *     <li><b>Store reference</b> — green status label + clickable store name (navigates to credential store) + alias</li>
 *     <li><b>Clear text</b> — warning status label and masked password with show/hide toggle</li>
 *     <li><b>Undefined</b> — gray "Not configured" text</li>
 * </ul>
 */
class CredentialReferenceValue implements IsElement<HTMLElement>, Attachable {

    // ------------------------------------------------------ factory

    static CredentialReferenceValue credentialReferenceValue(ResourceAttribute ra) {
        return new CredentialReferenceValue(ra);
    }

    // ------------------------------------------------------ instance

    private static final String MASKED = "••••••••";
    private static final Logger logger = Logger.getLogger(CredentialReferenceValue.class.getName());

    private final HTMLElement root;
    private final ResourceAttribute ra;
    private final Mode mode;
    private String capability;
    private HTMLElement storeLink;
    private AddressTemplate storeTemplate;

    CredentialReferenceValue(ResourceAttribute ra) {
        this.ra = ra;
        this.mode = CredentialReference.mode(ra.value);

        this.root = flex().css(halComponent(resource, view, credentialReference))
                .alignItems(center).columnGap(sm)
                .element();

        switch (mode) {
            case STORE_REFERENCE:
                buildStoreReference();
                break;
            case CLEAR_TEXT:
                buildClearText();
                break;
            case UNDEFINED:
                buildUndefined();
                break;
        }
        Attachable.register(this, this);
    }

    private void buildStoreReference() {
        String storeValue = ra.value.get(STORE).asString();
        String aliasValue = ra.value.hasDefined(ALIAS) ? ra.value.get(ALIAS).asString() : null;

        // resolve capability from the nested store attribute description
        AttributeDescriptions nested = ra.description.valueTypeAttributeDescriptions();
        AttributeDescription storeDescription = nested.get(STORE);
        if (storeDescription.hasDefined(CAPABILITY_REFERENCE)) {
            capability = storeDescription.get(CAPABILITY_REFERENCE).asString();
        }

        storeLink = button(storeValue).link().inline()
                .onClick((e, btn) -> navigateToStore(storeValue))
                .element();

        root.appendChild(label("Credential store").status(success).element());
        root.appendChild(storeLink);
        if (aliasValue != null) {
            root.appendChild(span().css(halComponent(resource, view, credentialReference))
                    .text("/")
                    .element());
            root.appendChild(span().css(halComponent(resource, view, credentialReference, value))
                    .text(aliasValue)
                    .element());
        }
    }

    private void buildClearText() {
        String clearTextValue = ra.value.get(CLEAR_TEXT).asString();
        HTMLElement maskedElement = span().text(MASKED).element();
        HTMLElement showButton = button("Show").link().inline()
                .onClick((e, btn) -> {
                    String current = maskedElement.textContent;
                    if (MASKED.equals(current)) {
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

    private void buildUndefined() {
        root.classList.add(halComponent(resource, view, undefined));
        root.appendChild(span().text("undefined").element());
    }

    @Override
    public void attach(MutationRecord mutationRecord) {
        if (mode == Mode.STORE_REFERENCE && capability != null && storeLink != null) {
            String storeValue = ra.value.get(STORE).asString();
            uic().capabilityRegistry().findResources(capability, storeValue)
                    .then(templates -> {
                        if (!templates.isEmpty()) {
                            storeTemplate = templates.get(0);
                            storeLink.appendChild(
                                    tooltip(storeLink, storeTemplate.toString()).element());
                        } else {
                            logger.warn("No credential store resource found for store '%s' via capability '%s'",
                                    storeValue, capability);
                        }
                        return null;
                    });
        }
    }

    @Override
    public HTMLElement element() {
        return root;
    }

    // ------------------------------------------------------ internal

    private void navigateToStore(String storeValue) {
        if (storeTemplate != null) {
            SelectInTree.dispatch(root, storeTemplate);
        } else if (capability != null) {
            uic().capabilityRegistry().findResources(capability, storeValue)
                    .then(templates -> {
                        if (!templates.isEmpty()) {
                            storeTemplate = templates.get(0);
                            SelectInTree.dispatch(root, storeTemplate);
                        } else {
                            logger.warn("Cannot navigate: no resource found for store '%s'", storeValue);
                        }
                        return null;
                    });
        }
    }
}
