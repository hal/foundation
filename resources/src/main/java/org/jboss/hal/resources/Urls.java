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
package org.jboss.hal.resources;

/**
 * Links used in HAL. Can contain specific placeholders for version, language, etc.
 */
public interface Urls {

    String BROWSE_ISSUES = "https://issues.jboss.org/browse/WFLY";
    String DEVELOPER_MAILING_LIST = "https://lists.jboss.org/archives/list/wildfly-dev@lists.jboss.org/";
    String FORUM = "https://groups.google.com/forum/#!forum/wildfly";
    String GETTING_STARTED = "https://www.wildfly.org/get-started/";
    String HAL_FOUNDATION = "https://github.com/hal/foundation";
    String LATEST_NEWS = "https://www.wildfly.org/news/";
    String LOGOUT = "/logout";
    String MANAGEMENT = "/management";
    String STABILITY_LEVELS = "https://docs.wildfly.org/%v/Admin_Guide.html#Feature_stability_levels";
    String UPLOAD = "/management-upload";
    String WILDFLY_CATALOG = "https://wildfly-extras.github.io/wildfly-catalog/%v.0.0.Final/index.html";
    String WILDFLY_DOCUMENTATION = "https://docs.wildfly.org/%v/";
    String WILDFLY_GUIDES = "https://www.wildfly.org/guides/";
    String WILDFLY_HOMEPAGE = "https://www.wildfly.org";
    String ZULIP_CHAT = "https://wildfly.zulipchat.com/";

    static String replaceVersion(String url, String version) {
        return url.replace("%v", version);
    }
}
