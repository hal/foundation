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
package org.jboss.hal.op.configuration.security;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.ui.resource.shell.ResourceTabs;
import org.jboss.hal.ui.resource.spi.ResourceTabsProvider;

import elemental2.promise.Promise;

import static org.jboss.hal.op.configuration.security.AliasManager.aliasManager;

@ApplicationScoped
public class CredentialStoreTabs implements ResourceTabsProvider {

    private final Dispatcher dispatcher;

    @Inject
    public CredentialStoreTabs(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Set<AddressTemplate> scopes() {
        Set<AddressTemplate> scopes = new LinkedHashSet<>();
        scopes.add(AddressTemplate.ofTrusted("subsystem=elytron/credential-store=*"));
        scopes.add(AddressTemplate.ofTrusted("subsystem=elytron/secret-key-credential-store=*"));
        return scopes;
    }

    @Override
    public boolean appliesTo(Environment environment, AddressTemplate template) {
        return template.fullyQualified();
    }

    @Override
    public Promise<ResourceTabs> customizeTabs(AddressTemplate template, Metadata metadata, ResourceTabs defaultTabs) {
        return Promise.resolve(defaultTabs.addTab(1, "credential-store-aliases", "Aliases",
                aliasManager(dispatcher, template, metadata).element()));
    }
}
