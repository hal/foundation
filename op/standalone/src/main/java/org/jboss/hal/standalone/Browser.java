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

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;

import io.quarkus.logging.Log;

class Browser {

    static void open(String url) {
        try {
            // Check if we can use the AWT Desktop API
            if (!GraphicsEnvironment.isHeadless() &&
                    Desktop.isDesktopSupported() &&
                    Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {

                Desktop.getDesktop().browse(new URI(url));
                return;
            }

            // Fallback: use OS-specific commands
            String os = System.getProperty("os.name").toLowerCase();
            String[] command = getCommand(url, os);
            new ProcessBuilder(command)
                    .inheritIO()
                    .start();
        } catch (Exception e) {
            Log.error("Failed to open browser: " + e.getMessage());
        }
    }

    private static String[] getCommand(String url, String os) {
        String[] command;

        if (os.contains("win")) {
            command = new String[]{"rundll32", "url.dll,FileProtocolHandler", url};
        } else if (os.contains("mac")) {
            command = new String[]{"open", url};
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            command = new String[]{"xdg-open", url};
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + os);
        }
        return command;
    }
}
