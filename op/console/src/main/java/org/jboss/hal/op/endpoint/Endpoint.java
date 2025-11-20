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
package org.jboss.hal.op.endpoint;

import org.jboss.elemento.Id;
import org.jboss.elemento.logger.Logger;
import org.jboss.hal.dmr.ModelDescriptionConstants;
import org.jboss.hal.resources.Urls;

import elemental2.dom.Request;
import elemental2.dom.RequestInit;
import elemental2.dom.URL;
import elemental2.promise.Promise;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

import static elemental2.dom.DomGlobal.fetch;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class Endpoint {

    @JsOverlay
    public static final String CONNECT_PARAMETER = "connect";

    @JsOverlay
    private static final Logger logger = Logger.getLogger(Endpoint.class.getName());

    @JsOverlay
    public static Promise<Boolean> ping(String url) {
        String managementEndpoint = url + Urls.MANAGEMENT;
        if (url.contains("://") && !validUrl(managementEndpoint)) {
            logger.error("'%s' is not a valid URL", managementEndpoint);
            return Promise.resolve(false);
        }

        RequestInit init = RequestInit.create();
        init.setMethod("GET");
        init.setMode("cors");
        init.setCredentials("include");
        Request request = new Request(managementEndpoint, init);

        logger.debug("Ping %s", managementEndpoint);
        return fetch(request)
                .then(response -> {
                    if (response.status == 200) {
                        logger.debug("GET for endpoint %s returned 200. Check if it is a valid management interface.", url);
                        return response.text().then(text -> {
                            if (text.contains(ModelDescriptionConstants.MANAGEMENT_MAJOR_VERSION)) {
                                logger.debug("Endpoint %s is valid.", url);
                                return Promise.resolve(true);
                            } else {
                                logger.debug("Endpoint %s is not a valid management interface.", url);
                                return Promise.resolve(false);
                            }
                        });
                    } else {
                        logger.debug("GET for endpoint %s returned %d. Not a valid endpoint.", url, response.status);
                        return Promise.resolve(false);
                    }
                });
    }

    @JsOverlay
    private static boolean validUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @JsOverlay
    public static Endpoint endpoint(String name, String url) {
        Endpoint endpoint = new Endpoint();
        endpoint.id = Id.uuid();
        endpoint.name = name;
        endpoint.url = url;
        return endpoint;
    }

    public String id;
    public String name;
    public String url;

    @JsOverlay
    public final String failSafeUrl() {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
