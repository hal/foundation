# Map Attribute Support — Design Spec

**Date:** 2026-07-14  
**Status:** Approved  
**Scope:** Pipeline support for free-form key-value map attributes (222 occurrences in WildFly 40)

## Problem

The HAL resource pipeline covers 89% of WildFly management model attributes. The largest uncovered category (222 attributes, 11% gap) is free-form key-value maps — OBJECT attributes with a simple VALUE_TYPE (typically STRING) representing arbitrary `{String → String}` maps. These include `properties`, `*-properties`, `params`, `configuration`, `meta-data`, and ~30 other attribute names across datasources, messaging, JGroups, Elytron, logging, and other subsystems.

Currently these render as raw JSON in both view and form, providing no structured editing experience.

## Detection

### How map attributes differ from structured OBJECTs

In the raw DMR description:

- **Structured OBJECTs** (credential-reference, file, time-unit, etc.): `VALUE_TYPE` is a `ModelNode` of type `ModelType.OBJECT` containing named sub-attributes with their own type definitions. `get(VALUE_TYPE).getType() == ModelType.OBJECT`.
- **Map attributes** (properties, params, etc.): `VALUE_TYPE` is a `ModelNode` of type `ModelType.TYPE` holding a simple type reference like `STRING`. `get(VALUE_TYPE).getType() == ModelType.TYPE` and `get(VALUE_TYPE).asType().simple()` returns true.

### MapMatcher predicate

```
type == OBJECT
  AND VALUE_TYPE is defined
  AND get(VALUE_TYPE).getType() == ModelType.TYPE
  AND get(VALUE_TYPE).asType().simple()
```

This is the complement of `hasObjectValueType()`. A new static helper `hasSimpleValueType()` will be added to `AttributeMatcher` for symmetry.

The matcher uses `partition()` to claim matching attributes as single-attribute groups.

### Why this is safe

- All composite matchers (CredentialReference, TimeUnit, File, PathRelativeTo) run first and claim their known structures before `MapMatcher` sees the pool.
- `FlatteningProvider` only handles `simpleRecord()` OBJECTs, which require `get(VALUE_TYPE).getType() == OBJECT` — mutually exclusive with our predicate.
- Attributes with undefined `VALUE_TYPE` are excluded (they fall through to `DefaultItemProvider` as JSON — safe default).
- Verified via model graph: the only structured OBJECT containing a map-like sub-attribute is `jwt.key-map` on `token-realm=*`, which is in the "Complex/recursive OBJECTs" category and not flattened.

### Model characteristics

- 222 instances across 37 distinct attribute names
- 189 read-write, 33 read-only (mostly `deployment=*` runtime resources)
- All have `map-put` / `map-get` / `map-remove` / `map-clear` global operations available
- Most are nillable (223 of 227 OBJECT-without-CONSISTS_OF; the 4 `credential-reference` on deployment resources are false positives excluded by our tighter predicate)

## View — MapViewItem

Extends `AbstractViewItem`. Renders inside the existing outer description list (one `DescriptionListGroup` per attribute, like all view items).

### Defined value rendering

The map entries are displayed as a PatternFly **Label Group** with compact labels:

- Each entry from the attribute's `ModelNode` value (iterated as property list) becomes a `Label` with text `key=value`
- Labels are compact, grey/neutral colored, read-only (no `onClose`)
- `LabelGroup` uses `numLabels` to collapse when there are many entries (e.g., show 5, then "+N more")

### Other states

Handled by `AbstractViewItem`:

- Undefined/empty → "undefined" text
- Not readable → "restricted" with lock icon
- Expression → expression rendering with resolve button

## Form — MapFormItem

### Input area

A PatternFly **Text Input Group** where the user types `key=value` and presses Enter to add an entry.

### Parsing rules

- Split on the **first** `=` only — key is everything before, value is everything after (supports `=` in values like URLs, JDBC strings)
- Key must be non-empty (at least 1 character)
- Value must be non-empty (WildFly `map-put` requires `minLength: 1` for the value parameter)
- No `=` present → validation error ("Use key=value format")
- `key=` with nothing after `=` → rejected
- Duplicate key → rejected

### Display area

A **Label Group** below/inline showing current entries as compact `key=value` labels with `onClose` handlers for removal. To edit an existing entry, the user removes it and re-adds with the new value.

### Change tracking

The form item tracks three sets of modifications against the original map value:

- **Added entries** — new keys not in the original map
- **Modified entries** — existing keys whose values changed
- **Removed entries** — keys present in the original but deleted by the user

### Save strategy

**Primary:** Use granular DMR operations for each tracked change:

- `map-put(name=<attr>, key=<k>, value=<v>)` for each added or modified entry
- `map-remove(name=<attr>, key=<k>)` for each removed entry

This avoids overwriting concurrent changes and matches how the WildFly CLI operates.

**Fallback:** If change tracking proves infeasible, use `write-attribute` with the complete map value as a single operation.

### Read-only mode

When the attribute is read-only, the form item renders like the view — Label Group with no input area and no close buttons on labels.

## Pipeline Integration

### Registration order

**Matchers** (priority order):

1. `CredentialReferenceMatcher`
2. `TimeUnitMatcher`
3. `FileMatcher`
4. `PathRelativeToMatcher`
5. **`MapMatcher`** ← new

**Providers** (registration order):

1. `CredentialReferenceProvider`
2. `TimeUnitProvider`
3. `FileProvider`
4. `PathRelativeToProvider`
5. `RelativeToProvider`
6. **`MapProvider`** ← new
7. `FlatteningProvider`
8. `DefaultItemProvider`

### MapProvider

Implements `ItemProvider`:

- `matches(group)` — same predicate as `MapMatcher` (simple VALUE_TYPE check)
- `viewItems(group, context)` — resolves attribute via `ResolvedAttribute.resolve()`, returns single `MapViewItem`
- `formItems(group, context)` — resolves attribute, returns single `MapFormItem`

## New and Modified Files

### New files

| File | Package | Purpose |
|------|---------|---------|
| `MapMatcher.java` | `pipeline` | Claims OBJECT attributes with simple VALUE_TYPE |
| `MapProvider.java` | `pipeline` | Creates MapViewItem / MapFormItem |
| `MapViewItem.java` | `view` | Label Group rendering of key=value entries |
| `MapFormItem.java` | `form` | Text Input Group + Label Group editing with change tracking |

### Modified files

| File | Change |
|------|--------|
| `AttributeMatcher.java` | Add `hasSimpleValueType()` static helper |
| `Pipeline.java` | Register `MapMatcher` and `MapProvider` |
| `COVERAGE.md` | Update coverage numbers |

## Coverage Impact

| Metric | Before | After |
|--------|--------|-------|
| Covered attributes | 5,165 (89%) | ~5,387 (93%) |
| Not covered | 638 (11%) | ~416 (7%) |
| Remaining uncovered | LIST of OBJECT (52), Complex OBJECTs (13), other edge cases |
