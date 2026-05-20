# OUIA Support for halOP

## Overview

[OUIA](https://www.patternfly.org/developer-resources/open-ui-automation) (Open UI Automation) is a specification that standardizes HTML attributes on UI components for automated testing. It provides reliable, stable locators that testing tools (Playwright, Selenium, Cypress) can use instead of fragile CSS selectors or XPaths.

The halOP test suite **dave** (`/Users/hpehl/dev/hal/dave`) uses Playwright and leverages OUIA attributes to locate and interact with UI elements.

## OUIA Attributes

| Attribute | Required | Description |
|---|---|---|
| `data-ouia-component-type` | Yes | Component kind, e.g., `PF6/Component/Button` |
| `data-ouia-component-id` | No | Stable instance identifier for test selectors |
| `data-ouia-safe` | No | Signals the component is ready for interaction |

OUIA is **gated by localStorage**: attributes are only rendered when `localStorage.setItem("ouia", "true")` is set in the browser. Zero overhead in production.

## ID Naming Convention

- **Prefix**: `hal-op-`
- **Pattern**: `hal-op-<context>-<action>[-<qualifier>]`
- **Composition**: `Ids.ouia(String first, String... rest)` joins segments with `-` via Elemento's `Id.build()`

### Examples

| ID | Component |
|---|---|
| `hal-op-endpoint-connect-btn` | Connect button in endpoint modal |
| `hal-op-data-source-add-modal` | Add resource modal for data sources |
| `hal-op-model-browser-refresh-btn` | Refresh button in model browser toolbar |
| `hal-op-operation-read-resource-execute-btn` | Execute button for `read-resource` operation |

### Suffix Constants

Defined in `Ids.java` for composition:

```
_ADD, _BTN, _CANCEL, _CLOSE, _CONNECT, _DELETE, _EDIT,
_EXECUTE, _MODAL, _OK, _REFRESH, _RESET, _SAVE, _SEARCH
```

## Sharing IDs Between Java and TypeScript

IDs are defined in Java (`Ids.java`) and shared with the dave test suite via the `@halconsole/ouia` NPM package.

The generator script `op/ouia/generate.mjs` parses `Ids.java` and emits `op/ouia/src/ids.ts` containing:
- The `buildId()` function (port of Elemento's `Id.build()`)
- The `ouia()` composition function
- All static ID constants
- All dynamic ID builder functions

The NPM package is published as part of the release workflow.

## Implementation Status

### Phase 1 — Navigation & Page Structure (COMPLETE)

Added OUIA IDs to navigation, masthead, and page structure elements.

**Files modified:**
- `Ids.java` — added navigation and page section constants
- `NavigationProducer.java` — navigation items
- `Skeleton.java`, `ErrorSkeleton.java` — masthead, sidebar, main content sections
- `DashboardPage.java`, `DeploymentsPage.java`, `RuntimePage.java`, `ConfigurationPage.java`, `TasksPage.java`, `ModelBrowserPage.java` — page sections
- `NotFound.java`, `NoData.java` — error page sections

### Phase 2 — Buttons & Modals (COMPLETE)

Added OUIA IDs to buttons and modals — the primary interactive controls needed for dave to test CRUD operations and workflows.

**Approach:**
- **Static IDs** for `op/console` components (fixed, known contexts)
- **Context-based dynamic IDs** for reusable `ui` module components (context derived from `AddressTemplate.last().key`)

**op/console files modified:**
- `EndpointModal.java` — modal, connect/save, cancel buttons
- `EndpointForm.java` — ping button
- `EndpointTable.java` — add link button
- `EndpointSelector.java` — select another button
- `BootstrapErrorElement.java` — select management interface button
- `StabilityBanner.java` — got it / dismiss button
- `NewExpressionModal.java` — modal, ok, cancel buttons
- `NotificationElements.java` — mark all read, clear all, unclear last items
- `LogCard.java` — show log file, choose log file buttons
- `TasksPage.java` — per-task launch buttons (dynamic from `task.id()`)

**ui module files modified:**
- `ResourceDialogs.java` — add/execute/delete modals and their buttons; context derived from template
- `ResourceToolbar.java` — reset, refresh, edit, save, cancel buttons; `ouiaContext` parameter added
- `ResourceManager.java` — passes template-derived context to toolbar
- `ModelBrowserTree.java` — back, forward, home, refresh, find, collapse buttons
- `FindResource.java` — modal, search, cancel buttons
- `ResourceList.java` — add button with template-derived context
- `OperationsTable.java` — execute button with operation name in context
- `BuildingBlocks.java` — crudColumn add, refresh, delete buttons

**Supporting changes:**
- `Ids.java` — added `ouia()` composition method, suffix constants, ~30 static ID constants
- `generate.mjs` — fixed varargs handling for the `ouia()` method
- `pom.xml` — bumped PatternFly Java to 0.7.8 (OUIA sub-component support)

### OuiaSupport on Custom Components (COMPLETE)

Implemented the `OuiaSupport<E, B>` interface (introduced in PatternFly Java 0.8.0) on 10 custom HAL components. This adds `data-ouia-component-type` attributes to container elements, complementing the existing `data-ouia-component-id` attributes on child buttons/modals from phases 1 & 2.

**Naming convention:** `HalOP/<ComponentName>` (e.g., `HalOP/ModelBrowserTree`)

**Components:**

| Component | Type |
|---|---|
| `Skeleton` | `HalOP/Skeleton` |
| `ErrorSkeleton` | `HalOP/ErrorSkeleton` |
| `StabilityBanner` | `HalOP/StabilityBanner` |
| `EndpointTable` | `HalOP/EndpointTable` |
| `EndpointForm` | `HalOP/EndpointForm` |
| `EndpointSelector` | `HalOP/EndpointSelector` |
| `ModelBrowserTree` | `HalOP/ModelBrowserTree` |
| `ModelBrowserDetail` | `HalOP/ModelBrowserDetail` |
| `ResourceToolbar` | `HalOP/ResourceToolbar` |
| `ResourceList` | `HalOP/ResourceList` |

**Per-component change (~5 lines):**
- Implement `OuiaSupport<HTMLElement, ClassName>`
- Add `ouiaComponentType()` returning the type string
- Add `that()` returning `this`
- Call `initOuia()` at end of constructor

This enables dave test selectors to scope by container type:

```typescript
// Find the refresh button inside the model browser tree
const refreshBtn = page.locator(
  '[data-ouia-component-type="HalOP/ModelBrowserTree"] ' +
  `[data-ouia-component-id="${MODEL_BROWSER_REFRESH_BTN}"]`
);
```

### Phase 3 — Remaining Controls (PLANNED)

Deferred items for future implementation:

- Per-row action buttons (edit/remove in tables) — need row context
- Filter multi-selects (TypesMultiSelect, etc.)
- Text inputs
- Tabs
- ResourcesSection / ExpressionsSection small buttons
- DashboardCard per-card refresh buttons
- NotificationListener per-notification action dropdowns
- Labels, cards, alerts, and other content elements

## Verification

```bash
# Build
mvn compile -P op,quick-build -q

# Full build with tests
mvn verify -P op
```

Run in dev mode (`mvn j2cl:watch -P op` + `cd op/console && npm run watch`) and verify OUIA attributes appear in browser DevTools after `localStorage.setItem("ouia", "true")`.

## dave Test Selectors

With OUIA enabled, Playwright tests in dave can locate elements like this:

```typescript
import { ENDPOINT_CONNECT_BTN, ouia } from "@halconsole/ouia";

// Using a static ID constant
const connectBtn = page.locator(`[data-ouia-component-id="${ENDPOINT_CONNECT_BTN}"]`);

// Using the ouia() composition function for dynamic IDs
const addModal = page.locator(`[data-ouia-component-id="${ouia("data-source", "add", "modal")}"]`);

// By component type + specific ID
const saveButton = page.locator(
  '[data-ouia-component-type="PF6/Component/Button"]' +
  `[data-ouia-component-id="${ouia("data-source", "save", "btn")}"]`
);
```
