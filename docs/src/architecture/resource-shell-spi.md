# Resource Shell & SPI

## Purpose

The `ResourceShell` provides a composable framework for CRUD operations on WildFly management resources. The model browser is the primary consumer. While the generic UX works for most resources, some benefit from specialized UI. The SPI allows external modules to contribute custom behavior without modifying the shell.

## Design

### Shell Composition

The `ResourceShell` is a pure layout container—all intelligence lives in composed children. The shell itself has no behavior and no data loading. Its composition follows this structure:

```
ResourceShell
├── Sticky header group
│   ├── ResourceBreadcrumb (optional)
│   └── ResourceHeader (optional)
└── Content section
    ├── ResourceTabs (option A)
    │   ├── Data tab → ResourceData → Pipeline → ResourceView / ResourceForm
    │   ├── Attributes tab → AttributesTable
    │   ├── Operations tab → OperationsTable
    │   └── Capabilities tab → CapabilitiesTable
    └── ResourceList (option B)
        └── DataList of child resources
```

The sticky header group remains visible while scrolling through content, providing consistent context for the current resource. The content section flexibly switches between two layouts:

- **ResourceTabs** — for viewing and editing single resources with rich metadata tabs
- **ResourceList** — for browsing child resources in a data list

### Design Decisions

1. **Separate registries per concern** — Each SPI contract has its own registry, allowing independent implementation and discovery of providers.

2. **Registration key** — Providers register against `Environment` + `AddressTemplate` (supports wildcards), enabling resources to be identified across environments and address patterns.

3. **CDI-based discovery** — Providers are CDI beans discovered at startup. External modules contribute via classpath, making the SPI discoverable without explicit registration.

### SPI Package

All SPI contracts live in `org.jboss.hal.ui.resource.spi`. This package is the public interface for resource customization.

## Current State & Open Work

| SPI Contract | Issue | Status |
|---|---|---|
| Custom tabs | [#329](https://github.com/hal/foundation/issues/329) | Done |
| Custom resource header | [#331](https://github.com/hal/foundation/issues/331) | Done |
| Custom add-resource dialog | [#330](https://github.com/hal/foundation/issues/330) | Open |
| Custom data view / form | [#332](https://github.com/hal/foundation/issues/332) | Open |
| Custom resource list rendering | [#333](https://github.com/hal/foundation/issues/333) | Open |
| Custom delete behavior | [#334](https://github.com/hal/foundation/issues/334) | Open |
| Custom breadcrumb | [#335](https://github.com/hal/foundation/issues/335) | Open |

Two SPI contracts are fully implemented and in use:

- **Custom tabs** — External modules can contribute additional tabs to the resource view
- **Custom resource header** — External modules can customize the header display

Five SPI contracts remain open and represent future extensibility points. These will be implemented as the need for additional customization arises.
