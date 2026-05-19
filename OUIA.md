# OUIA Support for halOP

## Overview

[OUIA](https://www.patternfly.org/developer-resources/open-ui-automation) (Open UI Automation) is a specification that standardizes HTML attributes on UI components for automated testing. It provides reliable, stable locators that testing tools (Playwright, Selenium, Cypress) can use instead of fragile CSS selectors or XPaths.

The halOP test suite **dave** (`/Users/hpehl/dev/hal/dave`) uses Playwright and should leverage OUIA attributes to locate and interact with UI elements.

## OUIA Attributes

| Attribute | Required | Description |
|---|---|---|
| `data-ouia-component-type` | Yes | Component kind, e.g., `PF6/Component/Button` |
| `data-ouia-component-id` | No | Stable instance identifier for test selectors |
| `data-ouia-safe` | No | Signals the component is ready for interaction |

OUIA is **gated by localStorage**: attributes are only rendered when `localStorage.setItem("ouia", "true")` is set in the browser. Zero overhead in production.

## Current State (as of 2026-05-19)

### What's Already Working

- **PatternFly Java v0.7.5** (used by halOP) has full OUIA support, fixed in the latest release.
- All 85+ PatternFly component types **automatically** set `data-ouia-component-type` and `data-ouia-safe="true"` in their constructors.
- Every PatternFly Java component exposes a `.ouiaId(String)` fluent method to set a stable `data-ouia-component-id`.

### What's Missing

**No `.ouiaId()` calls exist in the halOP codebase.** Without explicit component IDs, dave can find components by type (e.g., "all buttons") but cannot reliably target a *specific* button instance.

## Component Inventory

Approximate count of PatternFly component instantiations in halOP:

| Category | Count | Components |
|---|---|---|
| Interactive controls | ~132 | Buttons (82), selects (30), dropdowns (7), modals (7), menus (6) |
| Contextual elements | ~57 | Labels (33), cards (12), navigation items (6), alerts (6) |
| Supporting elements | ~43 | Tabs, drawers, radios, text inputs, toolbars |
| **Total** | **~232** | Across ~150 Java files (`op/console`: 72, `ui`: 78) |

## Implementation Plan

### Phase 1 — Navigation & Page Structure (~15 components)

Navigation items, page sections, masthead elements. Unblocks dave for basic app navigation tests.

### Phase 2 — Interactive Controls (~130 components)

Buttons, dropdowns, selects, modals. Enables testing CRUD operations and workflows.

### Phase 3 — Content Elements (~90 components)

Labels, cards, tables, alerts. Enables asserting on displayed data.

### What a Change Looks Like

Each change is a one-liner — adding `.ouiaId("stable-id")` to a component:

```java
// Before
button("Save").primary().onClick(this::save)

// After
button("Save").primary().ouiaId("resource-save").onClick(this::save)
```

### Where to Define IDs

Static OUIA IDs should be defined as constants in `resources/src/main/java/org/jboss/hal/resources/Ids.java`. This interface is the centralized location for QA-reusable IDs (see its Javadoc).

For dynamic IDs (containing resource names, server names, etc.), use `Ids` helper methods that generate deterministic composite IDs.

## Sharing IDs Between Java and TypeScript

halOP defines OUIA IDs in Java (`Ids.java`). The test suite dave is a TypeScript/Playwright project. The IDs must stay in sync. Options:

### Option A: Code Generation (Recommended)

Use a Maven plugin or build script to generate a TypeScript constants file from `Ids.java` at build time.

- **Pros**: Single source of truth, compile-time safety, automated sync
- **Cons**: Requires build tooling setup

### Option B: Shared JSON/YAML File

Define IDs in a shared data file (e.g., `ouia-ids.json`), read by both Java and TypeScript.

- **Pros**: Language-neutral, simple to maintain
- **Cons**: Java side needs to read from file or generate code from it; less idiomatic

### Option C: Manual Sync with Naming Convention

Maintain parallel constants in both codebases with a strict naming convention and CI validation.

- **Pros**: No build tooling needed, simple to start
- **Cons**: Drift risk, manual effort, harder to scale

### Option D: TypeScript Generation from Annotation Processing

Use a Java annotation processor to scan `Ids.java` and emit a `.ts` file during compilation.

- **Pros**: Integrated into Maven build, type-safe
- **Cons**: More complex setup, annotation processor maintenance

## dave Test Selectors

With OUIA enabled, Playwright tests in dave can locate elements like this:

```typescript
// By component type only
const buttons = page.locator('[data-ouia-component-type="PF6/Component/Button"]');

// By component type + specific ID
const saveButton = page.locator('[data-ouia-component-type="PF6/Component/Button"][data-ouia-component-id="resource-save"]');

// By ID only (when unique)
const nav = page.locator('[data-ouia-component-id="main-navigation"]');
```

For dave, consider creating a helper/page-object layer that wraps these selectors using shared ID constants.

## Effort Summary

- **Difficulty**: Low — each change is a one-liner
- **Scale**: Medium — ~232 instances across ~150 files, mostly mechanical
- **Risk**: Low — purely additive, no behavior change
- **Estimated time**: 2-3 days for full coverage, a few hours for Phase 1
