# Extension Point: Custom Resource Header (#331)

Parent issue: [#320 — Resource shell extension points](https://github.com/hal/foundation/issues/320)

## Summary

Allow resources to replace or augment the default `ResourceHeader` (title + stability label + description) with resource-specific content. A registry keyed by `AddressTemplate` with `Environment`-based activation enables custom headers for specific resources without modifying `ResourceShell` or its consumers.

## Motivation

The generic `ResourceHeader` works well for most resources but some benefit from richer headers:

- **Status indicators** — datasource connection state, deployment enabled/disabled, server running/stopped
- **Action buttons** — "Test Connection", "Enable/Disable", "Restart"
- **Additional context** — JNDI name for datasources, bound address for socket bindings

## Package

All new types go in `org.jboss.hal.ui.resource.extension` (new sub-package of `org.jboss.hal.ui.resource` in the `ui` module). This package will also host registries for future extension points (#329–#335).

## API

### ResourceContext

A record bundling the data a custom header (or other extension point) needs:

```java
package org.jboss.hal.ui.resource.extension;

public record ResourceContext(AddressTemplate template, Metadata metadata, ModelNode modelNode) {}
```

- `template` — the concrete resource template being rendered (e.g., `/subsystem=datasources/data-source=ExampleDS`)
- `metadata` — the resource's management model metadata
- `modelNode` — the resource's current attribute values (loaded via `read-resource`)

`ResourceContext` is designed to be reused by other extension points (tabs, data view, add dialog, etc.).

### ResourceHeaderProvider

The provider interface. Each implementation is a self-contained CDI bean for one address template pattern:

```java
package org.jboss.hal.ui.resource.extension;

public interface ResourceHeaderProvider {

    /**
     * The address template pattern this provider applies to.
     * Supports wildcards (e.g., {@code /subsystem=datasources/data-source=*}).
     * Matched via best-prefix against the concrete template being rendered.
     */
    AddressTemplate scope();

    /**
     * Whether this provider applies for the given environment and concrete resource template.
     * Called at lookup time when the environment is fully populated.
     * Defaults to {@code true}.
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
     * @param defaultHeader the default {@link ResourceHeader}, fully configured but lazily built.
     *                      The provider can use it for augmentation (wrap with extra content) or
     *                      ignore it for full replacement (no build cost if unused).
     */
    IsElement<HTMLElement> createHeader(ResourceContext context, ResourceHeader defaultHeader);
}
```

The provider serves three roles in one interface:
- `scope()` — declares where it applies (registration)
- `appliesTo()` — declares when it applies (activation)
- `createHeader()` — produces the header (factory)

### ResourceHeaderRegistry

CDI-managed registry that collects all `ResourceHeaderProvider` implementations at startup and provides best-prefix template matching at lookup time:

```java
package org.jboss.hal.ui.resource.extension;

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

    /**
     * Finds the best matching provider for the given environment and template.
     * Uses best-prefix matching on the provider's {@link ResourceHeaderProvider#scope()},
     * filtered by {@link ResourceHeaderProvider#appliesTo(Environment, AddressTemplate)}.
     * When multiple providers match, the one with the longest prefix wins;
     * ties are broken by exact (non-wildcard) value matches.
     */
    public Optional<ResourceHeaderProvider> lookup(Environment environment, AddressTemplate template) {
        // best-prefix matching algorithm (same as RouteRegistry.match())
    }
}
```

### Best-Prefix Matching Algorithm

The matching algorithm is the same as `RouteRegistry.match()`:

1. Compare the provider's `scope()` segments against the input template segments left-to-right.
2. Keys must match exactly.
3. If the scope segment's value is `*`, any input value matches (wildcard).
4. If the scope value is not `*`, values must match exactly (counted as `exactValues`).
5. The scope can be shorter than the input (prefix matching).
6. Best match = most matched segments; ties broken by most exact value matches.
7. Only providers where `appliesTo(environment, template)` returns `true` are considered.

The matching logic is duplicated from `RouteRegistry` into `ResourceHeaderRegistry` (private `match()` method and `Match` record). When the second extension registry is implemented, this should be extracted into a shared utility class (e.g., `TemplateMatcher` in the same package) and both `RouteRegistry` and the extension registries should delegate to it.

## UIContext Integration

`ResourceHeaderRegistry` is added to `UIContext` as a constructor-injected CDI dependency and exposed via a getter:

```java
public class UIContext {
    private final ResourceHeaderRegistry resourceHeaderRegistry;

    public ResourceHeaderRegistry resourceHeaderRegistry() {
        return resourceHeaderRegistry;
    }
}
```

## Integration Point (Deferred)

How `ResourceShell`, `ModelBrowserDetail`, or another component consumes the registry is **not part of this issue**. The integration point will be designed when the other extension points (#329–#335) are further along, so all extension points can be integrated consistently.

## Usage Example

A custom header for datasources that shows the JNDI name and a connection status badge:

```java
@ApplicationScoped
public class DataSourceHeaderProvider implements ResourceHeaderProvider {

    @Override
    public AddressTemplate scope() {
        return AddressTemplate.of("/subsystem=datasources/data-source=*");
    }

    @Override
    public boolean appliesTo(Environment environment, AddressTemplate template) {
        return environment.standalone();
    }

    @Override
    public IsElement<HTMLElement> createHeader(ResourceContext context, ResourceHeader defaultHeader) {
        String jndiName = context.modelNode().get("jndi-name").asString();
        boolean enabled = context.modelNode().get("enabled").asBoolean(false);

        return content()
                .add(flex().alignItems(center)
                        .addItem(flexItem().add(title(1, _3xl, context.template().last().value)))
                        .addItem(flexItem().add(label(enabled ? "Enabled" : "Disabled",
                                enabled ? "green" : "red"))))
                .add(p().text("JNDI: " + jndiName));
    }
}
```

This provider lives in `op/console` (where concrete WildFly resource knowledge resides), while the interfaces and registry live in `ui`.

## Augmentation Example

A provider that keeps the default header and adds extra content below it:

```java
@Override
public IsElement<HTMLElement> createHeader(ResourceContext context, ResourceHeader defaultHeader) {
    return div()
            .add(defaultHeader)
            .add(toolbar()
                    .addItem(toolbarItem()
                            .add(button("Test Connection").onClick(...))));
}
```

## File Summary

| File | Module | Type |
|---|---|---|
| `ResourceContext.java` | `ui` | New record |
| `ResourceHeaderProvider.java` | `ui` | New interface |
| `ResourceHeaderRegistry.java` | `ui` | New CDI bean |
| `UIContext.java` | `ui` | Modified (add registry) |
