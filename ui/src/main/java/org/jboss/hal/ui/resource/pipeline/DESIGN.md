# Resource Item Pipeline

Design document for the attribute-to-item pipeline that transforms resource metadata into view and form items.

## Naming

Working name: **pipeline**. Alternatives considered:

| Name | Entry point | Verdict |
|---|---|---|
| `pipeline` | `Pipeline.viewItems(template, metadata)` | Familiar, implies ordered transformation. Generic. |
| `resolution` | `ResourceResolver.viewItems(template, metadata)` | Captures the "figuring out" aspect. Could be confused with dependency resolution. |
| `composition` | `ItemComposer.viewItems(template, metadata)` | Building items from parts. Could be confused with OOP composition. |

## Overview

**Input:** `PipelineContext` bundles all pipeline inputs:

- `AddressTemplate` — the resource address template, needed for DMR operations in form items
- `Metadata` — contains `ResourceDescription` (with `AttributeDescriptions`, operations, capabilities) and `SecurityContext` (RBAC)
- `ModelNode resource` — the current attribute values from a `read-resource` operation
- `PipelineFlags` — scope (new/existing resource) and placeholder behavior

No upfront filtering of attributes; every attribute from `metadata.resourceDescription().attributes()` enters the pipeline.

**Output:** An ordered list of `PipelineViewItem` or `PipelineFormItem`. Each form item knows how to produce 1..n DMR operations via `operations(ResourceAddress)`. These operations are flat-mapped into a single composite operation for the resource write.

**Shape:** One pipeline with two entry points:

```
Pipeline pipeline = Pipeline.create();
pipeline.viewItems(context) → List<PipelineViewItem>
pipeline.formItems(context) → List<PipelineFormItem>
```

Stage 1 (grouping) is identical for both. Stage 2 (itemization) calls `viewItems()` or `formItems()` on the matched provider.

## Stage 1: Group

**Responsibility:** Decide which attributes belong together.

**Input:** `AttributeDescriptions` extracted from `context.resourceDescription().attributes()`. The `AttributeDescriptions` class is iterable and provides `stream()` and `get(name)`.

**Output:** `List<AttributeGroup>` — an ordered list of groups. Each group holds 1..n `AttributeDescription`s. Every input attribute appears in exactly one group. The pool is empty afterwards.

**Immutability:** Matchers receive the pool as a `List<AttributeDescription>` and return a `MatchResult(groups, remaining)` — they do not modify the input list.

**What happens:**

Registered matchers scan the attribute pool in priority order. Each matcher can claim attributes and remove them from the pool.

| Priority | Matcher | Claims | Example |
|---|---|---|---|
| 1 | Composite | A single OBJECT attribute with a known internal structure | `credential-reference`, `keepalive-time`, `file` |
| 2 | Sibling | n top-level attributes that are semantically related | `path` + `relative-to` |
| 3 | Default | Each remaining attribute becomes its own group | Everything else |

After all matchers have run, every unclaimed attribute becomes a single-attribute group.

**What does NOT happen:**

- No UI decisions — stage 1 doesn't know about view or form items
- No flattening — that's a rendering strategy decided in stage 2
- No provider lookup — stage 1 only groups, it doesn't resolve rendering

**Ordering:** Groups appear in the order of their "primary" attribute's position in the original metadata. For sibling groups, the primary attribute is the first one claimed by the matcher.

## Stage 2: Itemize

**Responsibility:** For each group, create the appropriate view or form item(s).

**Input:** `List<AttributeGroup>` from stage 1, plus the original `AddressTemplate` and `Metadata`. Stage 2 needs:
- `AddressTemplate` — for DMR write operations in form items
- `Metadata.securityContext()` — RBAC determines whether attributes are writable
- `Metadata.resourceDescription()` — capability references, operation metadata

**Output:** `List<ViewItem>` or `List<FormItem>` (depending on which entry point was called).

**What happens:**

For each group, the pipeline tries registered providers in order. First match wins. If no provider matches, default rendering applies.

| Scenario | Result |
|---|---|
| Provider matches → custom item | 1 item (e.g. credential-reference form, path+relative-to paired form) |
| No provider, OBJECT simpleRecord → flatten | n items (one per sub-attribute, prefixed with parent name) |
| No provider, single attribute → default | 1 item (type-based: text input, checkbox, dropdown, etc.) |

**Flattening lives here, not in stage 1.** Flattening answers "how should I render this unclaimed OBJECT?" — a rendering question. Stage 1 says "this OBJECT is unclaimed, here it is as a group." Stage 2 says "nobody wants it as a unit, so I'll flatten it into sub-attribute items."

**DMR write strategies** are encapsulated in each form item:

| Item type | DMR operations |
|---|---|
| Single attribute | `write-attribute(name, value)` → 1 op |
| Composite attribute | `write-attribute(name, {nested object})` → 1 op |
| Sibling group | `write-attribute(name₁, value₁)` + `write-attribute(name₂, value₂)` → n ops |
| Flattened sub-attribute | `write-attribute(name=parent.sub, value)` → 1 op per sub-attribute (uses FQN path) |

All operations from all form items are flat-mapped into a single composite operation.

## Data Types

### AttributeGroup

The contract between stage 1 and stage 2. Holds the grouped attribute descriptions.

```
AttributeGroup
  ├── attributes: List<AttributeDescription>  // 1..n descriptions
  └── name: String                            // display name for the group
```

No `kind` enum (SINGLE/COMPOSITE/SIBLING). Providers in stage 2 don't care how the group was formed — they match by inspecting the attributes inside. A credential-reference provider checks "does this group contain one OBJECT with store/alias/clear-text sub-attributes?" — it doesn't check a kind flag.

### Matcher (stage 1)

Scans the attribute pool and returns claimed groups plus the remaining unclaimed attributes.

```
Matcher
  └── match(pool: List<AttributeDescription>): MatchResult

MatchResult
  ├── groups: List<AttributeGroup>        // claimed groups
  └── remaining: List<AttributeDescription> // unclaimed attributes
```

Each matcher receives the remaining pool (attributes not yet claimed by higher-priority matchers). It returns groups and the remaining pool — no mutation of the input list.

Registered matchers run in priority order:
1. `CredentialReferenceMatcher`, `TimeUnitMatcher`, `FileMatcher` — composite matchers
2. `PathRelativeToMatcher` — sibling matcher
3. (implicit) Everything left → single-attribute groups

### ItemProvider (stage 2)

Creates view and form items for matched groups. Returns lists because a provider may produce multiple items from a single group (e.g. the default provider flattens OBJECT simpleRecords).

```
ItemProvider
  ├── matches(group: AttributeGroup): boolean
  ├── viewItems(group: AttributeGroup, context: PipelineContext): List<PipelineViewItem>
  └── formItems(group: AttributeGroup, context: PipelineContext): List<PipelineFormItem>
```

`PipelineContext` bundles `AddressTemplate`, `Metadata`, `ModelNode` resource values, and `PipelineFlags`. Providers use it to access RBAC, capabilities, current values, and the resource address for DMR operations.

Default methods return null (not an empty list), which signals "fall through to the next provider." This allows a provider to be "FIP only" (custom form items, default view items) or "VIP only" (custom view items, default form items).

### PipelineFlags

Pipeline-generic equivalent of `FormItemFlags`. Controls scope and placeholder behavior for both view and form items.

```
PipelineFlags
  ├── scope: Scope (NEW_RESOURCE | EXISTING_RESOURCE)
  └── placeholder: Placeholder (NONE | UNDEFINED | DEFAULT_VALUE)
```

## Matchers (stage 1 implementations)

### Composite matchers

Each composite matcher inspects individual OBJECT-type attributes for a known internal structure.

**CredentialReferenceMatcher:** Claims OBJECT attributes with sub-attributes `{store, alias, clear-text, type}`. Covers all 9 name variants (credential-reference, shared-secret-reference, key-credential-reference, etc.).

**TimeUnitMatcher:** Claims OBJECT attributes with sub-attributes `{time: LONG, unit: STRING}`. Covers `keepalive-time`.

**FileMatcher:** Claims OBJECT attributes with sub-attributes `{path: STRING, relative-to: STRING}`. Covers the composite `file` attribute in logging.

### Sibling matchers

Each sibling matcher scans the pool for related top-level attributes.

**PathRelativeToMatcher:** For each attribute ending with `relative-to`, looks for a sibling path attribute:

```
For each attribute A ending with "relative-to":
  prefix = A.name without "relative-to" suffix   // "keystore-", "object-store-", or ""
  siblingPath = prefix + "path"
  if pool has attribute named siblingPath:
    → claim [siblingPath, A.name]
  else if prefix == "" and pool has "directory":
    → claim ["directory", A.name]               // undertow access-log exception
```

Naming variants:

| Path Attr | Relative-To Attr | Occurrences |
|---|---|---|
| `path` | `relative-to` | 27 |
| `keystore-path` | `keystore-relative-to` | 2 |
| `object-store-path` | `object-store-relative-to` | 1 |
| `directory` | `relative-to` | 1 |

## Providers (stage 2 implementations)

### Composite providers

**CredentialReferenceProvider:** Custom VIP (compact display of store/alias) and FIP (store typeahead + alias + clear-text fields). Writes one OBJECT attribute.

**TimeUnitProvider:** Custom VIP (e.g. "100 MILLISECONDS") and FIP (number input + unit dropdown). Writes one OBJECT attribute.

**FileProvider:** Custom VIP (e.g. `server.log` relative to `jboss.server.log.dir`) and FIP (text input + path dropdown). Writes one OBJECT attribute.

### Sibling providers

**PathRelativeToProvider:** Custom VIP (e.g. `server.log` relative to `jboss.server.log.dir`) and FIP (text input + path typeahead/dropdown). Writes n separate attributes.

Shares the same UI component as FileProvider — same semantic concept (path + relative-to), different data source (top-level attributes vs. OBJECT sub-attributes).

### Standalone providers

**RelativeToProvider:** Custom FIP only (typeahead/dropdown populated from `/path=*` + standard paths). Default VIP. Matches single `*relative-to` attributes that were not claimed by the sibling matcher.

Only 1 occurrence: `ejb3/file-passivation-store=*` (deprecated). The 4 attributes that allow expressions need a fallback to free-text input.

### Default provider

Handles all unmatched groups:
- OBJECT simpleRecord → flatten into n items (one per sub-attribute, labeled with `parent / sub` prefix)
- Single attribute → type-based rendering (text input, checkbox, number input, dropdown for allowed values, etc.)

## Implementation Status

### Placeholder items

Existing `FormItem` methods (`validate()`, `isModified()`, `modelNode()`) are package-private in `org.jboss.hal.ui.resource.form`. Since existing code is not modified, the adapted providers (credential-reference, time-unit) and the default provider use `PlaceholderViewItem` / `PlaceholderFormItem` as temporary implementations. These render simple text displays and produce no operations.

During migration, when existing code can be modified:
1. Make the needed `FormItem` methods public, OR
2. Create native pipeline implementations for each item type

### What works now

- Full pipeline architecture (Pipeline, matchers, providers, context)
- All matchers correctly claim attributes from the pool
- All providers correctly match their groups
- Pipeline ordering preserves original metadata attribute order
- Immutable pool processing via `MatchResult`

### What needs migration work

- Credential-reference VIP/FIP (complex UI: radio modes, capability typeahead, inline creation)
- Time-unit VIP/FIP (number input + unit dropdown)
- File and path+relative-to VIP/FIP (text input + path typeahead)
- Default type-based VIP/FIP (Boolean, String, Number, Select, etc.)

## Comparison to Current Code

The existing implementation in `org.jboss.hal.ui.resource` splits the same concerns across multiple classes:

| Concern | Current | New pipeline |
|---|---|---|
| Grouping + flattening | `ResourceAttribute.resourceAttributes()` | Stage 1 matchers (grouping only) |
| Composite detection | `CompositeAttributes.isComposite()` + `CompositeAttribute` | Composite matchers in stage 1 |
| Flattening decision | `simpleRecord() && !isComposite()` in `ResourceAttribute` | `DefaultItemProvider` in stage 2 |
| Sibling detection | Not supported | `PathRelativeToMatcher` in stage 1 |
| View item creation | `ViewItemProviders` registry | `ItemProvider.viewItems()` in stage 2 |
| Form item creation | `FormItemProviders` registry | `ItemProvider.formItems()` in stage 2 |
| Data carrier | `ResourceAttribute` (wraps 1 description + value) | `AttributeGroup` (wraps 1..n descriptions) + `PipelineContext` (values) |
| Operation production | `ResourceForm.attributeOperations()` on the form | `PipelineFormItem.operations()` on each item |

The existing code remains untouched. The new pipeline is built separately in this package. Migration happens later.

## Known Attribute Patterns

### Already handled (by existing code)

**credential-reference family** — 49 occurrences, 9 name variants. Matched by OBJECT structure `{store, alias, clear-text, type}`.

### Composite candidates

**keepalive-time** — 19 occurrences across thread pools. Structure: `{time: LONG, unit: STRING}`.

**file** — 8 occurrences in logging subsystem. Structure: `{path: STRING, relative-to: STRING}`.

### Sibling group candidates

**path + relative-to** — 31 occurrences across 11 subsystems. Two separate STRING attributes that are semantically coupled.

### Standalone candidates

**relative-to without sibling path** — 1 deprecated occurrence (`ejb3/file-passivation-store=*`). FIP only.

### Not addressed by this pipeline

**filter (logging)** — Recursive OBJECT, not a simpleRecord. Needs a tree/builder UI.

**schedule (EJB timers)** — Read-only runtime attributes on deployment resources.

**properties / configuration / params** — Free-form key-value maps (not structured records). Need a map editor, not this pipeline.
