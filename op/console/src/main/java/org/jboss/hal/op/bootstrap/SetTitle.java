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
package org.jboss.hal.op.bootstrap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Settings;
import org.jboss.hal.resources.Names;

import elemental2.promise.Promise;

import static elemental2.dom.DomGlobal.document;

/**
 * Bootstrap task that sets the browser document title. Supports placeholders in the title setting: {@code %n} for the
 * instance name and {@code %o} for the organisation. Falls back to the built-in title if a placeholder cannot be resolved.
 */
class SetTitle implements Task<FlowContext> {

    private static final Logger logger = Logger.getLogger(SetTitle.class.getName());
    private static final String NAME_PLACEHOLDER = "%n";
    private static final String ORGANIZATION_PLACEHOLDER = "%o";

    private final Settings settings;
    private final Map<String, Supplier<String>> data;

    SetTitle(Settings settings, Environment environment) {
        this.settings = settings;
        this.data = new HashMap<>();

        data.put(NAME_PLACEHOLDER, environment::instanceName);
        data.put(ORGANIZATION_PLACEHOLDER, environment::instanceOrganization);
    }

    @Override
    public Promise<FlowContext> apply(FlowContext context) {
        String title = settings.get(Settings.Key.TITLE).value();
        if (title != null && !title.isEmpty()) {
            for (Map.Entry<String, Supplier<String>> entry : data.entrySet()) {
                if (title.contains(entry.getKey())) {
                    String value = entry.getValue().get();
                    if (value != null && !value.isEmpty()) {
                        title = title.replace(entry.getKey(), value);
                    } else {
                        logger.error("Value for placeholder '%s' in custom title is undefined. " +
                                "Fall back to built in title.", entry.getKey());
                        title = Names.BROWSER_FALLBACK_TITLE;
                        break;
                    }
                }
            }
            document.title = title;
        }
        return Promise.resolve(context);
    }
}
