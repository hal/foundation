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
package org.jboss.hal.dmr.dispatch;

/**
 * Represents the state of a single WildFly server after a management operation. Each instance identifies a server by host
 * and server name and records whether it requires a reload or restart.
 */
public class ServerState {

    /** The required action for a server after a configuration change. */
    public enum State {
        /** The server needs a reload to apply configuration changes. */
        RELOAD_REQUIRED,
        /** The server needs a full restart to apply configuration changes. */
        RESTART_REQUIRED
    }

    private final String host;
    private final String server;
    private final State state;

    ServerState(String host, String server, State state) {
        this.host = host;
        this.server = server;
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerState)) {
            return false;
        }

        ServerState that = (ServerState) o;
        if (host != null ? !host.equals(that.host) : that.host != null) {
            return false;
        }
        if (!server.equals(that.server)) {
            return false;
        }
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + server.hashCode();
        result = 31 * result + state.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ServerState(");
        if (host != null && !host.isEmpty()) {
            builder.append(host).append(" / ");
        }
        builder.append(server).append(": ").append(state.name()).append(")");
        return builder.toString();
    }

    /** Returns the server name. */
    public String getServer() {
        return server;
    }

    /** Returns the host name, or {@code null} in standalone mode. */
    public String getHost() {
        return host;
    }

    /** Returns the server's required action (reload or restart). */
    public State getState() {
        return state;
    }
}
