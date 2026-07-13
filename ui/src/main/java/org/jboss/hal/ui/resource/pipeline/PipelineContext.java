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
package org.jboss.hal.ui.resource.pipeline;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.description.AttributeDescription;
import org.jboss.hal.meta.description.ResourceDescription;
import org.jboss.hal.meta.security.SecurityContext;

/**
 * Bundles all inputs the pipeline needs: the resource address template, metadata (descriptions + security context), the
 * resource's current attribute values, and pipeline flags.
 * <p>
 * The {@code AddressTemplate} + {@code Metadata} pair is the standard context used throughout the halOP API. The
 * {@code resource} model node holds the current attribute values from a {@code read-resource} operation.
 */
public record PipelineContext(AddressTemplate template, Metadata metadata, ModelNode resource, PipelineFlags flags) {

    public ResourceDescription resourceDescription() {
        return metadata.resourceDescription();
    }

    public SecurityContext securityContext() {
        return metadata.securityContext();
    }

    /** Returns the current value of the given attribute from the resource model node. */
    public ModelNode value(AttributeDescription description) {
        if (description.nested()) {
            return ModelNodeHelper.nested(resource, description.fullyQualifiedName());
        }
        return resource.get(description.name());
    }

    /** Returns whether the given attribute is readable according to the security context. */
    public boolean readable(AttributeDescription description) {
        String name = description.nested() ? description.root().name() : description.name();
        return securityContext().readable(name);
    }

    /** Returns whether the given attribute is writable according to the security context. */
    public boolean writable(AttributeDescription description) {
        String name = description.nested() ? description.root().name() : description.name();
        return securityContext().writable(name);
    }
}
