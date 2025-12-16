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
package org.jboss.hal.model.host;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.env.Version;
import org.jboss.hal.model.HasServersNode;
import org.jboss.hal.model.RunningMode;
import org.jboss.hal.model.RunningState;
import org.jboss.hal.model.RuntimeConfigurationState;
import org.jboss.hal.model.SuspendState;

import static java.util.Comparator.comparing;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRIMARY;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNNING_MODE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RUNTIME_CONFIGURATION_STATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUSPEND_STATE;
import static org.jboss.hal.dmr.ModelNodeHelper.asEnumValue;
import static org.jboss.hal.model.RunningMode.ADMIN_ONLY;
import static org.jboss.hal.model.RunningState.RELOAD_REQUIRED;
import static org.jboss.hal.model.RunningState.RESTART_REQUIRED;
import static org.jboss.hal.model.RunningState.STARTING;

/**
 * For the host we need to distinguish between the address-name (the name which is part of the host address) and the
 * model-node-name (the name which is part of the host model node). When the latter is changed, the former remains unchanged
 * until the host is reloaded.
 */
public class Host extends HasServersNode {

    /**
     * Sorts the specified hosts alphabetically with the domain controller as the first element.
     */
    public static List<Host> sort(List<Host> hosts) {
        List<Host> sortedHosts;
        if (hosts != null) {
            sortedHosts = new ArrayList<>(hosts);
            sortedHosts.sort(comparing(Host::name));
        } else {
            sortedHosts = new ArrayList<>();
        }
        Host domainController = null;
        for (Iterator<Host> iterator = sortedHosts.iterator(); iterator.hasNext() && domainController == null; ) {
            Host host = iterator.next();
            if (host.isDomainController()) {
                domainController = host;
                iterator.remove();
            }
        }
        if (domainController != null) {
            sortedHosts.add(0, domainController);
        }
        return sortedHosts;
    }

    // ------------------------------------------------------ factory

    public static Host failed(String name) {
        return new Host(name, false, true, true, null, null);
    }

    public static Host disconnected(String name, Date disconnected, Date lastConnected) {
        return new Host(name, false, false, false, disconnected, lastConnected);
    }

    // ------------------------------------------------------ instance

    private final boolean booting;
    private final boolean connected;
    private final boolean failed;
    private final Date disconnected;
    private final Date lastConnected;
    private final String addressName;
    private final Version managementVersion;

    Host(String name, boolean booting, boolean connected, boolean failed, Date disconnected,
            Date lastConnected) {
        super(name, new ModelNode().setEmptyObject());
        this.booting = booting;
        this.connected = connected;
        this.failed = failed;
        this.disconnected = disconnected;
        this.lastConnected = lastConnected;
        this.addressName = name;
        this.managementVersion = Version.EMPTY_VERSION;
    }

    public Host(ModelNode node) {
        super(node.get(NAME).asString(), node);
        this.booting = false;
        this.connected = true;
        this.failed = false;
        this.disconnected = null;
        this.lastConnected = null;
        this.addressName = node.get(NAME).asString();
        this.managementVersion = ModelNodeHelper.parseVersion(node);
    }

    public Host(Property property) {
        super(property.getValue().get(NAME).asString(), property.getValue());
        this.booting = false;
        this.connected = true;
        this.failed = false;
        this.disconnected = null;
        this.lastConnected = null;
        this.addressName = property.getName();
        this.managementVersion = ModelNodeHelper.parseVersion(property.getValue());
    }

    public String getAddressName() {
        return addressName;
    }

    public Version getManagementVersion() {
        return managementVersion;
    }

    public boolean isConnected() {
        return connected;
    }

    public Date getDisconnected() {
        return disconnected;
    }

    public Date getLastConnected() {
        return lastConnected;
    }

    public boolean isDomainController() {
        return hasDefined(PRIMARY) && get(PRIMARY).asBoolean();
    }

    /** @return the status as defined by {@code runtime-configuration-state} */
    public RuntimeConfigurationState getRuntimeConfigurationState() {
        return asEnumValue(this, RUNTIME_CONFIGURATION_STATE, RuntimeConfigurationState::valueOf,
                RuntimeConfigurationState.UNDEFINED);
    }

    public RunningState getHostState() {
        return asEnumValue(this, HOST_STATE, RunningState::valueOf, RunningState.UNDEFINED);
    }

    /** @return the state as defined by {@code server.running-mode} */
    public RunningMode getRunningMode() {
        return asEnumValue(this, RUNNING_MODE, RunningMode::valueOf, RunningMode.UNDEFINED);
    }

    /** @return the state as defined by {@code server.suspend-state} */
    public SuspendState getSuspendState() {
        return asEnumValue(this, SUSPEND_STATE, SuspendState::valueOf, SuspendState.UNDEFINED);
    }

    /** Shortcut for {@link #isConnected()} {@code && !}{@link #isBooting()} */
    public boolean isAlive() {
        return isConnected() && !isBooting();
    }

    public boolean isBooting() {
        return booting;
    }

    public boolean isStarting() {
        return getHostState() == STARTING;
    }

    public boolean isRunning() {
        return getHostState() == RunningState.RUNNING;
    }

    public boolean isAdminMode() {
        return getRunningMode() == ADMIN_ONLY;
    }

    public boolean isFailed() {
        return failed;
    }

    public boolean needsRestart() {
        return getHostState() == RESTART_REQUIRED;
    }

    public boolean needsReload() {
        return getHostState() == RELOAD_REQUIRED;
    }

    public ResourceAddress getAddress() {
        return new ResourceAddress().add(HOST, getAddressName());
    }
}
