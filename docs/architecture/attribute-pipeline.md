# Attribute Pipeline

## Purpose

The attribute pipeline transforms WildFly management model metadata into view and form items for the UI. It bridges the gap between raw attribute descriptions (metadata only from the management model) and interactive UI controls that display values and enable editing.

The pipeline solves the core problem of rendering heterogeneous WildFly attributes — simple scalars, complex nested objects, sibling groups, and composite structures — through a consistent, extensible architecture.

## Design

The pipeline uses a **two-tier architecture**: handlers claim and produce items for known patterns, while providers handle unclaimed attributes and child attributes delegated by handlers.

### Handlers

`AttributeHandler`s scan the attribute pool in priority order, claiming groups of related attributes into `AttributeMatch`es. Each handler both claims and produces items for its matches. Handlers bridge the **description world** (metadata only) and the **value world** (resolved snapshots with current values and RBAC state).

Registered handlers in priority order:

| Priority | Handler | Pattern | Attributes |
|---|---|---|---|
| 1 | `CredentialReferenceHandler` | OBJECT with {store, alias, clear-text} | 49 |
| 2 | `TimeUnitHandler` | OBJECT with {time, unit} | 8 |
| 3 | `FileHandler` | OBJECT with {path, relative-to} | 8 |
| 4 | `PathRelativeToHandler` | sibling path + relative-to STRING pairs | 31 |
| 5 | `MapHandler` | OBJECT with simple scalar VALUE_TYPE | 222 |
| 6 | `FlatteningHandler` | simpleRecord OBJECTs (all simple sub-attributes) | ~80 |

### Providers

`ItemProvider`s handle unclaimed attributes and child attributes delegated by handlers. Providers operate in the value world only — they receive already-resolved `ResolvedAttribute`s. First match wins.

Registered providers in order:

| Priority | Provider | Pattern | Attributes |
|---|---|---|---|
| 1 | `RelativeToProvider` | standalone relative-to attributes (form only) | 1 |
| 2 | `DefaultProvider` | type-based dispatch catch-all | 5,384 |

### Type Flow

The pipeline operates through distinct type transformations:

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

### Use Cases

The pipeline handles four distinct attribute patterns:

| Pattern | Match | Resolution | Items | Example |
|---|---|---|---|---|
| **Single attribute** | Unclaimed | 1 resolved | 1 item, 1 resolved | `enabled` (STRING) |
| **Composite OBJECT** | 1 OBJECT desc | 1 parent + n children | 1 composite item | `credential-reference` |
| **Flattened simple-record** | 1 OBJECT desc | 1 parent + n children | n items with FQN paths | `{foo, bar}` OBJECT |
| **Sibling group** | n descs | n resolved | 1 composite item | `path` + `relative-to` |

#### Single Attribute

A standalone STRING, BOOLEAN, INT, etc.

```
Match:    no handler claims it → unclaimed
Resolve:  Pipeline resolves → ResolvedAttribute(enabled)
Provider: DefaultProvider → SwitchControl / StringControl / etc.
Item:     1 item, 1 ResolvedAttribute
```

#### Composite OBJECT

An OBJECT kept as a single unit (e.g., `credential-reference`).

```
Match:    CredentialReferenceHandler claims it → AttributeMatch([credential-reference])
Handler:  resolves parent, derives children (store, alias, clear-text) via parent.child()
          delegates children to provider chain → DefaultProvider creates child items
          wraps in composite CredentialReferenceViewItem / CredentialReferenceControl
Item:     1 composite item
```

#### Flattened Simple-Record OBJECT

An OBJECT with all simple sub-attributes, flattened into individual items.

```
Match:    FlatteningHandler claims it → AttributeMatch([my-record])
Handler:  resolves parent (RBAC captured), derives children:
          → parent.child("foo") → ResolvedAttribute(foo) with fqn="my-record.foo"
          → parent.child("bar") → ResolvedAttribute(bar) with fqn="my-record.bar"
          Each child inherits the parent's readable/writable state.
          Delegates each child to Pipeline.viewItem/formItem → provider chain.
Items:    n items, each holds 1 ResolvedAttribute with FQN path
```

#### Sibling Group

Multiple sibling attributes that semantically belong together (e.g., `path` + `relative-to`).

```
Match:    PathRelativeToHandler claims both → AttributeMatch([path, relative-to])
Handler:  resolves both attributes against context
          creates composite PathRelativeToViewItem / PathRelativeToFormItem
Item:     1 composite item, holds 2 ResolvedAttributes
```

## Current State & Open Work

### Coverage (WildFly 40)

The pipeline covers **~93%** of all 5,803 attributes (~5,387 attributes):

| Storage | Total | Covered | Not Covered | Coverage |
|---|---|---|---|---|
| Configuration | 4,118 | ~3,909 | ~209 | ~95% |
| Runtime | 1,685 | ~1,478 | ~207 | ~88% |

Runtime attributes are read-only, so even uncovered attributes render acceptably as plain text or JSON display.

### Planned Handlers

The following handlers are planned but not yet implemented:

| Handler | Pattern | Count | Priority |
|---|---|---|---|
| **List of Simple Records** | LIST of OBJECT with simple-type sub-attributes | 19 | HIGH |
| **List of Nested Lists** | LIST of OBJECT with nested LIST sub-attributes | 8 | MEDIUM |
| **List of Nested Objects** | LIST of OBJECT with nested OBJECT sub-attributes | 1 | MEDIUM |
| **Complex Object** | Complex/recursive OBJECTs (not lists) | 7 | LOW |

These represent the remaining 7% of uncovered attributes. Implementing the high-priority "List of Simple Records" handler would push coverage past 95%.

## Implementation Details

Detailed handler documentation lives alongside the source code in `ui/docs/handler/`. Each handler documents:

- Pattern recognition logic
- Attribute claiming rules
- Resolution strategy
- Item production approach
- Covered attributes with examples

The pipeline source code is in `ui/src/main/java/org/jboss/hal/ui/resource/pipeline/`, with comprehensive package-level Javadoc describing the architecture and data flow.
