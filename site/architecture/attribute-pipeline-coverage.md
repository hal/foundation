---
description: Coverage statistics for the attribute pipeline — active (non-deprecated) configuration attribute breakdown by type and handler coverage.
---

# Attribute Pipeline Coverage

Coverage statistics and planned work for the [attribute pipeline](attribute-pipeline.md). All counts are **active (non-deprecated) configuration attributes** only. Runtime attributes are read-only and render acceptably as plain text or JSON display without pipeline support.

## Coverage (WildFly 42)

### Active Configuration Attributes by Type

| STRING | BOOLEAN | INT | LONG | DOUBLE | OBJECT | LIST | BYTES | Total |
| ------ | ------- | --- | ---- | ------ | ------ | ---- | ----- | ----- |
| 1,628  | 1,065   | 613 | 285  | 19     | 303    | 207  | 1     | 4,121 |

235 attributes are deprecated and excluded from coverage tracking.

### Handler Chain

Handlers and providers are registered in `Pipeline.java` and evaluated in priority order (first match wins). Handlers produce full form-item groups; providers produce individual form items.

#### Handlers (priority order)

| Priority | Handler                    | Pattern                                                      |
| -------- | -------------------------- | ------------------------------------------------------------ |
| 1        | `CredentialReferenceHandler`| OBJECT with `{store, alias, clear-text}` value-type          |
| 2        | `TimeUnitHandler`          | OBJECT with `{time, unit}` value-type                        |
| 3        | `FileHandler`              | OBJECT with `{path, relative-to}` value-type                 |
| 4        | `PathRelativeToHandler`    | Sibling STRING pair (`*path` + `*relative-to`) rendered as one unit |
| 5        | `MapHandler`               | Simple scalar value-type maps                                |
| 6        | `ListSimpleRecordHandler`  | LIST of simple records                                       |
| 7        | `FlatteningHandler`        | Remaining simple records (all simple sub-attributes) — catch-all, must be last |

#### Providers (priority order)

| Priority | Provider           | Pattern                                                            |
| -------- | ------------------ | ------------------------------------------------------------------ |
| 1        | `RelativeToProvider`| Standalone `*relative-to` STRING not claimed by `PathRelativeToHandler` |
| 2        | `DefaultProvider`  | Catch-all fallback for all remaining attributes                    |

### OBJECT Breakdown (303 total)

| Category                                  | Count | Handler                                                                    |
| ----------------------------------------- | ----- | -------------------------------------------------------------------------- |
| Credential references                     | —     | `CredentialReferenceHandler` (subset of simple records)                    |
| Time/unit pairs                           | —     | `TimeUnitHandler` (subset of simple records)                               |
| File path/relative-to                     | —     | `FileHandler` (subset of simple records)                                   |
| Simple scalar value-type (maps)           | 178   | `MapHandler`                                                               |
| Simple record (all simple sub-attributes) | 111   | `FlatteningHandler` (after specialized handlers claim their share)         |
| Complex (nested LIST/OBJECT children)     | 14    | Not yet covered                                                            |

Counts for specialized handlers are subsets carved from the 111 simple-record total. The 178 + 111 + 14 = 303 breakdown remains valid; the specialized handlers simply take priority over `FlatteningHandler` for specific patterns.

### LIST Breakdown (207 total)

| Category                                | Count | Handler                   |
| --------------------------------------- | ----- | ------------------------- |
| LIST of simple type (STRING, INT, etc.) | 173   | `DefaultProvider`         |
| LIST of simple records                  | 27    | `ListSimpleRecordHandler` |
| LIST with nested `LIST<OBJECT>`         | 5     | Not yet covered           |
| LIST with nested OBJECT                 | 2     | Not yet covered           |

### Summary

The pipeline covers ~95% of all active configuration attributes (~97% excluding the 14 complex OBJECTs and 7 complex LISTs). Only 21 active attribute definitions remain uncovered, all with complex nested structures.

## Planned Handlers

| Handler                      | Pattern                                                    | Active Count | Priority |
| ---------------------------- | ---------------------------------------------------------- | ------------ | -------- |
| List of Nested Complex Lists | LIST of OBJECT with nested `LIST<OBJECT>` sub-attributes   | 5            | MEDIUM   |
| List of Nested Objects       | LIST of OBJECT with nested OBJECT sub-attributes           | 2            | MEDIUM   |
| Complex Object               | Complex/recursive OBJECTs with nested LIST/OBJECT children | 14           | LOW      |

Examples: `mechanism-configurations`, `permission-mappings`, `constant-headers`, `services`, `server-auth-modules`, `principal-query`, `identity-mapping`, `jwt`, `any`/`not` (interface), `attributes` (console-access-log), `new-item-template`.