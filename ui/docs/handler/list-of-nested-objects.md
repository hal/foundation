# List of Nested Objects

Lists where each element has simple-type sub-attributes plus nested OBJECT sub-attributes (simple records, not free-form maps). Sub-attributes — including nested OBJECTs and nested LISTs — are processed recursively by the pipeline.

## Status

- **Implemented**: no
- **Matcher**: n/a
- **Provider**: n/a
- **Priority**: MEDIUM

## Attributes — 1 occurrence

| Resource | Attribute | Structure | Storage | Access | Deprecated |
|---|---|---|---|---|---|
| `/subsystem=elytron/jdbc-realm=*` | `principal-query` | sql(S), data-source(S), attribute-mapping(LIST of {to(S), index(I)}), clear-password-mapper(OBJ: {password-index(I)}), bcrypt-mapper(OBJ: {password-index(I), salt-index(I), iteration-count-index(I), hash-encoding(S), salt-encoding(S)}), salted-simple-digest-mapper(OBJ), simple-digest-mapper(OBJ), scram-mapper(OBJ), modular-crypt-mapper(OBJ) | configuration | read-write | no |

All nested OBJECTs are simple records. Also has a nested LIST of simple record (`attribute-mapping`).

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=elytron/jdbc-realm=*` | `principal-query` | The only occurrence; deep nesting with multiple mapper OBJECTs + one nested LIST |
