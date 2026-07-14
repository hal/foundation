# Management Model Coverage (WildFly 40)

Analysis of the WildFly management model attributes and their pipeline coverage.

Total attributes: **5,803** (4,118 configuration + 1,685 runtime)

## Covered — ~93% (~5,387 attributes)

| Type / Pattern | Count | Pipeline handler |
|---|---|---|
| STRING | 2,128 | DefaultItemProvider |
| BOOLEAN | 1,380 | DefaultItemProvider |
| INT | 946 | DefaultItemProvider |
| LONG | 638 | DefaultItemProvider |
| DOUBLE | 36 | DefaultItemProvider |
| BYTES | 1 | DefaultItemProvider (unsupported display) |
| LIST of simple type | 255 | DefaultItemProvider |
| credential-reference family | 49 | CredentialReferenceMatcher + CredentialReferenceProvider |
| keepalive-time | 8 | TimeUnitMatcher + TimeUnitProvider |
| file (logging) | 8 | FileMatcher + FileProvider |
| \*-column (infinispan JDBC) | 20 | FlatteningProvider (simpleRecord) |
| Other simpleRecord OBJECTs | ~60 | FlatteningProvider |
| path + relative-to siblings | 31 | PathRelativeToMatcher + PathRelativeToProvider |
| standalone relative-to | 1 | RelativeToProvider |
| Free-form key-value maps | 222 | MapMatcher + MapProvider |

## Not yet covered — ~7% (~416 attributes)

Two distinct UI patterns that differ fundamentally from the single-attribute / simple-record model.

### 1. LIST of OBJECT — 52 occurrences (MEDIUM impact)

Lists where each element is a structured object. These need a table/list editor with per-row structured editing.

| Attribute | Occurrences | Sub-types |
|---|---|---|
| `timers` | 6 | OBJECT, STRING, BOOLEAN, LONG |
| `filters` | 3 | BOOLEAN, STRING, DOUBLE |
| `wm-security-mapping-*` | 6 | STRING |
| `mechanism-configurations` | 2 | STRING, LIST |
| `permissions`, `certificates`, `global-modules`, etc. | 35 | various |

Many of these (especially `timers`, `certificates`) are runtime-only, so read-only table display would suffice.

### 2. Complex/recursive OBJECTs — 13 occurrences (LOW impact)

OBJECTs with nested OBJECT or LIST sub-attributes — not `simpleRecord()`.

| Attribute | Occurrences | Notes |
|---|---|---|
| `filter` (logging) | 8 | Recursive tree: `replace`, `not`, `any`, `all`, `level-range` are nested OBJECTs |
| `identity-mapping` | 1 | Deep nesting: sub-OBJECTs + sub-LISTs |
| `jwt` | 1 | Nested OBJECT (`key-map`) + LISTs (`audience`, `issuer`) |
| `attributes` (undertow) | 1 | 35 nested OBJECTs (access log format attributes) |
| `last-gc-info` | 1 | Runtime-only, nested memory usage OBJECTs |

Most are runtime-only or deployment-scoped. The `filter` attribute (8 occurrences) is the only configuration-relevant one, and it would need a tree/builder UI.

## Coverage by storage type

| Storage | Covered | Not covered | Coverage |
|---|---|---|---|
| Configuration (4,118) | ~3,909 | ~209 | **~95%** |
| Runtime (1,685) | ~1,478 | ~207 | **~88%** |

Runtime attributes are read-only, so even uncovered ones render acceptably as plain text or JSON display.
