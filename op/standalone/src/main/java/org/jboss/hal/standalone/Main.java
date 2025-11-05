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
package org.jboss.hal.standalone;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

@ApplicationScoped
public class Main {

    @ConfigProperty(name = "quarkus.http.port")
    int port;

    @ConfigProperty(name = "browser.open")
    boolean openBrowser;

    public void init(@Observes Router router) {
        router.get().order(0).handler(StaticHandler.create());
        if (openBrowser) {
            new Thread(() -> {
                waitForServerReady(port);
                Browser.open("http://localhost:" + port);
            }).start();
        }
    }

    private void waitForServerReady(int port) {
        while (!isPortAvailable(port)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private boolean isPortAvailable(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", port), 100);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
