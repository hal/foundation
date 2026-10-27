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
package org.jboss.hal.mgt;

/**
 * Enumerates the facets available in the MGT catalog API. Each facet has a {@link #key} (the REST API query parameter name)
 * and a {@link #group} that classifies it as general or type-specific.
 *
 * <p>Type-specific facets belong to exactly one {@link Group}: {@link Group#ATTRIBUTE}, {@link Group#RESOURCE}, or
 * {@link Group#OPERATION}. When a type-specific facet is selected without an explicit type filter, the type is implicitly
 * constrained to the facet's group.
 */
public enum CatalogFacet {

    // ------------------------------------------------------ general

    TYPE("type", Group.GENERAL),
    STABILITY("stability", Group.GENERAL),
    DEPRECATED("deprecated", Group.GENERAL),

    // ------------------------------------------------------ resource

    SINGLETON("singleton", Group.RESOURCE),
    DECLARES_CAP("declaresCap", Group.RESOURCE),
    DEFINES_ATTR_GROUPS("definesAttrGroups", Group.RESOURCE),

    // ------------------------------------------------------ attribute

    DATA_TYPE("dataType", Group.ATTRIBUTE),
    ACCESS_TYPE("accessType", Group.ATTRIBUTE),
    STORAGE("storage", Group.ATTRIBUTE),
    RESTART("restart", Group.ATTRIBUTE),
    REQUIRED("required", Group.ATTRIBUTE),
    NILLABLE("nillable", Group.ATTRIBUTE),
    EXPRESSIONS("expressions", Group.ATTRIBUTE),
    SENSITIVE("sensitive", Group.ATTRIBUTE),
    HAS_ALLOWED("hasAllowed", Group.ATTRIBUTE),
    UNIT("unit", Group.ATTRIBUTE),
    HAS_RELATIONSHIPS("hasRelationships", Group.ATTRIBUTE),
    REFS_CAP("refsCap", Group.ATTRIBUTE),

    // ------------------------------------------------------ operation

    READ_ONLY("readOnly", Group.OPERATION),
    RUNTIME_ONLY("runtimeOnly", Group.OPERATION),
    GLOBAL("global", Group.OPERATION),
    HAS_PARAMS("hasParams", Group.OPERATION),
    HAS_RETURN_VALUE("hasReturnValue", Group.OPERATION);

    // ------------------------------------------------------ instance

    public final String key;
    public final Group group;

    CatalogFacet(String key, Group group) {
        this.key = key;
        this.group = group;
    }

    // ------------------------------------------------------ group

    /**
     * Classifies facets into general or type-specific groups. Type-specific groups carry an {@link #implicitType()} that
     * identifies which result type the group constrains to.
     */
    public enum Group {

        GENERAL(null),
        ATTRIBUTE(ModelGraphType.ATTRIBUTE),
        RESOURCE(ModelGraphType.RESOURCE),
        OPERATION(ModelGraphType.OPERATION);

        private final ModelGraphType implicitType;

        Group(ModelGraphType implicitType) {
            this.implicitType = implicitType;
        }

        /** Returns the type this group implicitly constrains to, or {@code null} for {@link #GENERAL}. */
        public ModelGraphType implicitType() {
            return implicitType;
        }
    }
}
