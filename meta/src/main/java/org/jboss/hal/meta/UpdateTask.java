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
package org.jboss.hal.meta;

import java.util.Map;
import java.util.Set;

import org.jboss.elemento.flow.Task;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

import elemental2.promise.Promise;

import static org.jboss.hal.meta.Metadata.metadata;

/** Task that transfers parsed {@link RrdResult} data (resource descriptions and security contexts) into the {@link MetadataRepository}. */
class UpdateTask implements Task<ProcessingContext> {

    private static final Logger logger = Logger.getLogger(UpdateTask.class.getName());
    private final MetadataRepository metadataRepository;

    UpdateTask(MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
    }

    @Override
    public Promise<ProcessingContext> apply(ProcessingContext context) {
        if (context.rrdResult.shouldUpdate()) {
            for (Map.Entry<String, ResourceDescription> entry : context.rrdResult.resourceDescriptions.entrySet()) {
                String address = entry.getKey();
                ResourceDescription resourceDescription = entry.getValue();
                SecurityContext securityContext = context.rrdResult.securityContexts.get(address);
                if (securityContext == null) {
                    logger.warn("No security context for %s in rrd results. Fallback to read-only security context.", address);
                    securityContext = SecurityContext.READ_ONLY;
                }
                metadataRepository.addMetadata(metadata(address, resourceDescription, securityContext));
            }
            for (Map.Entry<String, Set<String>> entry : context.rrdResult.processedAddresses.entrySet()) {
                metadataRepository.addProcessedAddresses(entry.getKey(), entry.getValue());
            }
        }
        return Promise.resolve(context);
    }
}
