# Custom Resource Header Extension Point — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the registry infrastructure for custom resource headers, allowing resources to replace or augment the default `ResourceHeader` via CDI-discovered providers keyed by `AddressTemplate`.

**Architecture:** Three new types in `org.jboss.hal.ui.resource.extension`: a `ResourceContext` record (shared data carrier), a `ResourceHeaderProvider` interface (scope + activation + factory), and a `ResourceHeaderRegistry` CDI bean (best-prefix template matching with `appliesTo` filtering). The registry is wired into `UIContext` for access by non-CDI UI classes.

**Tech Stack:** Java 11+, Jakarta CDI (Crysknife), JUnit 5

## Global Constraints

- License header: Apache 2.0 (2024 Red Hat) — copy from any existing file in `ui/src/main/java/`
- Style: 4-space indent, UTF-8, max line 128, LF endings
- Package: `org.jboss.hal.ui.resource.extension` (new package in `ui` module)
- No changes to `ResourceShell` or `ModelBrowserDetail` — integration point is deferred
- Matching algorithm duplicated from `RouteRegistry`, not extracted yet

---

### Task 1: ResourceContext record and ResourceHeaderProvider interface

**Files:**
- Create: `ui/src/main/java/org/jboss/hal/ui/resource/extension/ResourceContext.java`
- Create: `ui/src/main/java/org/jboss/hal/ui/resource/extension/ResourceHeaderProvider.java`
- Create: `ui/src/main/java/org/jboss/hal/ui/resource/extension/package-info.java`

**Interfaces:**
- Consumes: `org.jboss.hal.meta.AddressTemplate`, `org.jboss.hal.meta.Metadata`, `org.jboss.hal.dmr.ModelNode`, `org.jboss.hal.env.Environment`, `org.jboss.hal.ui.resource.ResourceHeader`
- Produces: `ResourceContext(AddressTemplate, Metadata, ModelNode)` record, `ResourceHeaderProvider` interface with `scope()`, `appliesTo(Environment, AddressTemplate)`, `createHeader(ResourceContext, ResourceHeader)`

- [ ] **Step 1: Create the package-info.java**

```java
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

/**
 * Extension points for the resource shell components. Provides registries that allow resource-specific UI customizations
 * (headers, tabs, dialogs, etc.) to be contributed via CDI and matched against address templates.
 */
package org.jboss.hal.ui.resource.extension;
```

- [ ] **Step 2: Create ResourceContext.java**

```java
/*
 *  Copyright 2024 Red Hat
 *  ... (full Apache 2.0 header)
 */
package org.jboss.hal.ui.resource.extension;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Metadata;

/**
 * Bundles the data that extension point providers need to render resource-specific UI. Designed to be shared across
 * extension points (headers, tabs, data views, dialogs).
 *
 * @param template  the concrete resource template being rendered
 *                  (e.g., {@code /subsystem=datasources/data-source=ExampleDS})
 * @param metadata  the resource's management model metadata
 * @param modelNode the resource's current attribute values (loaded via {@code read-resource})
 */
public record ResourceContext(AddressTemplate template, Metadata metadata, ModelNode modelNode) {}
```

- [ ] **Step 3: Create ResourceHeaderProvider.java**

```java
/*
 *  Copyright 2024 Red Hat
 *  ... (full Apache 2.0 header)
 */
package org.jboss.hal.ui.resource.extension;

import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.resource.ResourceHeader;

import elemental2.dom.HTMLElement;

/**
 * Extension point for providing custom resource headers. Implementations are discovered via CDI and registered in
 * {@link ResourceHeaderRegistry}.
 * <p>
 * Each provider declares:
 * <ul>
 *     <li>{@link #scope()} — the address template pattern it applies to (supports wildcards, matched via best-prefix)</li>
 *     <li>{@link #appliesTo(Environment, AddressTemplate)} — runtime activation condition</li>
 *     <li>{@link #createHeader(ResourceContext, ResourceHeader)} — the custom header factory</li>
 * </ul>
 * <p>
 * Providers can fully replace the default header or augment it by wrapping the passed {@code defaultHeader}:
 * <pre>
 * // Full replacement — ignore defaultHeader
 * public IsElement&lt;HTMLElement&gt; createHeader(ResourceContext context, ResourceHeader defaultHeader) {
 *     return content().add(title(1, _3xl, "Custom Title"));
 * }
 *
 * // Augmentation — wrap defaultHeader with extra content
 * public IsElement&lt;HTMLElement&gt; createHeader(ResourceContext context, ResourceHeader defaultHeader) {
 *     return div()
 *             .add(defaultHeader)
 *             .add(button("Test Connection"));
 * }
 * </pre>
 */
public interface ResourceHeaderProvider {

    /**
     * The address template pattern this provider applies to. Supports wildcards
     * (e.g., {@code /subsystem=datasources/data-source=*}). Matched via best-prefix against the concrete template
     * being rendered.
     */
    AddressTemplate scope();

    /**
     * Whether this provider applies for the given environment and concrete resource template. Called at lookup time
     * when the environment is fully populated. Defaults to {@code true}.
     *
     * @param environment the runtime environment (operation mode, stability, product version, etc.)
     * @param template    the concrete resource template being rendered
     */
    default boolean appliesTo(Environment environment, AddressTemplate template) {
        return true;
    }

    /**
     * Creates a custom header for the given resource.
     *
     * @param context       the resource context with template, metadata, and current attribute values
     * @param defaultHeader the default {@link ResourceHeader}, fully configured but lazily built. The provider can
     *                      use it for augmentation (wrap with extra content) or ignore it for full replacement
     *                      (no build cost if unused).
     */
    IsElement<HTMLElement> createHeader(ResourceContext context, ResourceHeader defaultHeader);
}
```

- [ ] **Step 4: Verify compilation**

Run: `mvn compile -pl ui -am -P quick-build`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/resource/extension/
git commit -m "feat: add ResourceContext and ResourceHeaderProvider for custom resource headers (#331)"
```

---

### Task 2: ResourceHeaderRegistry with best-prefix matching

**Files:**
- Create: `ui/src/main/java/org/jboss/hal/ui/resource/extension/ResourceHeaderRegistry.java`
- Create: `ui/src/test/java/org/jboss/hal/ui/resource/extension/ResourceHeaderRegistryTest.java`

**Interfaces:**
- Consumes: `ResourceHeaderProvider` (from Task 1), `org.jboss.hal.meta.AddressTemplate`, `org.jboss.hal.meta.Segment`, `org.jboss.hal.env.Environment`
- Produces: `ResourceHeaderRegistry` with `lookup(Environment, AddressTemplate)` returning `Optional<ResourceHeaderProvider>`

- [ ] **Step 1: Write the test class**

The test uses stub `ResourceHeaderProvider` implementations (no CDI needed). The registry is constructed manually with a list of providers. Test structure mirrors `RouteRegistryTest` but adds `appliesTo` filtering.

```java
/*
 *  Copyright 2024 Red Hat
 *  ... (full Apache 2.0 header)
 */
package org.jboss.hal.ui.resource.extension;

import java.util.List;
import java.util.Optional;

import org.jboss.elemento.IsElement;
import org.jboss.hal.env.Environment;
import org.jboss.hal.env.Version;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.ui.resource.ResourceHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import elemental2.dom.HTMLElement;

import static org.jboss.hal.env.OperationMode.STANDALONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceHeaderRegistryTest {

    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = new Environment();
        environment.update("test", "test", "test",
                Version.EMPTY_VERSION, Version.EMPTY_VERSION, STANDALONE);
    }

    // ------------------------------------------------------ no match

    @Test
    void noMatchWhenEmpty() {
        ResourceHeaderRegistry registry = registry();
        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    @Test
    void noMatchWhenKeysDiffer() {
        ResourceHeaderRegistry registry = registry(
                provider("interface=*"));
        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ exact match

    @Test
    void exactMatchSingleSegment() {
        ResourceHeaderProvider p = provider("interface=*");
        ResourceHeaderRegistry registry = registry(p);
        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("interface=public"));
        assertTrue(result.isPresent());
        assertEquals(p, result.get());
    }

    @Test
    void exactMatchMultipleSegments() {
        ResourceHeaderProvider p = provider("subsystem=datasources/data-source=*");
        ResourceHeaderRegistry registry = registry(p);
        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals(p, result.get());
    }

    // ------------------------------------------------------ prefix match

    @Test
    void prefixMatchSelectsLongest() {
        ResourceHeaderProvider shallow = provider("subsystem=*");
        ResourceHeaderProvider mid = provider("subsystem=datasources");
        ResourceHeaderProvider deep = provider("subsystem=datasources/data-source=*");
        ResourceHeaderRegistry registry = registry(shallow, mid, deep);

        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources/data-source=ExampleDS"));
        assertTrue(result.isPresent());
        assertEquals(deep, result.get());
    }

    // ------------------------------------------------------ exact value beats wildcard

    @Test
    void exactValueBeatsWildcardAtSameDepth() {
        ResourceHeaderProvider wildcard = provider("subsystem=*");
        ResourceHeaderProvider exact = provider("subsystem=logging");
        ResourceHeaderRegistry registry = registry(wildcard, exact);

        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=logging/logger=com.example"));
        assertTrue(result.isPresent());
        assertEquals(exact, result.get());
    }

    // ------------------------------------------------------ pattern longer than input

    @Test
    void patternLongerThanInputDoesNotMatch() {
        ResourceHeaderRegistry registry = registry(
                provider("subsystem=datasources/data-source=*"));
        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=datasources"));
        assertFalse(result.isPresent());
    }

    // ------------------------------------------------------ appliesTo filtering

    @Test
    void appliesToFilterExcludesProvider() {
        ResourceHeaderProvider never = provider("subsystem=*",
                (env, tmpl) -> false);
        ResourceHeaderRegistry registry = registry(never);

        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=logging"));
        assertFalse(result.isPresent());
    }

    @Test
    void appliesToFilterFallsBackToNextMatch() {
        ResourceHeaderProvider filtered = provider("subsystem=logging",
                (env, tmpl) -> false);
        ResourceHeaderProvider fallback = provider("subsystem=*");
        ResourceHeaderRegistry registry = registry(filtered, fallback);

        Optional<ResourceHeaderProvider> result = registry.lookup(environment,
                AddressTemplate.ofTrusted("subsystem=logging"));
        assertTrue(result.isPresent());
        assertEquals(fallback, result.get());
    }

    @Test
    void appliesToReceivesConcreteTemplate() {
        AddressTemplate captured[] = new AddressTemplate[1];
        ResourceHeaderProvider p = provider("subsystem=*",
                (env, tmpl) -> { captured[0] = tmpl; return true; });
        ResourceHeaderRegistry registry = registry(p);

        AddressTemplate input = AddressTemplate.ofTrusted("subsystem=logging");
        registry.lookup(environment, input);
        assertEquals(input, captured[0]);
    }

    // ------------------------------------------------------ helpers

    private ResourceHeaderRegistry registry(ResourceHeaderProvider... providers) {
        return new ResourceHeaderRegistry(List.of(providers));
    }

    @FunctionalInterface
    interface AppliesToFn {
        boolean test(Environment env, AddressTemplate template);
    }

    private ResourceHeaderProvider provider(String scope) {
        return provider(scope, (env, tmpl) -> true);
    }

    private ResourceHeaderProvider provider(String scope, AppliesToFn appliesTo) {
        AddressTemplate scopeTemplate = AddressTemplate.ofTrusted(scope);
        return new ResourceHeaderProvider() {
            @Override
            public AddressTemplate scope() {
                return scopeTemplate;
            }

            @Override
            public boolean appliesTo(Environment environment, AddressTemplate template) {
                return appliesTo.test(environment, template);
            }

            @Override
            public IsElement<HTMLElement> createHeader(ResourceContext context,
                    ResourceHeader defaultHeader) {
                return null; // not tested here
            }
        };
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn test -pl ui -Dtest=ResourceHeaderRegistryTest -P quick-build`
Expected: Compilation failure — `ResourceHeaderRegistry` does not exist yet

- [ ] **Step 3: Create ResourceHeaderRegistry.java**

The registry has two constructors: one for CDI (`Instance<ResourceHeaderProvider>`) and one for testing (`List<ResourceHeaderProvider>`). The matching algorithm is duplicated from `RouteRegistry.match()`.

```java
/*
 *  Copyright 2024 Red Hat
 *  ... (full Apache 2.0 header)
 */
package org.jboss.hal.ui.resource.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.hal.env.Environment;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.Segment;

/**
 * CDI-managed registry that collects all {@link ResourceHeaderProvider} implementations at startup and provides
 * best-prefix template matching at lookup time.
 * <p>
 * The matching algorithm compares the provider's {@link ResourceHeaderProvider#scope()} segments against the input
 * template segments left-to-right. When multiple providers match, the one with the longest matching prefix wins;
 * ties are broken by the number of exact (non-wildcard) value matches. Only providers where
 * {@link ResourceHeaderProvider#appliesTo(Environment, AddressTemplate)} returns {@code true} are considered.
 */
@Startup
@ApplicationScoped
public class ResourceHeaderRegistry {

    private final List<ResourceHeaderProvider> providers;

    @Inject
    public ResourceHeaderRegistry(Instance<ResourceHeaderProvider> providers) {
        this.providers = new ArrayList<>();
        for (ResourceHeaderProvider provider : providers) {
            this.providers.add(provider);
        }
    }

    /** Constructor for testing without CDI. */
    ResourceHeaderRegistry(List<ResourceHeaderProvider> providers) {
        this.providers = new ArrayList<>(providers);
    }

    /**
     * Finds the best matching provider for the given environment and template. Returns {@link Optional#empty()} if no
     * provider matches.
     */
    public Optional<ResourceHeaderProvider> lookup(Environment environment, AddressTemplate template) {
        ResourceHeaderProvider best = null;
        int bestSegments = -1;
        int bestExactValues = -1;

        for (ResourceHeaderProvider provider : providers) {
            if (!provider.appliesTo(environment, template)) {
                continue;
            }
            Match match = match(provider.scope(), template);
            if (match.matches) {
                if (match.segments > bestSegments ||
                        (match.segments == bestSegments && match.exactValues > bestExactValues)) {
                    best = provider;
                    bestSegments = match.segments;
                    bestExactValues = match.exactValues;
                }
            }
        }
        return Optional.ofNullable(best);
    }

    // ------------------------------------------------------ internal

    private Match match(AddressTemplate pattern, AddressTemplate input) {
        List<Segment> patternSegments = pattern.segments();
        List<Segment> inputSegments = input.segments();
        if (patternSegments.isEmpty() || patternSegments.size() > inputSegments.size()) {
            return Match.NO_MATCH;
        }

        int exactValues = 0;
        for (int i = 0; i < patternSegments.size(); i++) {
            Segment ps = patternSegments.get(i);
            Segment is = inputSegments.get(i);
            if (!ps.key.equals(is.key)) {
                return Match.NO_MATCH;
            }
            if ("*".equals(ps.value)) {
                continue;
            }
            if (!ps.value.equals(is.value)) {
                return Match.NO_MATCH;
            }
            exactValues++;
        }
        return new Match(true, patternSegments.size(), exactValues);
    }

    private record Match(boolean matches, int segments, int exactValues) {

        static final Match NO_MATCH = new Match(false, 0, 0);
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn test -pl ui -Dtest=ResourceHeaderRegistryTest`
Expected: All tests PASS

- [ ] **Step 5: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/resource/extension/ResourceHeaderRegistry.java
git add ui/src/test/java/org/jboss/hal/ui/resource/extension/ResourceHeaderRegistryTest.java
git commit -m "feat: add ResourceHeaderRegistry with best-prefix matching and appliesTo filtering (#331)"
```

---

### Task 3: Wire ResourceHeaderRegistry into UIContext

**Files:**
- Modify: `ui/src/main/java/org/jboss/hal/ui/UIContext.java`

**Interfaces:**
- Consumes: `ResourceHeaderRegistry` (from Task 2)
- Produces: `UIContext.resourceHeaderRegistry()` returning `ResourceHeaderRegistry`

- [ ] **Step 1: Add the field, constructor parameter, and getter to UIContext**

Add to the instance fields (after `routeRegistry`):

```java
private final ResourceHeaderRegistry resourceHeaderRegistry;
```

Add to the `@Inject` constructor parameter list (after `routeRegistry`):

```java
ResourceHeaderRegistry resourceHeaderRegistry,
```

Add the assignment in the constructor body (after `this.routeRegistry = routeRegistry;`):

```java
this.resourceHeaderRegistry = resourceHeaderRegistry;
```

Add the getter method (after the `routeRegistry()` method):

```java
/** Returns the resource header registry for looking up custom resource header providers. */
public ResourceHeaderRegistry resourceHeaderRegistry() {
    return resourceHeaderRegistry;
}
```

Add the import:

```java
import org.jboss.hal.ui.resource.extension.ResourceHeaderRegistry;
```

- [ ] **Step 2: Verify compilation**

Run: `mvn compile -pl ui -am -P quick-build`
Expected: BUILD SUCCESS

- [ ] **Step 3: Run all existing tests to verify no regressions**

Run: `mvn test -pl ui`
Expected: All tests PASS (the CDI container will inject the registry automatically since it's `@ApplicationScoped`)

- [ ] **Step 4: Commit**

```bash
git add ui/src/main/java/org/jboss/hal/ui/UIContext.java
git commit -m "feat: wire ResourceHeaderRegistry into UIContext (#331)"
```
