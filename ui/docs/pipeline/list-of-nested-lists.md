# List of Nested Lists

Lists where each element has simple-type sub-attributes plus one or more nested LIST sub-attributes. The nested LISTs are either of simple type (LIST of STRING) or of simple record (LIST of OBJECT with only simple-type fields). Sub-attributes — including the nested LISTs — are processed recursively by the pipeline.

## Status

- **Implemented**: no
- **Matcher**: n/a
- **Provider**: n/a
- **Priority**: MEDIUM

## Attributes — 8 occurrences

| Resource | Attribute | Simple sub-attrs | Nested LIST sub-attrs | Nested LIST element type | Storage | Access | Deprecated |
|---|---|---|---|---|---|---|---|
| `/core-service=capability-registry` | `capabilities` | name(S), dynamic(B), scope(S) | registration-points | LIST of STRING | runtime | read-only | no |
| `/core-service=management/management-interface=http-interface` | `constant-headers` | path(S) | headers | LIST of {name(S), value(S)} | configuration | read-write | no |
| `/subsystem=elytron/http-authentication-factory=*` | `mechanism-configurations` | mechanism-name(S), host-name(S), protocol(S), pre-realm-principal-transformer(S), post-realm-principal-transformer(S), final-principal-transformer(S), realm-mapper(S), credential-security-factory(S) | mechanism-realm-configurations | LIST of {realm-name(S), pre-realm-principal-transformer(S), post-realm-principal-transformer(S), final-principal-transformer(S), realm-mapper(S)} | configuration | read-write | no |
| `/subsystem=elytron/sasl-authentication-factory=*` | `mechanism-configurations` | (same as above) | mechanism-realm-configurations | (same as above) | configuration | read-write | no |
| `/subsystem=elytron/simple-permission-mapper=*` | `permission-mappings` | match-all(B) | principals, roles, permissions, permission-sets | principals: LIST of STRING; roles: LIST of STRING; permissions: LIST of {class-name(S), module(S), target-name(S), action(S)}; permission-sets: LIST of {permission-set(S)} | configuration | read-write | no |
| `/core-service=capability-registry` | `possible-capabilities` | name(S), dynamic(B) | registration-points | LIST of STRING | runtime | read-only | no |
| `/subsystem=elytron/mapped-role-mapper=*` | `role-map` | from(S) | to | LIST of STRING | configuration | read-write | no |
| `/subsystem=discovery/static-provider=*` | `services` | abstract-type(S), abstract-type-authority(S), uri(S), uri-scheme-authority(S) | attributes | LIST of {name(S), value(S)} | configuration | read-write | no |

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=elytron/mapped-role-mapper=*` | `role-map` | Simplest case — one simple sub-attr + one nested LIST of STRING |
| `/core-service=management/management-interface=http-interface` | `constant-headers` | One simple sub-attr + one nested LIST of simple record |
| `/subsystem=elytron/http-authentication-factory=*` | `mechanism-configurations` | Complex — 8 simple sub-attrs + nested LIST of 5-field record |
| `/subsystem=elytron/simple-permission-mapper=*` | `permission-mappings` | Most complex — 4 nested LISTs of different types |
