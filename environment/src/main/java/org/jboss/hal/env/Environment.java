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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import static org.jboss.hal.env.AccessControlProvider.SIMPLE;
import static org.jboss.hal.env.BuildType.DEVELOPMENT;
import static org.jboss.hal.env.OperationMode.STANDALONE;
import static org.jboss.hal.env.Stability.COMMUNITY;
import static org.jboss.hal.env.Version.EMPTY_VERSION;

@ApplicationScoped
public class Environment {

    private static final Stability LOWEST_LEVEL_WITHOUT_HIGHLIGH = COMMUNITY;

    private final String applicationId;
    private final String applicationName;
    private final Version applicationVersion;
    private final String base;
    private final BuildType buildType;
    private final Stability builtInStability;
    private String instanceName;
    private String instanceOrganization;
    private String productName;
    private Version productVersion;
    private Version managementVersion;
    private OperationMode operationMode;
    private Stability serverStability;
    private Stability[] permissibleStabilityLevels;
    private AccessControlProvider accessControlProvider;
    private boolean sso;
    private String domainController;

    public Environment() {
        // final instance variables must be passed to the
        // j2cl-maven-plugin as `<environment.x/>` closure defines (sse POM)
        this.applicationId = System.getProperty("environment.id");
        this.applicationName = System.getProperty("environment.name");
        this.applicationVersion = Version.parseVersion(System.getProperty("environment.version"));
        this.base = System.getProperty("environment.base", "/");
        this.buildType = BuildType.parse(System.getProperty("environment.build"), DEVELOPMENT);
        this.builtInStability = Stability.parse(System.getProperty("environment.stability"), COMMUNITY);

        // default values for the non-final properties
        this.instanceName = "undefined";
        this.instanceOrganization = "undefined";
        this.productName = "undefined";
        this.productVersion = EMPTY_VERSION;
        this.managementVersion = EMPTY_VERSION;
        this.operationMode = STANDALONE;
        this.serverStability = COMMUNITY;
        this.permissibleStabilityLevels = new Stability[0];
        this.accessControlProvider = SIMPLE;
        this.sso = false;
        this.domainController = "undefined";
    }

    // used for unit tests only
    Environment(String applicationId, String applicationName, Version applicationVersion,
            String base, BuildType buildType, Stability builtInStability) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        this.base = base;
        this.buildType = buildType;
        this.builtInStability = builtInStability;
    }

    // ------------------------------------------------------ update

    // update non-final properties as part of the bootstrap process
    public void update(String instanceName, String instanceOrganization, String productName, Version productVersion,
            Version managementVersion, OperationMode operationMode) {
        this.instanceName = instanceName;
        this.instanceOrganization = instanceOrganization;
        this.productName = productName;
        this.productVersion = productVersion;
        this.managementVersion = managementVersion;
        this.operationMode = operationMode;
    }

    public void update(String domainController) {
        this.domainController = domainController;
    }

    public void update(AccessControlProvider accessControlProvider, boolean sso) {
        this.accessControlProvider = accessControlProvider;
        this.sso = sso;
    }

    public void update(Stability stability, Stability[] permissibleStabilityLevels) {
        this.serverStability = stability;
        this.permissibleStabilityLevels = permissibleStabilityLevels;
    }

    // ------------------------------------------------------ getter

    public String applicationId() {
        return applicationId;
    }

    public String applicationName() {
        return applicationName;
    }

    public Version applicationVersion() {
        return applicationVersion;
    }

    public String base() {
        return base;
    }

    public BuildType buildType() {
        return buildType;
    }

    public String instanceName() {
        return instanceName;
    }

    public String instanceOrganization() {
        return instanceOrganization;
    }

    public String productName() {
        return productName;
    }

    public Version productVersion() {
        return productVersion;
    }

    /**
     * Retrieves the product version to be used in links.
     * <p>
     * The product version link is a representation of the product version in the format of "major.minor" if the minor version
     * is not 0, otherwise it only includes the major version. The product version is obtained from the
     * {@link #productVersion()} method.
     *
     * @return the product version link as a string
     * @see #productVersion()
     */
    public String productVersionLink() {
        return productVersion().minor() == 0
                ? String.valueOf(productVersion().major())
                : productVersion().major() + "." + productVersion().minor();
    }

    public Version managementVersion() {
        return managementVersion;
    }

    public OperationMode operationMode() {
        return operationMode;
    }

    public Stability serverStability() {
        return serverStability;
    }

    public Stability builtInStability() {
        return builtInStability;
    }

    /**
     * Determines if the stability level of the server is greater than or equal to the built-in stability level.
     * <p>
     * The built-in stability level is the stability level the console was built with. It is passed at built time using the
     * system property {@code environment.stability}.
     *
     * @return true if the stability level of the server is greater than or equal to the built-in stability level, false
     * otherwise.
     */
    public boolean highlightStability() {
        return checkStability(serverStability, builtInStability);
    }

    /**
     * Determines if the given stability level is greater than or equal tothe stability level of the server. If one or more
     * dependent stability levels are given, then this method returns true only if the dependent stability is greater than all
     * previous levels.
     * <p>
     * Use this method to check resources, attributes, operations and operation parameters, e.g.:
     * <ul>
     *     <li><code>highlightStability(resourceStability)</code></li>
     *     <li><code>highlightStability(resourceStability, attributeStability)</code></li>
     *     <li><code>highlightStability(resourceStability, operationStability)</code></li>
     *     <li><code>highlightStability(resourceStability, operationStability, parameterStability)</code></li>
     * </ul>
     *
     * @param stability            the stability level to compare with the server stability level
     * @param dependentStabilities optional dependent stability levels
     * @return true if the stability level and all dependent stability levels are greater than the stability level of the
     * server, false otherwise.
     */
    public boolean highlightStability(Stability stability, Stability... dependentStabilities) {
        boolean highlight = checkStability(stability, serverStability);
        if (dependentStabilities != null) {
            List<Stability> previous = new ArrayList<>();
            previous.add(serverStability);
            previous.add(stability);
            for (Stability dependency : dependentStabilities) {
                highlight = checkStability(dependency, previous);
                previous.add(dependency);
            }
        }
        return highlight;
    }

    public boolean standalone() {
        return operationMode == OperationMode.STANDALONE;
    }

    public boolean domain() {
        return operationMode == OperationMode.DOMAIN;
    }

    public AccessControlProvider accessControlProvider() {
        return accessControlProvider;
    }

    public boolean sso() {
        return sso;
    }

    public String domainController() {
        return domainController;
    }

    // ------------------------------------------------------ internal

    private static boolean checkStability(Stability stability, List<Stability> compareToAll) {
        boolean check = stability != null &&
                stability.order > LOWEST_LEVEL_WITHOUT_HIGHLIGH.order;
        for (Iterator<Stability> iterator = compareToAll.iterator(); iterator.hasNext() && check; ) {
            Stability compareTo = iterator.next();
            check = checkStability(stability, compareTo);
        }
        return check;
    }

    private static boolean checkStability(Stability stability, Stability compareTo) {
        return stability != null &&
                stability.order > LOWEST_LEVEL_WITHOUT_HIGHLIGH.order &&
                stability.order >= compareTo.order;
    }
}
