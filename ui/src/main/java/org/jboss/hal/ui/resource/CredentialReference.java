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

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelType;
import org.jboss.hal.meta.description.AttributeDescription;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ALIAS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLEAR_TEXT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STORE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TYPE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.VALUE_TYPE;

/**
 * {@link CompositeAttribute} implementation that matches credential reference attributes by their structure: an OBJECT with a
 * value-type containing {@code store}, {@code alias}, and {@code clear-text} sub-attributes.
 * <p>
 * This structural match covers all name variants used across the WildFly management model:
 * <ul>
 *     <li>{@code credential-reference} — datasources, elytron, mail, messaging, undertow</li>
 *     <li>{@code cluster-credential-reference} — messaging-activemq server</li>
 *     <li>{@code key-credential-reference} — jgroups encryption protocols</li>
 *     <li>{@code key-password-credential-reference}, {@code keystore-password-credential-reference} — audit syslog TLS</li>
 *     <li>{@code recovery-credential-reference} — XA datasources, resource adapters</li>
 *     <li>{@code source-credential-reference}, {@code target-credential-reference} — JMS bridges</li>
 * </ul>
 * <p>
 * A credential reference operates in one of three modes:
 * <ol>
 *     <li><b>Store reference</b> — {@code store} + {@code alias} are set, referencing an entry in a credential store. This is
 *         the recommended, secure configuration.</li>
 *     <li><b>Clear text</b> — only {@code clear-text} is set. The password is visible in the server configuration.</li>
 *     <li><b>Undefined</b> — no sub-attributes are set.</li>
 * </ol>
 *
 * @see CompositeAttributes
 */
public class CredentialReference implements CompositeAttribute {

    /** The credential reference mode derived from which sub-attributes have values. */
    public enum Mode {
        /** {@code store} and {@code alias} are set — references a credential store entry. */
        STORE_REFERENCE,
        /** Only {@code clear-text} is set — password visible in configuration. */
        CLEAR_TEXT,
        /** No sub-attributes are set. */
        UNDEFINED
    }

    /** Detects the credential reference mode from the attribute's current value. */
    public static Mode mode(ModelNode value) {
        if (value.isDefined()) {
            boolean hasStore = value.hasDefined(STORE);
            boolean hasAlias = value.hasDefined(ALIAS);
            boolean hasClearText = value.hasDefined(CLEAR_TEXT);
            if (hasStore || hasAlias) {
                return Mode.STORE_REFERENCE;
            } else if (hasClearText) {
                return Mode.CLEAR_TEXT;
            }
        }
        return Mode.UNDEFINED;
    }

    @Override
    public boolean matches(AttributeDescription description) {
        try {
            ModelType type = description.get(TYPE).asType();
            if (type != ModelType.OBJECT) {
                return false;
            }
            if (!description.hasDefined(VALUE_TYPE)) {
                return false;
            }
            if (description.get(VALUE_TYPE).getType() != ModelType.OBJECT) {
                return false;
            }
            ModelNode valueType = description.get(VALUE_TYPE);
            return valueType.has(STORE) && valueType.has(ALIAS) && valueType.has(CLEAR_TEXT);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
