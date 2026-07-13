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

**Output:** An ordered list of `ViewItem` or `FormItem`. Each form item knows how to produce 1..n DMR operations via `operations(ResourceAddress)`. These operations are flat-mapped into a single composite operation for the resource write.

**Shape:** One pipeline with two entry points:

```
Pipeline pipeline = Pipeline.create();
pipeline.viewItems(context) → List<ViewItem>
pipeline.formItems(context) → List<FormItem>
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
  ├── viewItems(group: AttributeGroup, context: PipelineContext): List<ViewItem>
  └── formItems(group: AttributeGroup, context: PipelineContext): List<FormItem>
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

### Flattening provider

**FlatteningProvider:** Matches unclaimed OBJECT simpleRecord groups. Flattens into n items, one per sub-attribute. Each sub-attribute uses its `fullyQualifiedName()` for DMR writes (e.g. `"my-record.foo"`). Must be registered after all composite providers and before the default provider.

### Default provider

**DefaultItemProvider:** Catch-all for all remaining unmatched groups. Pure type-based dispatch:
- BOOLEAN → checkbox/switch
- INT/LONG/DOUBLE → number input
- STRING with allowed values → select dropdown
- STRING with capability reference → typeahead
- STRING (plain) → text input
- LIST of STRING → string list editor
- Other → read-only unsupported display

## Implementation Status

All view and form item implementations are complete. No placeholder items remain.

### View items

- `AbstractViewItem` — base class with label creation, value dispatch (restricted/expression/defined/undefined)
- `DefaultViewItem` — type-based rendering (BOOLEAN switch, simple text/unit/allowed, LIST, OBJECT JSON)
- `TimeUnitViewItem` — time + unit display
- `CredentialReferenceViewItem` — store/alias or masked clear-text display
- `FileViewItem` — path + relative-to display
- `PathRelativeToViewItem` — sibling path + relative-to display

### Form items

- `AbstractFormItem` — base class with label, expression toggle, modification tracking, DMR operations
- `StringFormItem` — mixed-mode text input
- `BooleanFormItem` — switch with expression fallback
- `NumberFormItem` — number input with min/max/allowed-values and range validation
- `SelectFormItem` — dropdown for STRING with ALLOWED values
- `UnsupportedFormItem` — read-only JSON fallback
- `RestrictedFormItem` — locked display for unreadable attributes
- `TimeUnitFormItem` — number input + unit dropdown
- `CredentialReferenceFormItem` — radio mode selection (not configured / clear text / credential store)
- `FileFormItem` — path + relative-to text inputs
- `PathRelativeToFormItem` — sibling path + relative-to (produces 2 separate DMR operations)

### What works now

- Full pipeline architecture (Pipeline, matchers, providers, context)
- All matchers correctly claim attributes from the pool
- All providers correctly match their groups and produce real view/form items
- Pipeline ordering preserves original metadata attribute order
- Immutable pool processing via `MatchResult`

## Comparison to Current Code

The existing implementation in `org.jboss.hal.ui.resource` splits the same concerns across multiple classes:

| Concern | Current | New pipeline |
|---|---|---|
| Grouping + flattening | `ResourceAttribute.resourceAttributes()` | Stage 1 matchers (grouping only) |
| Composite detection | `CompositeAttributes.isComposite()` + `CompositeAttribute` | Composite matchers in stage 1 |
| Flattening decision | `simpleRecord() && !isComposite()` in `ResourceAttribute` | `FlatteningProvider` in stage 2 |
| Sibling detection | Not supported | `PathRelativeToMatcher` in stage 1 |
| View item creation | `ViewItemProviders` registry | `ItemProvider.viewItems()` in stage 2 |
| Form item creation | `FormItemProviders` registry | `ItemProvider.formItems()` in stage 2 |
| Data carrier | `ResourceAttribute` (wraps 1 description + value) | `AttributeGroup` (wraps 1..n descriptions) + `PipelineContext` (values) |
| Operation production | `ResourceForm.attributeOperations()` on the form | `FormItem.operations()` on each item |

The existing code remains untouched. The new pipeline is built separately in this package. Migration happens later.

## Management Model Coverage Analysis (WildFly 40)

Total attributes: **5,803** (4,118 configuration + 1,685 runtime)

### Fully covered — 89% (5,165 attributes)

| Type / Pattern | Count | Pipeline handler |
|---|---|---|
| STRING | 2,128 | DefaultItemProvider |
| BOOLEAN | 1,380 | DefaultItemProvider |
| INT | 946 | DefaultItemProvider |
| LONG | 638 | DefaultItemProvider |
| DOUBLE | 36 | DefaultItemProvider |
| BYTES | 1 | DefaultItemProvider (unsupported display) |
| LIST of simple type | 255 | DefaultItemProvider |
| credential-reference family | 49 | CredentialReferenceMatcher + Provider |
| keepalive-time | 8 | TimeUnitMatcher + Provider |
| file (logging) | 8 | FileMatcher + Provider |
| *-column (infinispan JDBC) | 20 | FlatteningProvider (simpleRecord) |
| Other simpleRecord OBJECTs | ~60 | FlatteningProvider |
| path + relative-to siblings | 31 | PathRelativeToMatcher + Provider |
| standalone relative-to | 1 | RelativeToProvider |

The pipeline handles all simple types (STRING, BOOLEAN, INT, LONG, DOUBLE), all LIST-of-simple-type attributes, all known composite OBJECTs, all simpleRecord OBJECTs (flattened), and sibling attribute groups.

### Not yet covered — 11% (638 attributes)

Three distinct UI patterns that differ fundamentally from the single-attribute / simple-record model:

#### 1. Free-form key-value maps — 222 occurrences (HIGH impact)

OBJECT attributes with no structured value-type — arbitrary `{String → String}` maps. These need a **map editor** (add/remove key-value rows), not a type-based form item.

| Attribute | Occurrences |
|---|---|
| `properties` | 105 |
| `*-properties` (capacity-incrementer, stale-connection-checker, etc.) | 71 |
| `params` | 13 |
| `configuration` | 12 |
| `meta-data`, `activation-config`, `property`, etc. | 21 |

**Recommendation:** A `MapProvider` that matches OBJECT attributes with no CONSISTS_OF relationship (or where the value-type is a simple scalar). This is the highest-value next addition.

#### 2. LIST of OBJECT — 52 occurrences (MEDIUM impact)

Lists where each element is a structured object. These need a **table/list editor** with per-row structured editing.

| Attribute | Occurrences | Sub-types |
|---|---|---|
| `timers` | 6 | OBJECT, STRING, BOOLEAN, LONG |
| `filters` | 3 | BOOLEAN, STRING, DOUBLE |
| `wm-security-mapping-*` | 6 | STRING |
| `mechanism-configurations` | 2 | STRING, LIST |
| `permissions`, `certificates`, `global-modules`, etc. | 35 | various |

Many of these (especially `timers`, `certificates`) are runtime-only, so read-only table display would suffice.

#### 3. Complex/recursive OBJECTs — 13 occurrences (LOW impact)

OBJECTs with nested OBJECT or LIST sub-attributes — not `simpleRecord()`.

| Attribute | Occurrences | Notes |
|---|---|---|
| `filter` (logging) | 8 | Recursive tree: `replace`, `not`, `any`, `all`, `level-range` are nested OBJECTs |
| `identity-mapping` | 1 | Deep nesting: sub-OBJECTs + sub-LISTs |
| `jwt` | 1 | Nested OBJECT (`key-map`) + LISTs (`audience`, `issuer`) |
| `attributes` (undertow) | 1 | 35 nested OBJECTs (access log format attributes) |
| `last-gc-info` | 1 | Runtime-only, nested memory usage OBJECTs |

Most are runtime-only or deployment-scoped. The `filter` attribute (8 occurrences) is the only configuration-relevant one, and it would need a tree/builder UI.

### Coverage by storage type

| Storage | Covered | Not covered | Coverage |
|---|---|---|---|
| Configuration (4,118) | 3,687 | 431 | **90%** |
| Runtime (1,685) | 1,478 | 207 | **88%** |

Runtime attributes are read-only, so even "not covered" ones render acceptably as plain text or JSON display.
