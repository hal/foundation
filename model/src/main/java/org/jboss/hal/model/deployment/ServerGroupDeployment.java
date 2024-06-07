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

import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.DISABLED;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ENABLED;

/** A deployed content used in domain mode. */
public class ServerGroupDeployment extends Content {

    // ------------------------------------------------------ instance

    private final String serverGroup;
    private Deployment deployment; // might be null if there's no reference server available

    public ServerGroupDeployment(String serverGroup, ModelNode node) {
        super(node);
        this.serverGroup = serverGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerGroupDeployment)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        ServerGroupDeployment that = (ServerGroupDeployment) o;
        // noinspection SimplifiableIfStatement
        if (!serverGroup.equals(that.serverGroup)) {
            return false;
        }
        return name().equals(that.name());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + serverGroup.hashCode();
        result = 31 * result + name().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ServerGroupDeployment{" + name() + "@" + serverGroup + ", " + (enabled() ? ENABLED : DISABLED) + "}";
    }

    // ------------------------------------------------------ api

    public String serverGroup() {
        return serverGroup;
    }

    public Deployment deployment() {
        return deployment;
    }

    public void deployment(Deployment deployment) {
        this.deployment = deployment;
    }

    public boolean runningWithReferenceServer() {
        return deployment != null && deployment.status() == DeploymentStatus.OK && deployment.referenceServer() != null;
    }
}
