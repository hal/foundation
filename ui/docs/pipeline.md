# Attribute-to-Item Pipeline

The pipeline transforms resource metadata from the WildFly management model into view and form items for the UI.

## Two-Tier Architecture

### Handlers

`AttributeHandler`s scan the attribute pool in priority order, claiming groups of related attributes into `AttributeMatch`es. Each handler both claims and produces items for its matches, performing resolution (description → value + RBAC snapshot) internally. Handlers bridge the *description world* (metadata only) and the *value world* (resolved snapshots).

Registered handlers (in priority order):

1. `CredentialReferenceHandler` — OBJECT with {store, alias, clear-text}
2. `TimeUnitHandler` — OBJECT with {time, unit}
3. `FileHandler` — OBJECT with {path, relative-to}
4. `PathRelativeToHandler` — sibling path + relative-to STRING pairs
5. `MapHandler` — OBJECT with simple scalar VALUE_TYPE
6. `FlatteningHandler` — simpleRecord OBJECTs (all simple sub-attributes)

### Providers

`ItemProvider`s handle unclaimed attributes and child attributes delegated by handlers. Providers operate in the value world only — they receive already-resolved `ResolvedAttribute`s. First match wins.

1. `RelativeToProvider` — standalone relative-to attributes (form only)
2. `DefaultProvider` — type-based dispatch catch-all

## Type Relationships

```
AttributeDescription  — raw metadata from the management model (no values, no RBAC)
        ↓ handler.match() claims groups
AttributeMatch        — 1..n descriptions that belong together (still no values)
        ↓ handler resolves against PipelineContext
ResolvedAttribute     — 1 description + its current value + readable/writable (snapshot)
        ↓ handler produces items or delegates to provider chain
ViewItem / FormItem   — holds 1..n ResolvedAttributes, renders UI
```

`AttributeMatch` lives in the description world. `ResolvedAttribute` lives in the value world. Handlers bridge the two — they receive matches and context, perform resolution, and either produce items directly or delegate children to the provider chain via `Pipeline.viewItem/formItem`.

## Entry Points

- **Full pipeline** — `Pipeline.instance().viewItems(context)` / `Pipeline.instance().formItems(context)`
- **Child pipeline** — `Pipeline.instance().viewItem(context, resolvedAttribute)` / `Pipeline.instance().formItem(context, resolvedAttribute)`

## Use Cases

### Single attribute (e.g., a STRING `enabled`)

```
Match:    no handler claims it → unclaimed
Resolve:  Pipeline resolves → ResolvedAttribute(enabled)
Provider: DefaultProvider → SwitchControl / StringControl / etc.
Item:     1 item, 1 ResolvedAttribute
```

### Composite OBJECT kept as unit (e.g., `credential-reference`)

```
Match:    CredentialReferenceHandler claims it → AttributeMatch([credential-reference])
Handler:  resolves parent, derives children (store, alias, clear-text) via parent.child()
          delegates children to provider chain → DefaultProvider creates child items
          wraps in composite CredentialReferenceViewItem / CredentialReferenceControl
Item:     1 composite item
```

### Flattened simple-record OBJECT (e.g., an unclaimed `{foo, bar}` OBJECT)

```
Match:    FlatteningHandler claims it → AttributeMatch([my-record])
Handler:  resolves parent (RBAC captured), derives children:
          → parent.child("foo") → ResolvedAttribute(foo) with fqn="my-record.foo"
          → parent.child("bar") → ResolvedAttribute(bar) with fqn="my-record.bar"
          Each child inherits the parent's readable/writable state.
          Delegates each child to Pipeline.viewItem/formItem → provider chain.
Items:    n items, each holds 1 ResolvedAttribute with FQN path
```

### Sibling group (e.g., `path` + `relative-to`)

```
Match:    PathRelativeToHandler claims both → AttributeMatch([path, relative-to])
Handler:  resolves both attributes against context
          creates composite PathRelativeToViewItem / PathRelativeToFormItem
Item:     1 composite item, holds 2 ResolvedAttributes
```

### Summary

| Use case | AttributeMatch | ResolvedAttributes | Items | Operations |
|---|---|---|---|---|
| Single attribute | unclaimed | 1 resolved | 1 item, 1 resolved | 1 op |
| Composite (credential-ref) | 1 desc (OBJECT) | 1 parent + n children | 1 composite item | 1 op (whole OBJECT) |
| Flattened simple-record | 1 desc (OBJECT) | 1 parent + n children | n items | n ops (FQN paths) |
| Sibling group | n descs | n resolved | 1 composite item | n ops (separate attrs) |

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
