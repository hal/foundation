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

    /** Creates a host instance representing a host that failed to connect. */
    public static Host failed(String name) {
        return new Host(name, false, true, true, null, null);
    }

    /** Creates a host instance representing a disconnected host with optional timestamps. */
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

    /** Creates a connected host from a model node. */
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

    /** Creates a connected host from a DMR property. The property name is used as the address name. */
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

    /** Returns the name used in the host's resource address. This may differ from {@link #name()} after a rename. */
    public String getAddressName() {
        return addressName;
    }

    /** Returns the management model version of this host. */
    public Version getManagementVersion() {
        return managementVersion;
    }

    /** Returns {@code true} if this host is connected to the domain controller. */
    public boolean isConnected() {
        return connected;
    }

    /** Returns the time this host was disconnected, or {@code null} if connected. */
    public Date getDisconnected() {
        return disconnected;
    }

    /** Returns the time this host was last connected, or {@code null} if unknown. */
    public Date getLastConnected() {
        return lastConnected;
    }

    /** Returns {@code true} if this host is the domain controller (primary). */
    public boolean isDomainController() {
        return hasDefined(PRIMARY) && get(PRIMARY).asBoolean();
    }

    /** @return the status as defined by {@code runtime-configuration-state} */
    public RuntimeConfigurationState getRuntimeConfigurationState() {
        return asEnumValue(this, RUNTIME_CONFIGURATION_STATE, RuntimeConfigurationState::valueOf,
                RuntimeConfigurationState.UNDEFINED);
    }

    /** Returns the host state as defined by {@code host-state}. */
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

    /** Returns {@code true} if this host is still booting. */
    public boolean isBooting() {
        return booting;
    }

    /** Returns {@code true} if the host state is {@link RunningState#STARTING}. */
    public boolean isStarting() {
        return getHostState() == STARTING;
    }

    /** Returns {@code true} if the host state is {@link RunningState#RUNNING}. */
    public boolean isRunning() {
        return getHostState() == RunningState.RUNNING;
    }

    /** Returns {@code true} if this host runs in admin-only mode. */
    public boolean isAdminMode() {
        return getRunningMode() == ADMIN_ONLY;
    }

    /** Returns {@code true} if this host failed to connect. */
    public boolean isFailed() {
        return failed;
    }

    /** Returns {@code true} if the host requires a restart to apply configuration changes. */
    public boolean needsRestart() {
        return getHostState() == RESTART_REQUIRED;
    }

    /** Returns {@code true} if the host requires a reload to apply configuration changes. */
    public boolean needsReload() {
        return getHostState() == RELOAD_REQUIRED;
    }

    /** Returns the management resource address for this host. */
    public ResourceAddress getAddress() {
        return new ResourceAddress().add(HOST, getAddressName());
    }
}
