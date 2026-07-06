# Attribute Group Layout for Resource Views and Forms

**Date:** 2026-07-06
**Status:** Approved

## Problem

Many WildFly management model resources define attribute groups — logical groupings of related attributes (e.g., `journal`, `security`, `cluster` on `messaging-activemq/server=*`). HAL currently displays all attributes in a flat list, ignoring group metadata. Resources with many attributes (some have 50+) become hard to navigate.

## Design Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Ungrouped vs grouped ordering | Ungrouped first, named groups a-z | Preserves existing experience for common attributes; specialized groups appear below |
| Collapsibility | Collapsible, expanded by default | Handles both simple (2-attribute) and complex (30-attribute) groups |
| View mode grouping | ExpandableSection per group | Consistent expand/collapse UX across view and form modes |
| Group title format | Humanized kebab-case | `inbound-config` → "Inbound Config"; consistent with HAL's attribute name display |
| User control | Toolbar toggle button | User decides flat vs grouped; only visible when resource has attribute groups |
| Default layout | Flat | Matches current behavior; no surprise for existing users |
| State persistence | Survives view/edit transitions | Grouped flag lives on ResourceData alongside filter state |

## Architecture

### Strategy-Based Rendering (Approach C)

`ResourceView` and `ResourceForm` remain unchanged structurally. The factory methods that populate them decide whether to render flat or grouped based on a boolean flag. This keeps the public API stable and provides a graceful fallback for resources without attribute groups.

### Data Layer

**`ResourceAttribute.grouped()`** — existing method, fix ordering: ungrouped attributes first, then named groups sorted a-z. Current implementation puts ungrouped last; reverse to `LinkedHashMap` that inserts ungrouped first, then appends sorted `TreeMap` entries.

**`ResourceAttribute.hasGroups()`** — new static method. Returns `true` if any attribute in the list has a non-null `group` value. Used by the toolbar to conditionally show the toggle.

**Group name humanization** — utility method: split on `-`, capitalize each word. Example: `scale-down` → "Scale Down". Implemented as a private helper where group titles are rendered.

### Form (Edit Mode)

When grouped mode is active, the factory builds:

1. **Ungrouped attributes** — added directly to the `Form` as `FormGroup` items (identical to current flat layout)
2. **Each named group** — wrapped in a `FormFieldGroup`:
   - `FormFieldGroupHeader` with humanized group name
   - `FormFieldGroupBody` containing the group's `FormGroup` items
   - Expandable, expanded by default

When grouped mode is off (or no groups exist), the form renders flat — identical to today.

`FormItem` instances, validation, `modelNode()`, and `attributeOperations()` work unchanged — they iterate the `items` map regardless of visual grouping.

### View (Read-Only Mode)

When grouped mode is active, the factory builds:

1. **Ungrouped attributes** — rendered in a plain `DescriptionList` (identical to current flat layout)
2. **Each named group** — wrapped in an `ExpandableSection`:
   - Humanized group name as the toggle title
   - Separate `DescriptionList` inside containing the group's `ViewItem`s
   - Expanded by default

When grouped mode is off (or no groups exist), the view renders as a single flat `DescriptionList` — identical to today.

### Filtering with Groups

Filtering works at two levels:

- **Item level** — same as today, individual items that don't match the filter get hidden
- **Group level** — when all items within a named group are filtered out, the group container (`FormFieldGroup` or `ExpandableSection`) hides too. When at least one item matches, the group stays visible.

**Collapse vs filter independence**: a collapsed group's items are still considered "visible" by the filter. Collapse is a user layout preference; filter is data selection. These are independent concerns.

### Toolbar Toggle

A toggle button is added to `ResourceDataToolbar`'s action group (alongside refresh/reset/edit).

- **Visibility**: only shown when `ResourceAttribute.hasGroups()` returns `true` for the current resource
- **State**: the `grouped` boolean lives on `ResourceData`, alongside the existing filter state. Survives view/edit transitions — if the user enables grouped mode in the form and switches to view, the view renders grouped too
- **Default**: flat (existing behavior) when a resource is first loaded
- **Action**: clicking the toggle updates the flag on `ResourceData` and triggers a re-render with the new layout strategy

### Graceful Fallback

Resources without attribute groups behave identically to today: no toggle shown, flat layout, zero visual difference. No code path changes for these resources.

## Change Map

| File | Change |
|---|---|
| `ResourceAttribute` | Fix `grouped()` ordering (ungrouped first), add `hasGroups()` |
| `ResourceData` | Add `grouped` boolean field, pass through to rendering |
| `ResourceDataToolbar` | Add toggle button, visible only when groups exist |
| `ResourceView` factory | Support grouped rendering with `ExpandableSection` per group |
| `ResourceForm` factory | Support grouped rendering with `FormFieldGroup` per group |
| Filter callback | Add group-container visibility logic (hide empty groups) |

## What Doesn't Change

- `ResourceView` and `ResourceForm` class structure
- `FormItem` and `ViewItem` classes
- Validation logic
- `modelNode()` and `attributeOperations()` methods
- The filter system itself
- Any resource without attribute groups

## WildFly Model Context

Attribute groups are used across several subsystems. Notable examples:

- **`messaging-activemq/server=*`** — 10+ groups: journal (30+ attrs), security, cluster, debug, management, statistics, transaction, etc.
- **`iiop-openjdk`** — 9 groups: orb, security, naming, tcp, transport-config, etc.
- **`elytron/*`** — class-loading, file, implementation, search groups across many resources
- **`jaxrs`** — resteasy, tracing groups
- **`opentelemetry`** — exporter, sampler, span-processor groups
- **`batch-jberet`** — single environment group (2 attributes)

Resources without any attribute groups (the majority) are unaffected by this feature.
