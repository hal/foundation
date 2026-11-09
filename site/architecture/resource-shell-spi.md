# Resource Shell & SPI

## What It Does

The resource shell is the composable framework behind CRUD operations on WildFly management resources. The model browser is its primary consumer — when you navigate to a resource and see its attributes, operations, and capabilities, that's the resource shell at work.

The generic UX handles most resources well, but some benefit from specialized UI. The SPI lets external modules contribute custom behavior — a custom tab, a custom header, a custom add-resource dialog — without modifying the shell itself.

## How It Works

### Shell Composition

The shell is a pure layout container. It has no behavior and loads no data — all intelligence lives in its composed children. The structure splits into a sticky header (always visible while scrolling) and a content area that switches between two layouts:

```
ResourceShell
├── Sticky header
│   ├── ResourceBreadcrumb (optional)
│   └── ResourceHeader (optional)
└── Content
    ├── Option A: ResourceTabs
    │   ├── Data tab
    │   ├── Attributes tab
    │   ├── Operations tab
    │   └── Capabilities tab
    └── Option B: ResourceList
        └── DataList of child resources
```

- **ResourceTabs** — for viewing and editing a single resource with rich metadata tabs
- **ResourceList** — for browsing child resources in a data list

### Design Decisions

Three key decisions shape the SPI:

1. **Separate registries per concern** — each SPI contract has its own registry, so providers for tabs, headers, and dialogs are discovered and managed independently.

2. **Registration key** — providers register against `Environment` + `AddressTemplate` (with wildcard support), so a provider can target a specific resource address pattern across environments.

3. **CDI-based discovery** — providers are CDI beans discovered at startup. External modules contribute by placing their implementations on the classpath — no explicit registration code needed.

### SPI Package

All SPI contracts live in `org.jboss.hal.ui.resource.spi`. This package is the public interface for resource customization.

## Current State & Open Work

| SPI Contract                   | Issue                                                | Status |
| ------------------------------ | ---------------------------------------------------- | ------ |
| Custom tabs                    | [#329](https://github.com/hal/foundation/issues/329) | Done   |
| Custom resource header         | [#331](https://github.com/hal/foundation/issues/331) | Done   |
| Custom add-resource dialog     | [#330](https://github.com/hal/foundation/issues/330) | Open   |
| Custom data view / form        | [#332](https://github.com/hal/foundation/issues/332) | Open   |
| Custom resource list rendering | [#333](https://github.com/hal/foundation/issues/333) | Open   |
| Custom delete behavior         | [#334](https://github.com/hal/foundation/issues/334) | Open   |
| Custom breadcrumb              | [#335](https://github.com/hal/foundation/issues/335) | Open   |

Custom tabs and custom resource headers are fully implemented and in use. The remaining five contracts are future extensibility points, to be implemented as the need for additional customization arises.

## Examples

Two SPI implementations in the codebase show the pattern in practice.

### Custom Resource Header: DataSourceHeader

The `DataSourceHeader` targets `subsystem=datasources/data-source=*` and `subsystem=datasources/xa-data-source=*`. It adds a database icon and a live "enabled"/"disabled" status label to the resource header. The label reads the datasource's runtime `enabled` attribute via DMR and updates automatically when the datasource is toggled, by listening for `ResourceEvents.Modified`.

### Custom Tab: CredentialStoreTabs

The `CredentialStoreTabs` targets `subsystem=elytron/credential-store=*` and `subsystem=elytron/secret-key-credential-store=*`. It adds an "Aliases" tab (at position 1, right after the Data tab) that provides a UI for viewing and managing credential store aliases.

### Common Pattern

Both implementations follow the same structure:

1. They are `@ApplicationScoped` CDI beans — discovered automatically at startup
2. They implement `scopes()` to declare which `AddressTemplate`s they target
3. They implement `appliesTo()` for additional filtering (both check `template.fullyQualified()`)
4. They provide a `create*` / `customize*` method that receives the default header or tabs and returns a customized version wrapped in a `Promise`

New SPI implementations follow this same pattern: declare the target resources, implement the customization method, and let CDI discovery handle the registration.