# Attribute Pipeline

## What It Does

The attribute pipeline turns WildFly management model metadata into interactive UI controls. WildFly attributes come in many shapes — simple scalars, composite objects, sibling groups, lists of records — but the UI needs consistent view items and form items for all of them. The pipeline bridges that gap: it takes raw attribute descriptions (metadata only), resolves them against live values and RBAC state, and produces the right UI controls automatically.

## The Core Idea

The pipeline uses a two-tier architecture. First, **handlers** scan the attribute pool and claim groups of related attributes they recognize — credential references, path/relative-to pairs, maps, and so on. Each handler knows how to turn its claimed attributes into UI items. Then, everything left unclaimed — plus any child attributes that handlers delegate back — passes through a chain of **providers**, which are simpler factories that produce items based on attribute type.

Handlers live at the boundary between two worlds: the *description world* (metadata only, no values) and the *value world* (resolved snapshots with current values and RBAC state). They receive metadata, perform resolution, and produce items. Providers live entirely in the value world — they receive already-resolved attributes.

## How the Parts Connect

The pipeline has four key interactions that tie everything together:

### 1. Pipeline feeds handlers

The pipeline starts with a pool of all attribute descriptions from the resource metadata. It walks through the registered handlers in priority order. Each handler scans the remaining pool, claims what it recognizes, and returns the rest. The pool shrinks with each handler, so earlier handlers take priority — if `CredentialReferenceHandler` claims an attribute, `FlatteningHandler` never sees it.

### 2. Handlers delegate back to the pipeline

Some handlers produce composite items that contain child attributes (like the `store`, `alias`, and `clear-text` fields inside a `credential-reference`). Instead of duplicating the type-based rendering logic, handlers delegate these children back to the pipeline's provider chain via `Pipeline.viewItem()` / `Pipeline.formItem()`. This keeps rendering logic in one place.

### 3. Pipeline feeds providers

Unclaimed attributes — and children delegated by handlers — go through the provider chain. Each provider is asked whether it handles a given resolved attribute; the first one that says yes creates the item. `DefaultProvider` is the catch-all at the end, dispatching by type (BOOLEAN → switch, STRING → text input, and so on).

### 4. Description world → value world

The transition from metadata to live data happens at resolution time. An `AttributeDescription` becomes a `ResolvedAttribute` by resolving it against a `PipelineContext`, which supplies the current resource values and RBAC permissions. After resolution, the attribute carries not just its type and constraints but also its current value and whether it's readable/writable.

### Trace: `credential-reference`

To see all four interactions in action, follow a `credential-reference` attribute through the pipeline:

```
Pool: [enabled, credential-reference, timeout, ...]
                │
                ▼
   CredentialReferenceHandler scans the pool
   Recognizes OBJECT with {store, alias, clear-text} → claims it
   Pool shrinks to: [enabled, timeout, ...]
                │
                ▼
   Handler resolves credential-reference against PipelineContext
   → ResolvedAttribute with value, readable/writable state
                │
                ▼
   Handler derives child attributes (store, alias, clear-text)
   Delegates each child back to Pipeline.viewItem/formItem
                │
                ▼
   Provider chain handles each child
   DefaultProvider: store → StringControl, alias → StringControl, ...
                │
                ▼
   Handler wraps children in a composite CredentialReferenceViewItem
   or CredentialReferenceControl
```

## Type Flow

The pipeline operates through distinct type transformations:

```
AttributeDescription          raw metadata, no values
        │
        │  handler.match()
        ▼
  AttributeMatch               1..n grouped descriptions
        │
        │  resolve against PipelineContext
        ▼
 ResolvedAttribute             description + value + RBAC
        │
        │  produce items or delegate to providers
        ▼
 ViewItem / FormItem           UI controls
```

`AttributeMatch` lives in the description world — it groups one or more attribute descriptions that a handler recognized as belonging together. `ResolvedAttribute` lives in the value world — it carries the description, the current value, and the RBAC permissions. Handlers bridge the two.

## Use Cases

The pipeline handles five distinct attribute patterns:

### Single attribute

A standalone STRING, BOOLEAN, INT, or other scalar. No handler claims it, so it falls through to the provider chain. The pipeline resolves it into a `ResolvedAttribute` and `DefaultProvider` picks the right control based on type.

### Composite OBJECT

An OBJECT kept as a single composite unit. A handler claims it, resolves the parent, derives children via `parent.child()`, delegates the children to the provider chain, and wraps everything in a composite item. Examples: `credential-reference` (store + alias + clear-text), `file` (path + relative-to), `time-unit` (time + unit).

### Flattened simple-record OBJECT

An OBJECT where all sub-attributes are simple types. `FlatteningHandler` claims it and "flattens" it — each sub-attribute becomes its own item with a fully qualified name (e.g., `my-record.foo`). The children inherit the parent's RBAC state and render as individual form fields rather than a single composite widget.

### Sibling group

Multiple sibling attributes at the same level that semantically belong together. `PathRelativeToHandler` claims both `path` and `relative-to`, resolves both, and produces a single composite item. The `AttributeMatch` holds multiple descriptions, unlike the single-description matches of other handlers.

### List of simple records

A LIST where each entry is an OBJECT with all-simple sub-attributes. `ListSimpleRecordHandler` claims it, resolves the parent, iterates list entries, and delegates children to the provider chain. Renders as a compact table (view) or an editable table with a modal form (edit).

## Handlers

Handlers are registered in `Pipeline.java` in priority order. Order matters: earlier handlers claim first, and `FlatteningHandler` must be last because it claims any remaining simple-record OBJECT.

| Priority | Handler | Pattern |
|---|---|---|
| 1 | `CredentialReferenceHandler` | OBJECT with `{store, alias, clear-text}` |
| 2 | `TimeUnitHandler` | OBJECT with `{time, unit}` |
| 3 | `FileHandler` | OBJECT with `{path, relative-to}` |
| 4 | `PathRelativeToHandler` | Sibling `path` + `relative-to` STRING pairs |
| 5 | `MapHandler` | OBJECT with simple scalar VALUE_TYPE |
| 6 | `ListSimpleRecordHandler` | LIST of OBJECT with all-simple sub-attributes |
| 7 | `FlatteningHandler` | Simple-record OBJECTs (all simple sub-attributes) — catch-all |

## Providers

Providers handle unclaimed attributes and children delegated by handlers. First match wins.

| Priority | Provider | Pattern |
|---|---|---|
| 1 | `RelativeToProvider` | Standalone `relative-to` attributes (form only) |
| 2 | `DefaultProvider` | Type-based dispatch catch-all |

## How to Extend

### Adding a handler

1. Implement `AttributeHandler` — define `match()` to claim your pattern, `viewItems()` and `formItems()` to produce items
2. In `match()`, use the `partition()` helper to split the pool by a predicate, or build custom grouping logic for sibling patterns
3. In the item methods, resolve attributes via `ResolvedAttribute.resolve(context, description)` and delegate children to `Pipeline.viewItem()` / `Pipeline.formItem()` if needed
4. Register in `Pipeline.java`'s static initializer — insert at the right priority position (before `FlatteningHandler`, which must remain last)
5. Test: verify your handler claims the right attributes and produces correct view and form items

### Adding a provider

1. Implement `ItemProvider` — define `handles()` to test whether this provider applies, and `viewItem()` / `formItem()` to produce items
2. Register in `Pipeline.java`'s static initializer — insert before `DefaultProvider`, which must remain last as the catch-all
3. Test: verify your provider handles the right resolved attributes and produces correct items

## How to Debug

When an attribute renders incorrectly or doesn't appear, work through these diagnostic questions:

### Was the attribute claimed?

Check whether any handler's `match()` claimed the attribute. If it was claimed by the wrong handler, a higher-priority handler's predicate is too broad. If it wasn't claimed at all but should have been, the target handler's predicate doesn't match the attribute's structure.

### Was it resolved correctly?

After claiming, the handler resolves the attribute against `PipelineContext`. Check whether the resolved attribute has the expected value, readable/writable flags, and RBAC state. Resolution issues usually mean the `PipelineContext` doesn't have the right data (missing resource values or security context).

### Was it delegated?

For composite attributes, check whether the handler delegates children to the provider chain. If a child renders wrong, the issue is in the provider chain, not the handler. If a child doesn't appear, check whether `parent.child()` or `listEntry()` produces the expected child description.

### Which provider handled it?

For unclaimed attributes and delegated children, check which provider's `handles()` returned `true`. If `DefaultProvider` produced an `UnsupportedControl`, the attribute type isn't supported there yet.

### Where to look in code

| Symptom | Start here |
|---|---|
| Attribute missing entirely | `Pipeline.items()` — is it in the pool? Is it filtered out? |
| Wrong handler claimed it | The handler's `match()` predicate |
| Wrong UI control | The handler's `viewItems()` / `formItems()`, or `DefaultProvider.formItem()` |
| Child attribute wrong | `Pipeline.viewItem()` / `Pipeline.formItem()` — the provider chain |
| RBAC state wrong | `ResolvedAttribute.resolve()` — check context permissions |
| Wrong order | `Pipeline.sortByOriginalOrder()` — uses `AttributeMatch.primary()` for position |

## Implementation Details

The pipeline source code is in `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/`. Each handler documents its pattern recognition logic, claiming rules, resolution strategy, and covered attributes. See the package-level Javadoc in `package-info.java` for the full architecture overview.

For attribute coverage statistics and planned handler work, see [Attribute Pipeline Coverage](attribute-pipeline-coverage.md).
