# Flattening

Handles simpleRecord OBJECT attributes — OBJECTs whose sub-attributes are all simple types. The provider "flattens" the nested object's fields into the parent form/view, so each sub-attribute appears as its own form item.

Covers two sub-patterns:
- **\*-column (Infinispan JDBC)**: Column definition OBJECTs with `name` + `type` sub-attributes (~20 occurrences)
- **Other simpleRecord OBJECTs**: Various OBJECTs with simple-type sub-attributes (~60 occurrences)

## Status

- **Implemented**: yes
- **Matcher**: n/a (uses `simpleRecord()` check)
- **Provider**: `FlatteningProvider`
- **Priority**: —

## Attributes — ~80 occurrences

### \*-column (Infinispan JDBC) — ~20 occurrences

| Resource | Attribute | Sub-attributes | Storage | Access | Deprecated |
|---|---|---|---|---|---|
| `/subsystem=infinispan/cache-container=*/distributed-cache=*/store=jdbc/table=string` | `id-column`, `data-column`, `segment-column`, `timestamp-column` | name(S), type(S) | configuration | read-write | no |
| `/subsystem=infinispan/cache-container=*/local-cache=*/store=jdbc/table=string` | `id-column`, `data-column`, `segment-column`, `timestamp-column` | name(S), type(S) | configuration | read-write | no |
| `/subsystem=infinispan/cache-container=*/replicated-cache=*/store=jdbc/table=string` | `id-column`, `data-column`, `segment-column`, `timestamp-column` | name(S), type(S) | configuration | read-write | no |

### Other simpleRecord OBJECTs — ~60 occurrences

| Resource | Attribute | Sub-attributes | Storage | Access | Deprecated |
|---|---|---|---|---|---|
| `/core-service=platform-mbean/type=memory` | `heap-memory-usage`, `non-heap-memory-usage` | init(L), used(L), committed(L), max(L) | runtime | read-only | no |
| `/core-service=platform-mbean/type=memory-pool/name=*` | `usage`, `peak-usage`, `collection-usage` | init(L), used(L), committed(L), max(L) | runtime | read-only | no |
| `/core-service=management/management-interface=http-interface` | `http-upgrade` | enabled(B), sasl-authentication-factory(S) | configuration | read-write | no |
| `/subsystem=elytron/properties-realm=*` | `users-properties`, `groups-properties` | path(S), relative-to(S) + others | configuration | read-write | no |
| `/subsystem=elytron/trust-manager=*` | `ocsp`, `certificate-revocation-list` | various STRING sub-attrs | configuration | read-write | no |
| `/subsystem=elytron/authentication-configuration=*` | `webservices` | http-mechanism(S), ws-security-type(S) | configuration | read-write | no |

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=infinispan/cache-container=*/distributed-cache=*/store=jdbc/table=string` | `id-column`, `data-column`, `segment-column`, `timestamp-column` | Four column OBJECTs on one resource |
| `/subsystem=infinispan/cache-container=*/local-cache=*/store=jdbc/table=string` | `id-column`, `data-column`, `segment-column`, `timestamp-column` | Same pattern on local cache |
| `/subsystem=infinispan/cache-container=*/replicated-cache=*/store=jdbc/table=string` | `id-column`, `data-column`, `segment-column`, `timestamp-column` | Same pattern on replicated cache |
| `/core-service=platform-mbean/type=memory` | `heap-memory-usage`, `non-heap-memory-usage` | Read-only; available on every server |
| `/core-service=platform-mbean/type=memory-pool/name=*` | `usage`, `peak-usage`, `collection-usage` | Read-only memory pool stats |
| `/core-service=management/management-interface=http-interface` | `http-upgrade` | Writable simpleRecord |
| `/subsystem=elytron/properties-realm=*` | `users-properties`, `groups-properties` | Writable simpleRecord |
| `/subsystem=elytron/trust-manager=*` | `ocsp`, `certificate-revocation-list` | Writable simpleRecords |
| `/subsystem=elytron/authentication-configuration=*` | `webservices` | Writable simpleRecord |
