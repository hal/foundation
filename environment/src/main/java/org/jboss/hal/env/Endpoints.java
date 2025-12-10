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
package org.jboss.hal.env;

import jakarta.enterprise.context.ApplicationScoped;

import static elemental2.dom.DomGlobal.location;
import static org.jboss.hal.resources.Urls.LOGOUT;
import static org.jboss.hal.resources.Urls.MANAGEMENT;
import static org.jboss.hal.resources.Urls.UPLOAD;

@ApplicationScoped
public class Endpoints {

    private String dmr;
    private String logout;
    private String upload;
    private boolean sameOrigin;

    public Endpoints() {
        init(location.origin);
    }

    // init properties as part of the bootstrap process
    public void init(String url) {
        dmr = url + MANAGEMENT;
        logout = url + LOGOUT;
        upload = url + UPLOAD;
        sameOrigin = url.isEmpty();
    }

    /** @return the endpoint used to execute management operations. */
    public String dmr() {
        return dmr;
    }

    /** @return the endpoint used for file uploads. */
    public String upload() {
        return upload;
    }

    /** @return the endpoint used for logout. */
    public String logout() {
        return logout;
    }

    /**
     * @return {@code true} if the console is served from a WildFly / EAP instance, {@code false} if it runs standalone and
     * connected to an arbitrary management endpoint.
     */
    public boolean sameOrigin() {
        return sameOrigin;
    }
}
