# Attribute-to-Item Pipeline

The pipeline transforms resource metadata from the WildFly management model into view and form items for the UI.

## Pipeline Stages

1. **Match** — `AttributeMatcher`s scan the attribute pool in priority order, claiming groups of related attributes into `AttributeMatch`es.
2. **Itemize** — `ItemProvider`s resolve each group against the `PipelineContext` into `ResolvedAttribute`s and create `ViewItem`s or `FormItem`s. Providers are tried in registration order; first match wins.

## Provider Chain

Providers are tried in this order:

1. `CredentialReferenceProvider` — composite: credential-reference
2. `TimeUnitProvider` — composite: time-unit
3. `FileProvider` — composite: file
4. `PathRelativeToProvider` — sibling group: path + relative-to
5. `RelativeToProvider` — standalone: relative-to
6. `FlatteningProvider` — unclaimed simpleRecord OBJECTs → n sub-attribute items
7. `DefaultItemProvider` — everything else: type-based dispatch

## Type Relationships

```
AttributeDescription  — raw metadata from the management model (no values, no RBAC)
        ↓ stage 1 matchers group them
AttributeMatch        — 1..n descriptions that belong together (still no values)
        ↓ stage 2 providers resolve against PipelineContext
ResolvedAttribute     — 1 description + its current value + readable/writable (snapshot)
        ↓ passed to item constructors
FormItem         — holds 1..n ResolvedAttributes, renders UI, produces operations
ViewItem         — holds 1..n ResolvedAttributes, renders read-only display
```

`AttributeMatch` is the stage 1 → stage 2 contract (descriptions only). `ResolvedAttribute` is the stage 2 → item contract (descriptions + values + RBAC). The split happens at the provider: it receives a group, resolves each description against the context, and passes resolved attributes to the item constructor.

## Use Cases

### Single attribute (e.g., a STRING `enabled`)

```
Stage 1: AttributeMatch([enabled])              — 1 description
Stage 2: resolve → ResolvedAttribute(enabled)   — 1 resolved
Item:    holds 1 ResolvedAttribute
         operations() → 1 write-attribute(name="enabled", value=X)
```

### Composite OBJECT kept as unit (e.g., `credential-reference`)

```
Stage 1: AttributeMatch([credential-reference])              — 1 description (the OBJECT)
Stage 2: resolve → ResolvedAttribute(credential-reference)   — 1 resolved
         Sub-attributes (store, alias, clear-text) are INSIDE the value ModelNode
         and the description's valueTypeAttributeDescriptions()
Item:    holds 1 ResolvedAttribute
         operations() → 1 write-attribute(name="credential-reference", value={store:X, alias:Y, ...})
```

### Flattened simple-record OBJECT (e.g., an unclaimed `{foo, bar}` OBJECT)

```
Stage 1: AttributeMatch([my-record])                          — 1 description (the OBJECT)
Stage 2: FlatteningProvider detects simpleRecord, flattens:
         → ResolvedAttribute(foo) with fqn="my-record.foo"    — nested description
         → ResolvedAttribute(bar) with fqn="my-record.bar"
Items:   2 items, each holds 1 ResolvedAttribute
         operations() → 1 write-attribute(name="my-record.foo", value=X) each (FQN path)
```

### Sibling group (e.g., `path` + `relative-to`)

```
Stage 1: AttributeMatch([path, relative-to])                  — 2 descriptions
Stage 2: resolve each:
         → ResolvedAttribute(path)
         → ResolvedAttribute(relative-to)
Item:    1 item, holds 2 ResolvedAttributes
         operations() → 2 write-attribute ops (one per attribute)
```

### Summary

| Use case | AttributeMatch | ResolvedAttributes | Items | Operations |
|---|---|---|---|---|
| Single attribute | 1 desc | 1 resolved | 1 item, 1 resolved | 1 op |
| Composite (credential-ref) | 1 desc (OBJECT) | 1 resolved | 1 item, 1 resolved | 1 op (whole OBJECT) |
| Flattened simple-record | 1 desc (OBJECT) | n resolved | n items, each 1 resolved | n ops (FQN paths) |
| Sibling group | n descs | n resolved | 1 item, n resolved | n ops (separate attrs) |

Entry point: `Pipeline.DEFAULT`

## Type Abbreviations

The handler detail files use these abbreviations for sub-attribute types:

| Abbreviation | Type |
|---|---|
| S | STRING |
| B | BOOLEAN |
| I | INT |
| L | LONG |
| D | DOUBLE |
| MAP | Free-form key-value OBJECT |
| OBJ | Nested OBJECT (simple record) |
| LIST | Nested LIST |

## Attribute Support

See [Pipeline Support](pipeline-support.md) for the full attribute coverage overview and per-handler details.
