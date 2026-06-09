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
package org.jboss.hal.model.deployment;

import java.util.ArrayList;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.model.subsystem.Subsystem;

import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;

/**
 * A nested deployment within a parent {@link Deployment}, such as a WAR inside an EAR.
 */
public class Subdeployment extends NamedNode {

    private final Deployment parent;
    private final String name;
    private final List<Subsystem> subsystems;

    Subdeployment(Deployment parent, String name, ModelNode node) {
        super(name, node);
        this.parent = parent;
        this.name = name;
        this.subsystems = new ArrayList<>();
        if (node.hasDefined(SUBSYSTEM)) {
            Deployment.parseSubsystems(node, subsystems);
        }
    }

    @Override
    public String toString() {
        return "Subdeployment{" + name + "}";
    }

    /** Returns the parent deployment that contains this subdeployment. */
    public Deployment parent() {
        return parent;
    }

    /** Returns the subsystems within this subdeployment. */
    public List<Subsystem> subsystems() {
        return subsystems;
    }

    /** Returns {@code true} if this subdeployment contains a subsystem with the given name. */
    public boolean hasSubsystem(String name) {
        return subsystems.stream().anyMatch(subsystem -> name.equals(subsystem.name()));
    }
}
