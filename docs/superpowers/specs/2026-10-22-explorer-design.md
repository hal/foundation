# Model Catalog — Faceted Model Search

## Intent

halOP's quick search (UniversalSearchBox) provides fast, ad-hoc lookup of management model
elements. When MGT is connected, users can search resources, attributes, operations, and
capabilities by name. But the quick search is flat — a single text query with no structured
filtering. The MGT Neo4j database holds 26 filterable facets across ~17,800 nodes
(842 resources, 7,610 attributes, 2,093 operations, 7,062 parameters, 262 capabilities).
None of that structured query power is accessible today.

The Model Catalog is a top-level page in halOP that exposes the full MGT data surface through a
faceted search interface. Users type a text query and progressively narrow results by clicking
facet counts in a sidebar. Active filters show as removable chips. The result table adapts
columns per type. Clicking a result navigates to the resource.

## Why

Admins and developers working with WildFly need to answer questions the quick search cannot:
- "Which attributes in the datasources subtree require a server restart?"
- "What experimental features exist?"
- "Show me all sensitive attributes."
- "Which operations are runtime-only in elytron?"
- "What capabilities does the undertow subsystem declare?"

These are cross-cutting, multi-faceted queries. The data exists in MGT but is only
reachable via direct Cypher queries or the MCP tool interface. The Model Catalog makes it
accessible through the console UI.

## Goals

- Expose all 26 facets of the MGT data model plus 2 live filters through the Model Catalog UI.
- Provide progressive disclosure — facet counts guide users without requiring upfront
  knowledge of the management model vocabulary.
- Results in a compact, sortable table with type-adaptive columns.
- URL-addressable searches — filters encode into query params for bookmarking and sharing.
- Navigate from any result to its resource view in the console.

## Non-Goals

- Replacing the quick search. The Model Catalog is a separate, complementary feature.
- Natural language search. Ruled out due to LLM dependency incompatible with halOP's
  offline/air-gapped deployment model.
- Editing resources from the Model Catalog. It is read-only — navigation to the resource view
  is the action.
- Parameter-level search results. Parameters are searchable as part of their parent
  operations but do not appear as standalone result rows.

## Anti-Goals

- A query builder with dropdowns that must all be set before seeing results. The faceted
  explorer pattern shows results immediately and lets users narrow progressively.
- Overwhelming the user with 27 facets upfront. Facet groups are contextual — they
  collapse/hide based on the selected type filter.

## Constraints

- Available only when MGT is connected. The Model Catalog navigation item is hidden when MGT
  is unavailable.
- The existing quick search (UniversalSearchBox) stays unchanged.
- Two codebases must change: the MGT rest-api (new endpoint) and HAL foundation (new page).
- The REST API response must include both results and facet counts in a single response to
  avoid multiple round-trips per interaction.

---

## Facets

### General (all types)

| # | Facet | Filter key | Values | Notes |
|---|-----------|-----------|--------|-------|
| 1 | Name | `q` | free text | Fulltext search on name only (not description) |
| 2 | Type | `type` | Resource, Attribute, Operation, Capability | Controls which type-specific facet groups are visible |
| 3 | Stability | `stability` | default, community, preview, experimental | |
| 4 | Deprecated | `deprecated` | yes, no, since:&lt;version&gt; | See notes below |
| 5 | Exists on server | — | toggle switch (off by default) | Client-side only, not a server facet. See "Exists on Server" section |
| 6 | Resource scope | `scope` | address prefix (e.g., `/subsystem=elytron`) | Subtree filter — applies to all types. For resources: matches address prefix. For attributes/operations: matches parent resource address. For capabilities: matches declaring resource address. |

### Resource-specific

| # | Facet | Filter key | Values | Notes |
|---|-----------|-----------|--------|-------|
| 7 | Singleton | `singleton` | yes, no | |
| 8 | Declares capability | `declaresCap` | yes, no, contains:&lt;text&gt; | DECLARES_CAPABILITY relationship. `contains:` filters by capability name substring. |
| 9 | Defines attribute groups | `definesAttrGroups` | yes, no | Resource has attributes with attribute-group metadata |
| — | Exists | — | toggle (off by default) | **Live filter.** Checks if the resource exists on the connected server. See "Live Filters" section. |

### Attribute-specific

| # | Facet | Filter key | Values | Notes |
|---|-----------|-----------|--------|-------|
| 10 | Data type | `dataType` | string, boolean, int, long, object, list, double, bytes, complex | complex = has CONSISTS_OF sub-attributes (replaces separate "Complex" facet) |
| 11 | Access type | `accessType` | read-write, read-only, metric | |
| 12 | Storage | `storage` | configuration, runtime | |
| 13 | Restart required | `restart` | all-services, no-services, resource-services, jvm | |
| 14 | Required | `required` | yes, no | |
| 15 | Nillable | `nillable` | yes, no | |
| 16 | Expressions allowed | `expressions` | yes, no | |
| 17 | Sensitive | `sensitive` | yes, no | IS_SENSITIVE relationship |
| 18 | Has allowed values | `hasAllowed` | yes, no | Attributes with enumerated options |
| 19 | Unit | `unit` | milliseconds, bytes, seconds, per_second, nanoseconds, minutes, percentage, days | |
| 20 | Has relationships | `hasRelationships` | yes, no | REQUIRES or ALTERNATIVE dependencies |
| 21 | References capability | `refsCap` | yes, no, contains:&lt;text&gt; | REFERENCES_CAPABILITY relationship. `contains:` filters by capability name substring. |
| — | Defined | — | toggle (off by default) | **Live filter.** Checks if the attribute has a value on the server. Requires "Exists" to be active. See "Live Filters" section. |

### Operation-specific

| # | Facet | Filter key | Values | Notes |
|---|-----------|-----------|--------|-------|
| 22 | Read-only | `readOnly` | yes, no | |
| 23 | Runtime-only | `runtimeOnly` | yes, no | |
| 24 | Global | `global` | yes, no | |
| 25 | Has parameters | `hasParams` | yes, no | |
| 26 | Has return value | `hasReturnValue` | yes, no | |

### Facet Notes

**Description excluded:** The text search (`q`) searches name only, not description.
Descriptions are long prose with common words, leading to noisy, low-precision results.
Users searching for a specific element know its name; description content is better served
by reading the result detail after finding it.

**Deprecated facet:** Supports three modes:
- `deprecated=yes` — show only deprecated elements
- `deprecated=no` — show only non-deprecated elements
- `deprecated=since:25.0.0` — show elements deprecated since a specific management model
  version (uses the `DEPRECATED_SINCE` → `Version` relationship in Neo4j).
  **Deferred:** not implemented in REST API 0.3.0; only `yes`/`no` are supported.

**Data type + Complex merged:** The `COMPLEX` value in the data type facet replaces the
former standalone "Complex" facet. An attribute with `CONSISTS_OF` sub-attributes is
typed as COMPLEX regardless of its declared type (OBJECT or LIST). This gives one facet
for "what kind of data is this" instead of two.

**Capability name filtering:** Both `declaresCap` and `refsCap` support three modes:
`yes` (has any), `no` (has none), or `contains:<text>` (capability name contains substring).
Users often don't know the fully qualified capability name, so substring matching is essential.

**Clear all filters:** Clears all facet selections and live filter toggles, but preserves the
text query.

---

## Live Filters

### Overview

The facets above are **static** — they come from the MGT Neo4j database and
describe the management model schema. Live filters are fundamentally different: they
**cross-reference MGT results with the connected WildFly server** at runtime. They are
rendered as toggle switches within their respective facet groups (Resource, Attribute),
distinguished from static facets by a "live" badge.

Live filters do not produce facet counts and do not affect the server-computed facet counts
in the sidebar. They are client-side filters applied after the server response.

### Exists (Resource)

Checks whether the resource exists on the connected WildFly server. Shown in the Resource
facet group.

- **Default: off.** All MGT results are shown regardless of server state. Page load is fast.
- **Activation:** When toggled on, halOP performs a batch existence check against the live
  server for all unique resource addresses in the current result page.
- **Batch check:** Uses `ModelTree.resolveWildcards()` and the existing `verifyExistence()`
  pipeline — parallel `ResourceCheck` calls, one per unique resource address. A page of 50
  results typically has ~20-30 unique parent resource addresses.
- **Result marking:** Each result row gets an exists/not-exists badge. Non-existing rows can
  be filtered out or shown greyed out (left to the builder).
- **Cascading:** Attributes and operations inherit existence from their parent resource.
  Capabilities exist if at least one declaring resource exists.

#### Add Resource Action

Results marked as not-existing on the server show an **"Add" action** in the result row.
Clicking it opens the add-resource dialog (`AddResourceDialogs.addResourceWizard()` or
`addResourceModal()`) for that resource template. This requires:

1. Looking up `Metadata` for the resource template via `MetadataRepository`.
2. Extracting the `add` operation description and its parameters.
3. Rendering the add-resource form in a modal/wizard.

After successful creation, the existence status of the affected results updates (the newly
created resource and its attributes/operations flip to "exists").

### Defined (Attribute)

Checks whether an attribute has a value set on the connected server. Shown in the Attribute
facet group. **Requires "Exists" to be active** — the toggle is greyed out until Exists is
turned on, because an attribute can only have a value if its parent resource exists.

- **Default: off (and greyed out).** Becomes available once the Exists toggle is on.
- **Activation:** When toggled on, for each existing resource in the result page, halOP reads
  the resource's attribute values (via `read-resource` with `attributes-only=true`) and
  checks which attributes have defined (non-undefined) values.
- **Result marking:** Attribute results get a defined/undefined badge. Undefined attributes
  can be filtered out or shown greyed out.
- **Scope:** Only applies to attribute results. Resource, operation, and capability results
  are unaffected by this toggle.

### Performance

Live filter checks only run when their toggle is activated and only for the current result
page (not the entire result set). Re-checking happens on page navigation and when filters
change while the toggle is active. A loading indicator shows while the checks run. The
Defined check piggybacks on the Exists check — once a resource is confirmed to exist, its
attribute values can be read in the same batch.

---

## Example Queries

Concrete queries mapped to facets and live filters, verifying the design covers real use cases.

### Basic facet queries

| Query | Facets | Result |
|-------|--------|--------|
| All attributes in elytron that allow expressions | `scope=/subsystem=elytron` + `type=Attribute` + `expressions=yes` | ✅ |
| Which operations are runtime-only in undertow? | `scope=/subsystem=undertow` + `type=Operation` + `runtimeOnly=yes` | ✅ |
| What attributes require a JVM restart? | `type=Attribute` + `restart=jvm` | ✅ |
| All sensitive attributes in elytron | `scope=/subsystem=elytron` + `type=Attribute` + `sensitive=yes` | ✅ |
| Which resources are experimental or preview? | `type=Resource` + `stability=experimental,preview` | ✅ |
| Attributes with constrained values in datasources | `scope=/subsystem=datasources` + `type=Attribute` + `hasAllowed=yes` | ✅ |
| All non-global, non-read-only operations | `type=Operation` + `global=no` + `readOnly=no` | ✅ |
| All metrics in the messaging subsystem | `scope=/subsystem=messaging-activemq` + `type=Attribute` + `accessType=metric` | ✅ |
| Attributes that have dependencies on other attributes | `type=Attribute` + `hasRelationships=yes` | ✅ |

### Capability queries

| Query | Facets | Result |
|-------|--------|--------|
| All attributes that reference a capability | `type=Attribute` + `refsCap=yes` | ✅ |
| All attributes referencing a capability containing "datasource" | `type=Attribute` + `refsCap=contains:datasource` | ✅ |
| Resources that declare capabilities | `type=Resource` + `declaresCap=yes` | ✅ |
| Resources declaring a capability containing "undertow" | `type=Resource` + `declaresCap=contains:undertow` | ✅ |

### Deprecation queries

| Query | Facets | Result |
|-------|--------|--------|
| All deprecated resources and attributes | `type=Resource,Attribute` + `deprecated=yes` | ✅ |
| Recently deprecated elements (since model version 25.0.0) | `deprecated=since:25.0.0` | ✅ |
| Recently deprecated resources and attributes | `type=Resource,Attribute` + `deprecated=since:25.0.0` | ✅ |

### Live filter queries

| Query | Facets + Live Filters | Result |
|-------|----------------------|--------|
| All datasources resources that exist on the server | `scope=/subsystem=datasources` + `type=Resource` + Exists=on | ✅ |
| Datasources resources that do NOT exist on the server | `scope=/subsystem=datasources` + `type=Resource` + Exists=on → filter to non-existing | ✅ |
| All undefined attributes in existing elytron resources | `scope=/subsystem=elytron` + `type=Attribute` + Exists=on + Defined=on → filter to undefined | ✅ |

### Implicit type constraint

| Query | User action | What happens |
|-------|------------|--------------|
| Search "session", then click "required: yes" | `q=session` → click Attribute facet "required: yes" | Type implicitly constrained to Attribute. Chips: `[Attribute ✕] [required: yes ✕]`. Removing `[required ✕]` also removes implicit `[Attribute ✕]`. |
| Search "session", select Attribute, then click "required: yes" | `q=session` → click Type "Attribute" → click "required: yes" | Type explicitly set. Chips: `[Attribute ✕] [required: yes ✕]`. Removing `[required ✕]` keeps `[Attribute ✕]` (explicit). |
| Select Attribute + Operation, then click "required: yes" | `type=Attribute,Operation` → click "required: yes" | AND semantics: Operations excluded (no `required` property). Only Attributes with `required=yes` remain. |

---

## REST API Contract

**Codebase:** `/Users/hpehl/dev/wildfly/model-graph/rest-api`

### Endpoint

```
GET /api/catalog?q=&type=&stability=&deprecated=&scope=&...&limit=&offset=
```

All query parameters are optional. When none are provided, the endpoint returns all items
(paginated) with full facet counts.

### Request Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `q` | string | Fulltext search on name only (not description) |
| `type` | string (multi) | Filter by node type: `resource`, `attribute`, `operation`, `capability` |
| `stability` | string (multi) | Filter by stability level |
| `deprecated` | string | `yes`, `no`, or `since:<version>` (e.g., `since:25.0.0`) |
| `scope` | string | Resource address prefix — filters all types by parent resource subtree (for capabilities: by declaring resource address) |
| `singleton` | boolean | Resource: singleton flag |
| `declaresCap` | string | Resource: `yes`, `no`, or `contains:<text>` (capability name substring) |
| `dataType` | string (multi) | Attribute: data type (includes `COMPLEX` for attributes with CONSISTS_OF sub-attributes) |
| `accessType` | string (multi) | Attribute: access type |
| `storage` | string (multi) | Attribute: storage type |
| `restart` | string (multi) | Attribute: restart required level |
| `required` | boolean | Attribute: required flag |
| `nillable` | boolean | Attribute: nillable flag |
| `expressions` | boolean | Attribute: expressions allowed |
| `sensitive` | boolean | Attribute: IS_SENSITIVE |
| `hasAllowed` | boolean | Attribute: has allowed values |
| `unit` | string (multi) | Attribute: unit |
| `definesAttrGroups` | boolean | Resource: defines attribute groups for its attributes |
| `hasRelationships` | boolean | Attribute: has REQUIRES or ALTERNATIVE |
| `refsCap` | string | Attribute: `yes`, `no`, or `contains:<text>` (capability name substring) |
| `readOnly` | boolean | Operation: read-only flag |
| `runtimeOnly` | boolean | Operation: runtime-only flag |
| `global` | boolean | Operation: global flag |
| `hasParams` | boolean | Operation: has parameters |
| `hasReturnValue` | boolean | Operation: has return value |
| `sort` | string | Sort field: `name` (default), `type`, `address`, `stability` |
| `order` | string | Sort direction: `asc` (default), `desc` |
| `limit` | int | Page size (default 50) |
| `offset` | int | Pagination offset (default 0) |

Multi-value parameters accept comma-separated values (e.g., `type=resource,attribute`).

**Multi-value semantics:** OR within a facet, AND across facets. Selecting
`type=resource,attribute` means "Resource OR Attribute". Selecting `type=attribute` and
`accessType=read-write` means "Attribute AND read-write". This is the standard faceted
search convention.

**Type-specific facets under multi-type selection:** When multiple types are selected (e.g.,
`type=attribute,operation`) and a type-specific facet is applied (e.g., `required=yes`),
AND semantics apply. Items that cannot satisfy the predicate (operations have no `required`
property) are excluded from the results. This effectively narrows the result set to only the
applicable type.

### Response

```json
{
  "results": [
    {
      "type": "attribute",
      "name": "connection-url",
      "description": "The JDBC driver connection URL",
      "address": "/subsystem=datasources/data-source=*/connection-url",
      "resourceAddress": "/subsystem=datasources/data-source=*",
      "stability": "default",
      "deprecated": false,
      "properties": {
        "dataType": "string",
        "accessType": "read-write",
        "storage": "configuration",
        "restart": "all-services",
        "required": false,
        "nillable": true,
        "expressions": true,
        "sensitive": false,
        "unit": null,
        "definesAttrGroups": false
      }
    }
  ],
  "facets": {
    "type": { "resource": 42, "attribute": 128, "operation": 38, "capability": 5 },
    "stability": { "default": 200, "community": 8, "preview": 3, "experimental": 2 },
    "deprecated": { "yes": 15, "no": 198 },
    "dataType": { "string": 55, "boolean": 30, "int": 20, "long": 12, "complex": 15, "object": 8, "list": 3 },
    "accessType": { "read-write": 80, "read-only": 35, "metric": 13 },
    "storage": { "configuration": 90, "runtime": 38 },
    "restart": { "all-services": 50, "no-services": 25, "resource-services": 20, "jvm": 2 },
    "required": { "yes": 30, "no": 98 },
    "nillable": { "yes": 100, "no": 28 },
    "expressions": { "yes": 95, "no": 33 },
    "sensitive": { "yes": 12, "no": 116 },
    "hasAllowed": { "yes": 18, "no": 110 },
    "unit": { "milliseconds": 8, "bytes": 5, "seconds": 3 },
    "definesAttrGroups": { "yes": 40, "no": 88 },
    "hasRelationships": { "yes": 10, "no": 118 },
    "refsCap": { "yes": 8, "no": 120 },
    "singleton": { "yes": 20, "no": 22 },
    "declaresCap": { "yes": 15, "no": 27 },
    "readOnly": { "yes": 10, "no": 28 },
    "runtimeOnly": { "yes": 8, "no": 30 },
    "global": { "yes": 5, "no": 33 },
    "hasParams": { "yes": 25, "no": 13 },
    "hasReturnValue": { "yes": 15, "no": 23 }
  },
  "total": 213,
  "limit": 50,
  "offset": 0
}
```

**Facet count semantics:** Facet counts reflect the result set *with all other active filters
applied but without the facet's own filter*. This lets users see how many results each value
would produce if they clicked it. Values with zero count are included (so the UI can show
them greyed out). This is the standard faceted search contract (same as Elasticsearch
aggregations).

**Contextual facet omission:** When a `type` filter is active, the response omits facet
facets that do not apply to the selected types. For example, with `type=operation`, the
response omits attribute-specific facets (dataType, accessType, storage, etc.) and
resource-specific facets (singleton, declaresCap). When no type filter is active or multiple
types are selected, all facets are returned.

### Implementation Notes (REST API)

The endpoint assembles a Cypher query from the filter parameters. The query has two parts:

1. **Result query** — `MATCH` + `WHERE` clauses from filters, `RETURN` result fields,
   `ORDER BY name`, `SKIP offset LIMIT limit`.
2. **Facet aggregation queries** — one per facet, each applying all filters *except its own*
   and returning `value, count(*)` grouped by that facet.

Performance consideration: running 27 separate facet queries is expensive. Optimization
strategies (left to the builder):
- Batch facet aggregations into fewer Cypher queries using `CASE`/conditional counting.
- Cache facet counts for the unfiltered state (they change rarely for a given WildFly version).
- Compute facets only for visible/relevant ones (based on type filter).
- Consider a two-phase approach: return results first, facets asynchronously.

---

## UI Design

**Codebase:** `/Users/hpehl/dev/hal/foundation` (module `op/console`)

### Page Layout

```
┌──────────────────────────────────────────────────────────────────────────┐
│  Model Catalog                                                                │
├──────────────────────────────────────────────────────────────────────────┤
│  [ Search name or description…                                   🔍 ]  │
│                                                                          │
│  Active: [Attribute ✕] [read-write ✕] [all-services ✕]  Clear all      │
│                                                                          │
│  213 results                                                             │
│                                                                          │
│ ┌─────────────────┐  ┌─────────────────────────────────────────────┐    │
│ │                  │  │ Name         Type  Address      Stability  │    │
│ │  GENERAL         │  ├─────────────────────────────────────────────┤    │
│ │  Type            │  │ connection…  Attr  /subsyst…    default    │    │
│ │   Resource    42 │  │ driver-name  Attr  /subsyst…    default    │    │
│ │   Attribute  128 │  │ jndi-name    Attr  /subsyst…    default    │    │
│ │   Operation   38 │  │ …                                          │    │
│ │   Capability   5 │  │                                            │    │
│ │                  │  │                                            │    │
│ │  Stability       │  │                                            │    │
│ │   default   200  │  │                                            │    │
│ │   community   8  │  │                                            │    │
│ │   preview     3  │  │                                            │    │
│ │                  │  │                                            │    │
│ │  Deprecated      │  │                                            │    │
│ │   yes        15  │  │                                            │    │
│ │   no        198  │  │                                            │    │
│ │                  │  │                                            │    │
│ │  Scope       [▾] │  │                                            │    │
│ │                  │  │                                            │    │
│ │  ATTRIBUTE       │  │                                            │    │
│ │  Data Type       │  │                                            │    │
│ │  Access Type     │  │                                            │    │
│ │  Storage         │  │                                            │    │
│ │  Restart Req.    │  │                                            │    │
│ │  Required        │  │                                            │    │
│ │  Nillable        │  │                                            │    │
│ │  Expressions     │  │                                            │    │
│ │  Sensitive       │  │                                            │    │
│ │  Allowed Values  │  │                                            │    │
│ │  Unit            │  │                                            │    │
│ │  Attr Group      │  │                                            │    │
│ │  Complex         │  │                                            │    │
│ │  Relationships   │  │                                            │    │
│ │  Refs Capability │  │                                            │    │
│ │                  │  │                                            │    │
│ │  OPERATION       │  │                                            │    │
│ │  (collapsed)     │  │                                            │    │
│ │                  │  │                                            │    │
│ └─────────────────┘  └─────────────────────────────────────────────┘    │
│                       [ ← 1 2 3 4 5 → ]                                 │
└──────────────────────────────────────────────────────────────────────────┘
```

### Navigation Integration

- Add "Model Catalog" to the main navigation at the same level as Configuration, Deployments, Tasks.
- Visible only when `ModelGraphTools.available()` returns true.
- Route: `/model-catalog` (or consistent with the console's existing route pattern).

### Facet Sidebar Behavior

- **Contextual groups:** When the Type facet has a single selection (e.g., "Attribute"),
  only the General and Attribute facet groups are visible. The Operation and Resource groups
  collapse. When multiple types or no type is selected, all groups are visible.
- **Implicit type constraint:** Selecting a type-specific facet (e.g., "required: yes" from
  the Attribute group) while no type filter is active implicitly constrains results to that
  type. Non-attribute items cannot satisfy attribute-specific predicates, so they are excluded.
  The Type facet updates to show "Attribute" as active, and the chip bar shows both
  `[Attribute ✕] [required: yes ✕]`. The implicit type constraint is tracked separately from
  an explicit user selection:
  - **Implicit type + remove type-specific chip:** Removing `[required ✕]` also removes the
    implicit type constraint and restores all types.
  - **Explicit type + remove type-specific chip:** If the user explicitly selected "Attribute"
    in the Type facet before clicking "required: yes", removing `[required ✕]` keeps the
    explicit type constraint — the user chose that deliberately.
  - **Remove type chip:** Removing `[Attribute ✕]` always removes any type-specific chips
    that depend on it (like `[required ✕]`), regardless of whether the type was implicit or
    explicit, since those facets have no meaning without the type constraint.
- **Collapsible sections:** Each facet group (General, Resource, Attribute, Operation) is
  collapsible. Individual facets within a group are also collapsible when they have many
  values (e.g., Attribute Group with 40+ values).
- **Zero counts:** Facet values with zero count are shown greyed out and non-clickable.
- **Resource scope:** Special facet — a typeahead/autocomplete input for address prefixes
  rather than a list of clickable values. Uses the same address segment completion as the
  quick search.

### Result Table

Compact table with sortable columns. Column set adapts based on the Type filter:

**All types / mixed:**
| Column | Description |
|--------|-------------|
| Name | Element name |
| Type | Badge: Resource / Attribute / Operation / Capability |
| Address | Resource address (truncated with tooltip) |
| Description | Truncated with tooltip |
| Stability | Badge |

**Attributes only (additional columns):**
| Column | Description |
|--------|-------------|
| Data Type | STRING, BOOLEAN, INT, etc. |
| Access Type | read-write, read-only, metric |
| Storage | configuration, runtime |

**Operations only (additional columns):**
| Column | Description |
|--------|-------------|
| Read-only | yes/no |
| Runtime-only | yes/no |

### Result Row Actions

Clicking a result row navigates to the resource. The navigation pipeline is the same as
the quick search:

1. Extract the resource address template from the result.
2. Resolve wildcards via the model tree.
3. Verify the resource exists on the connected server.
4. Navigate to the resource view.
5. For attributes and operations, pass a highlight hint so the target view scrolls to and
   highlights the matching element.

If the resource does not exist on the connected server (the MGT data is for a WildFly
version, not necessarily the one connected), show an inline warning rather than navigating.

### URL State

All active filters sync to URL query parameters. Loading a URL with filter params restores
the full faceted search state. This enables:
- Bookmarking searches.
- Sharing searches via URL.
- Browser back/forward navigation through filter changes.

### Empty and Loading States

- **Initial state (no query, no filters):** Show a welcome message explaining the Model Catalog
  and its capabilities. Optionally show summary statistics (842 resources, 7,610 attributes,
  etc.) as an invitation to explore.
- **Loading:** Skeleton loader in the result table area while the API responds.
- **No results:** Empty state with suggestion to broaden filters. Show which active filters
  could be removed to get results.
- **MGT unavailable:** The Model Catalog page shows a message explaining that MGT is required and
  how to connect it.

---

## Implementation Scope

### MGT REST API (`/Users/hpehl/dev/wildfly/model-graph/rest-api`)

1. New `GET /api/catalog` endpoint.
2. Cypher query builder that assembles `MATCH`/`WHERE` clauses from filter params.
3. Facet aggregation — compute counts per facet respecting cross-filter semantics.
4. Response serialization matching the contract above.
5. Pagination support.

### HAL Foundation (`/Users/hpehl/dev/hal/foundation`)

1. New `Model CatalogPage` top-level page component.
2. Facet sidebar component with collapsible groups, clickable values with counts, zero-count
   styling.
3. Result table with type-adaptive columns, sorting, pagination.
4. Filter chip bar with individual remove and clear-all.
5. URL ↔ filter state synchronization.
6. Navigation integration — add "Model Catalog" to main nav, conditional on MGT availability.
7. REST client for the `/api/catalog` endpoint in `ModelGraphConnection` or a new service class.
8. Resource scope typeahead (address prefix autocomplete).
9. Live filters — "Exists" toggle (batch existence check via `ModelTree`, result row badges,
   "Add" action for non-existing resources using `AddResourceDialogs`) and "Defined" toggle
   (attribute value check via `read-resource`, depends on Exists).

---

## Implementation Context

### MGT REST API tech stack

The REST API at `/Users/hpehl/dev/wildfly/model-graph/rest-api` is a **Quarkus** application.
Existing endpoints (`/api/search`, `/api/identity`, `/api/capability/{name}/references`,
`/api/version`) use JAX-RS resource classes. The Neo4j driver is used directly (not via
Spring Data or OGM). Responses are serialized via Jackson. The new `/api/catalog` endpoint
follows these same conventions.

### HAL console → MGT integration

`ModelGraphConnection` in `core/mgt` is the client-side service that talks to the MGT REST API.
It discovers the MGT URL, exposes `available()` to check connectivity, and provides methods
like `search(query, limit)` that return `Promise`-based results. The new advanced search
client can be added to `ModelGraphConnection` or a dedicated `ModelGraphCatalog` alongside it.
HTTP calls go through the standard async pattern (not the DMR `Dispatcher`).

### Highlight mechanism

The highlight hint system already exists and is used by the quick search. `UniversalSearchBox`
produces a highlight string (e.g., `a:connection-url`) via its `highlight()` method and passes
it to `goTo(template, highlight)`. The `Highlight` class parses these hints, and `ResourceData`
acts on them to scroll and flash the matching element. The Model Catalog reuses this mechanism
as-is.

### Address autocomplete

`ResourceAddressAsyncItems` handles segment-by-segment address completion in the quick search.
For the Model Catalog's scope filter, the same async item source can be used in a standalone
typeahead component. If extraction from `UniversalSearchBox` requires refactoring, that is
within scope.

### Top-level page registration

Top-level pages in halOP are registered via the routing and navigation infrastructure. Existing
pages (Configuration, Deployments, Tasks) provide the pattern. The Model Catalog page follows the
same registration approach, with visibility gated on `ModelGraphTools.available()`.

## Builder's Calls

These decisions were made during spec review and can be revisited:

- **Facet omission:** API omits type-irrelevant facets when a type filter is active (smaller
  response, cleaner UI logic).
- **Sort:** Server-side sorting via `sort` and `order` query params. Client-side column
  headers trigger re-requests with updated sort params.
- **Multi-value semantics:** OR within a facet, AND across facets. Standard faceted search
  convention.

## Open Questions (left to the builder)

- Exact PatternFly components for the facet sidebar (e.g., PF `TreeView`, custom list, or
  `Menu` with sections).
- Cypher query optimization strategy for facet aggregation.
- Whether to debounce the text input or search on Enter.
- Exact column widths and responsive behavior.
- Whether the initial unfiltered state pre-loads results or waits for user input.
- Pagination component choice (PF `Pagination` vs. infinite scroll).
