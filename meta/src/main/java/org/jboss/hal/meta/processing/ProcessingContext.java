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
package org.jboss.hal.meta.processing;

import java.util.Set;

import org.jboss.elemento.flow.FlowContext;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;

class ProcessingContext extends FlowContext {

    final Set<AddressTemplate> templates;
    final boolean recursive;
    final RepositoryStatus repositoryStatus;
    final RrdResult rrdResult;
    Metadata metadata;

    ProcessingContext(Set<AddressTemplate> templates, boolean recursive) {
        this.templates = templates;
        this.recursive = recursive;
        this.repositoryStatus = new RepositoryStatus(templates);
        this.rrdResult = new RrdResult();
        this.metadata = Metadata.empty();
    }
}
