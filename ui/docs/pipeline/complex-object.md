# Complex Object

OBJECTs with nested OBJECT or LIST sub-attributes — not matching the `simpleRecord()` check. These are structurally complex, deeply nested attributes that don't fit the flattening pattern. Sub-attributes are processed recursively by the pipeline.

## Status

- **Implemented**: no
- **Matcher**: n/a
- **Provider**: n/a
- **Priority**: LOW

## Attributes — 7 occurrences

Deprecated and excluded: `filter` on all logging handler types, logger, and root-logger (8 occurrences) — deprecated since model version 1.2.0. Use `filter-spec` (a simple STRING already covered by `DefaultItemProvider`).

### Configuration — standalone (6 occurrences)

| Resource | Attribute | Structure | Storage | Access | Deprecated |
|---|---|---|---|---|---|
| `/subsystem=elytron/ldap-realm=*` | `identity-mapping` | Deep nesting: sub-OBJECTs (`user-password-mapper`, `otp-credential-mapper`, `x509-credential-mapper`) + sub-LISTs (`attribute-mapping`, `new-identity-attributes`) | configuration | read-write | no |
| `/subsystem=elytron/token-realm=*` | `jwt` | Nested OBJECT (`key-map`) + LISTs (`audience`, `issuer`) | configuration | read-write | no |
| `/subsystem=undertow/server=*/host=*/setting=console-access-log` | `attributes` | 35 nested OBJECTs (access log format attributes) | configuration | read-write | no |
| `/interface=*` | `any` | Interface selection criteria with nested LISTs (`inet-address`, `nic`, `nic-match`, `subnet-match`) | configuration | read-write | no |
| `/interface=*` | `not` | Same structure as `any` — negated interface selection criteria | configuration | read-write | no |
| `/subsystem=elytron/ldap-key-store=*` | `new-item-template` | Contains sub-LIST (`new-item-attributes`) | configuration | read-write | no |

### Runtime (1 occurrence)

| Resource | Attribute | Structure | Storage | Access | Deprecated |
|---|---|---|---|---|---|
| `/core-service=platform-mbean/type=garbage-collector` | `last-gc-info` | Nested OBJECTs (`memory-usage-before-gc`, `memory-usage-after-gc`) | runtime | read-only | no |

Note: `last-gc-info` has `community` stability level.

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=elytron/ldap-realm=*` | `identity-mapping` | Deepest nesting — multiple sub-OBJECTs + sub-LISTs |
| `/subsystem=elytron/token-realm=*` | `jwt` | Nested OBJECT + LISTs |
| `/interface=*` | `any`, `not` | Two complex OBJECTs on one resource — interface selection criteria |
| `/core-service=platform-mbean/type=garbage-collector` | `last-gc-info` | Runtime/read-only, `community` stability |
