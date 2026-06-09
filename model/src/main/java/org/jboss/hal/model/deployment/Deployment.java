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
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.model.server.Server;
import org.jboss.hal.model.subsystem.Subsystem;

import elemental2.core.JsDate;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED_TIME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.STATUS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBDEPLOYMENT;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;

/** A deployment on a specific server. */
public class Deployment extends Content {

    /**
     * Expects a "subsystem" child resource. Modeled as a static helper method to make it usable from both deployments and
     * subdeployments.
     */
    static void parseSubsystems(ModelNode node, List<Subsystem> subsystems) {
        List<Property> properties = node.get(SUBSYSTEM).asPropertyList();
        for (Property property : properties) {
            Subsystem subsystem = new Subsystem(property);
            subsystems.add(subsystem);
        }
    }

    // ------------------------------------------------------ instance

    private final Server referenceServer;
    private final List<Subdeployment> subdeployments;
    private final List<Subsystem> subsystems;

    /** Creates a new deployment on the given reference server. Parses subsystems and subdeployments from the model node. */
    public Deployment(Server referenceServer, ModelNode node) {
        super(node);
        this.referenceServer = referenceServer;
        this.subdeployments = new ArrayList<>();
        this.subsystems = new ArrayList<>();

        if (node.hasDefined(SUBSYSTEM)) {
            parseSubsystems(node, subsystems);
        }
        if (node.hasDefined(SUBDEPLOYMENT)) {
            List<Property> properties = node.get(SUBDEPLOYMENT).asPropertyList();
            for (Property property : properties) {
                Subdeployment subdeployment = new Subdeployment(this, property.getName(), property.getValue());
                subdeployments.add(subdeployment);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deployment)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Deployment that = (Deployment) o;
        if (!referenceServer.equals(that.referenceServer)) {
            return false;
        }
        return name().equals(that.name());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + referenceServer.hashCode();
        result = 31 * result + name().hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Deployment(").append(name());
        if (!standalone()) {
            builder.append("@").append(referenceServer.getHost()).append("/").append(referenceServer.name());
        }
        builder.append(", ")
                .append((enabled() ? ENABLED : DISABLED))
                .append(", ")
                .append(status());
        builder.append(")");
        return builder.toString();
    }

    // ------------------------------------------------------ api

    /** Returns {@code true} if this deployment is on a standalone server. */
    public boolean standalone() {
        return referenceServer.isStandalone();
    }

    /** Returns the server on which this deployment was read. */
    public Server referenceServer() {
        return referenceServer;
    }

    /** Returns the runtime status of this deployment. */
    public DeploymentStatus status() {
        return ModelNodeHelper.asEnumValue(this, STATUS, DeploymentStatus::valueOf, DeploymentStatus.UNDEFINED);
    }

    /** Returns the time this deployment was enabled, or {@code null} if not available. */
    public JsDate enabledTime() {
        ModelNode node = get(ENABLED_TIME);
        if (node.isDefined()) {
            return new JsDate(node.asDouble());
        }
        return null;
    }

    /** Returns the time this deployment was disabled, or {@code null} if not available. */
    public JsDate disabledTime() {
        ModelNode node = get(DISABLED_TIME);
        if (node.isDefined()) {
            return new JsDate(node.asDouble());
        }
        return null;
    }

    /** Returns {@code true} if this deployment contains subdeployments. */
    public boolean hasSubdeployments() {
        return !subdeployments.isEmpty();
    }

    /** Returns the subdeployments within this deployment. */
    public List<Subdeployment> subdeployments() {
        return subdeployments;
    }

    /** Returns {@code true} if this deployment contains a subsystem with the given name. */
    public boolean hasSubsystem(String name) {
        return subsystems.stream().anyMatch(subsystem -> name.equals(subsystem.name()));
    }

    /** Returns {@code true} if any subdeployment contains a subsystem with the given name. */
    public boolean hasNestedSubsystem(String name) {
        for (Subdeployment subdeployment : subdeployments) {
            for (Subsystem subsystem : subdeployment.subsystems()) {
                if (name.equals(subsystem.name())) {
                    return true;
                }
            }
        }
        return false;
    }
}
