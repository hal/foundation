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
package org.jboss.hal.op;

import org.jboss.as.controller.SubsystemSchema;
import org.jboss.as.controller.persistence.xml.ResourceXMLParticleFactory;
import org.jboss.as.controller.persistence.xml.SubsystemResourceRegistrationXMLElement;
import org.jboss.as.controller.persistence.xml.SubsystemResourceXMLSchema;
import org.jboss.as.controller.xml.VersionedNamespace;
import org.jboss.staxmapper.IntVersion;

/**
 * XML schema versions for the HAL on premise subsystem.
 */
public enum HalOpSubsystemSchema implements SubsystemResourceXMLSchema<HalOpSubsystemSchema> {

    /** Initial schema version ({@code urn:jboss:domain:halop:1.0}). */
    VERSION_1_0(1, 0);

    /** The current (latest) schema version. */
    static final HalOpSubsystemSchema CURRENT = VERSION_1_0;

    private final ResourceXMLParticleFactory factory = ResourceXMLParticleFactory.newInstance(this);
    private final VersionedNamespace<IntVersion, HalOpSubsystemSchema> namespace;

    /** Creates a schema version and derives the legacy subsystem URN namespace. */
    HalOpSubsystemSchema(int major, int minor) {
        this.namespace = SubsystemSchema.createLegacySubsystemURN(
                HalOpSubsystemRegistrar.NAME, new IntVersion(major, minor));
    }

    /** Returns the versioned namespace URI for this schema version. */
    @Override
    public VersionedNamespace<IntVersion, HalOpSubsystemSchema> getNamespace() {
        return this.namespace;
    }

    /** Returns the XML element definition used to parse and marshal the subsystem configuration. */
    @Override
    public SubsystemResourceRegistrationXMLElement getSubsystemXMLElement() {
        return this.factory.subsystemElement(HalOpSubsystemRegistrar.REGISTRATION).build();
    }
}
