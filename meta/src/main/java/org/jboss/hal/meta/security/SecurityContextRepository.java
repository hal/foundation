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
package org.jboss.hal.meta.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.hal.meta.Repository;
import org.jboss.hal.meta.StatementContext;

@ApplicationScoped
public class SecurityContextRepository extends Repository<SecurityContext> {

    private static final int CAPACITY = 500;

    @Inject
    public SecurityContextRepository(StatementContext statementContext) {
        super("security context", CAPACITY, new SecurityContextResolver(statementContext));
    }
}
