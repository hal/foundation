# Attribute Pipeline Coverage

Coverage statistics and planned work for the [attribute pipeline](attribute-pipeline.md).

## Coverage (WildFly 42)

### Total Attributes by Storage and Type

| Storage | STRING | BOOLEAN | INT | LONG | DOUBLE | OBJECT | LIST | BYTES | Total |
|---|---|---|---|---|---|---|---|---|---|
| Configuration | 1,628 | 1,065 | 613 | 285 | 19 | 303 | 207 | 1 | 4,121 |
| Runtime | 500 | 318 | 333 | 353 | 17 | 64 | 100 | — | 1,685 |

235 attributes are deprecated across all storage types.

### Configuration OBJECT Breakdown (303 total)

| Category | Count | Handler |
|---|---|---|
| Simple scalar value-type (maps) | 178 | `MapHandler` |
| Simple record (all simple sub-attributes) | 111 | `FlatteningHandler` |
| Complex (nested LIST/OBJECT children) | 14 | Not yet covered |

### Configuration LIST Breakdown (207 total)

| Category | Count | Handler |
|---|---|---|
| LIST of simple type (STRING, INT, etc.) | 173 | `DefaultProvider` |
| LIST of simple records | 27 | `ListSimpleRecordHandler` |
| LIST with nested `LIST<OBJECT>` | 5 | Not yet covered |
| LIST with nested OBJECT | 2 | Not yet covered |

### Summary

The pipeline covers ~95% of all configuration attributes. Excluding deprecated attributes, active coverage is ~97% with only 13 active uncovered attribute definitions remaining. Runtime attributes are read-only, so even uncovered attributes render acceptably as plain text or JSON display.

## Planned Handlers

| Handler | Pattern | Active Count | Priority |
|---|---|---|---|
| List of Nested Complex Lists | LIST of OBJECT with nested `LIST<OBJECT>` sub-attributes | 5 | MEDIUM |
| List of Nested Objects | LIST of OBJECT with nested OBJECT sub-attributes | 2 | MEDIUM |
| Complex Object | Complex/recursive OBJECTs with nested LIST/OBJECT children | 6 | LOW |

Examples: `mechanism-configurations`, `permission-mappings`, `constant-headers`, `services`, `server-auth-modules`, `principal-query`, `identity-mapping`, `jwt`, `any`/`not` (interface), `attributes` (console-access-log), `new-item-template`.
