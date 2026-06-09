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

    /** WildFly issue tracker. */
    String BROWSE_ISSUES = "https://redhat.atlassian.net/jira/software/c/projects/WFLY/summary";

    /** WildFly developer mailing list archive. */
    String DEVELOPER_MAILING_LIST = "https://lists.jboss.org/archives/list/wildfly-dev@lists.jboss.org/";

    /** WildFly community forum. */
    String FORUM = "https://groups.google.com/forum/#!forum/wildfly";

    /** WildFly getting started guide. */
    String GETTING_STARTED = "https://www.wildfly.org/get-started/";

    /** WildFly latest news page. */
    String LATEST_NEWS = "https://www.wildfly.org/news/";

    /** Relative path to the management interface logout endpoint. */
    String LOGOUT = "/logout";

    /** Relative path to the management interface DMR endpoint. */
    String MANAGEMENT = "/management";

    /** Documentation link for stability levels; contains the {@code %v} version placeholder. */
    String STABILITY_LEVELS = "https://docs.wildfly.org/%v/Admin_Guide.html#Feature_stability_levels";

    /** Relative path to the management interface upload endpoint. */
    String UPLOAD = "/management-upload";

    /** WildFly catalog link; contains the {@code %v} version placeholder. */
    String WILDFLY_CATALOG = "https://docs.wildfly.org/wildfly-catalog/%v.0.0.Final/default/index.html";

    /** WildFly documentation root; contains the {@code %v} version placeholder. */
    String WILDFLY_DOCUMENTATION = "https://docs.wildfly.org/%v/";

    /** WildFly guides page. */
    String WILDFLY_GUIDES = "https://www.wildfly.org/guides/";

    /** WildFly project homepage. */
    String WILDFLY_HOMEPAGE = "https://www.wildfly.org";

    /** WildFly Zulip chat. */
    String ZULIP_CHAT = "https://wildfly.zulipchat.com/";

    /**
     * Replaces the {@code %v} placeholder in the given URL with the specified WildFly version.
     *
     * @param url     the URL containing the {@code %v} placeholder
     * @param version the WildFly version to substitute
     * @return the URL with the version placeholder replaced
     */
    static String replaceVersion(String url, String version) {
        return url.replace("%v", version);
    }
}
