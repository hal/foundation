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

import org.jboss.as.version.Stability;
import org.wildfly.subsystem.SubsystemConfiguration;
import org.wildfly.subsystem.SubsystemExtension;
import org.wildfly.subsystem.SubsystemPersistence;

/**
 * WildFly extension that registers the HAL on premise management console on the management HTTP interface.
 */
public class HalOpExtension extends SubsystemExtension<HalOpSubsystemSchema> {

    public HalOpExtension() {
        super(SubsystemConfiguration.of(
                        HalOpSubsystemRegistrar.NAME,
                        HalOpSubsystemModel.CURRENT,
                        HalOpSubsystemRegistrar::new),
                SubsystemPersistence.of(HalOpSubsystemSchema.CURRENT));
    }

    @Override
    public Stability getStability() {
        return Stability.EXPERIMENTAL;
    }
}
