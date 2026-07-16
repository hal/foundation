# List of Simple Records

Lists where each element is a flat object with only simple-type sub-attributes (STRING, BOOLEAN, INT, LONG, DOUBLE) or free-form key-value map OBJECTs (already covered by MapProvider). Sub-attributes of each list element are processed recursively by the pipeline — each sub-attribute is matched and provided by the appropriate handler.

## Status

- **Implemented**: no
- **Matcher**: n/a
- **Provider**: n/a
- **Priority**: HIGH

## Sub-attribute count distribution

Average: 3.0, median: 2.

| # Sub-attrs | Occurrences | Share | Attributes |
|---|---|---|---|
| 1 | 1 | 5% | `permission-sets` |
| 2 | 8 | 42% | `certificate-revocation-lists`, `client-interceptors`, `filters`(http), `incoming-interceptors`, `outgoing-interceptors`, `server-interceptors`, `wm-security-mapping-groups`, `wm-security-mapping-users` |
| 3 | 3 | 16% | `client-mappings`, `filters`(sasl), `resolvers` |
| 4 | 5 | 26% | `filters`(mechanism-provider), `permissions`, `realms`, `server-auth-modules`, `static-ejb-discovery` |
| 5 | 1 | 5% | `global-modules` |
| 12 | 1 | 5% | `match-rules` |

84% of occurrences have 2–4 sub-attributes. Only `match-rules` (12) is a significant outlier.

## Attributes — 19 occurrences

All have `stability: default`. No nested LIST-of-OBJECT within sub-attributes.

### Configuration — standalone (19 occurrences)

| Resource | Attribute | Sub-attributes | Storage | Access | Deprecated |
|---|---|---|---|---|---|
| `/subsystem=elytron/trust-manager=*` | `certificate-revocation-lists` | path(S), relative-to(S) | configuration | read-write | no |
| `/subsystem=ejb3` | `client-interceptors` | class(S), module(S) | configuration | read-write | no |
| `/socket-binding-group=*/socket-binding=*` | `client-mappings` | source-network(S), destination-address(S), destination-port(I) | configuration | read-write | no |
| `/subsystem=elytron/configurable-http-server-mechanism-factory=*` | `filters` | pattern-filter(S), enabling(B) | configuration | read-write | no |
| `/subsystem=elytron/configurable-sasl-server-factory=*` | `filters` | predefined-filter(S), pattern-filter(S), enabling(B) | configuration | read-write | no |
| `/subsystem=elytron/mechanism-provider-filtering-sasl-server-factory=*` | `filters` | mechanism-name(S), provider-name(S), provider-version(D), version-comparison(S) | configuration | read-write | no |
| `/subsystem=ee` | `global-modules` | name(S), slot(S), annotations(B), services(B), meta-inf(B) | configuration | read-write | no |
| `/subsystem=messaging-activemq/server=*` | `incoming-interceptors` | name(S), module(S) | configuration | read-write | no |
| `/subsystem=elytron/authentication-context=*` | `match-rules` | match-abstract-type(S), match-abstract-type-authority(S), match-host(S), match-local-security-domain(S), match-no-user(B), match-path(S), match-port(I), match-protocol(S), match-urn(S), match-user(S), authentication-configuration(S), ssl-context(S) | configuration | read-write | no |
| `/subsystem=messaging-activemq/server=*` | `outgoing-interceptors` | name(S), module(S) | configuration | read-write | no |
| `/subsystem=elytron/constant-permission-mapper=*` | `permission-sets` | permission-set(S) | configuration | read-write | no |
| `/subsystem=elytron/permission-set=*` | `permissions` | class-name(S), module(S), target-name(S), action(S) | configuration | read-write | no |
| `/subsystem=elytron/security-domain=*` | `realms` | realm(S), principal-transformer(S), role-decoder(S), role-mapper(S) | configuration | read-write | no |
| `/subsystem=elytron/expression=encryption` | `resolvers` | name(S), credential-store(S), secret-key(S) | configuration | read-write | no |
| `/subsystem=elytron/jaspi-configuration=*` | `server-auth-modules` | class-name(S), module(S), flag(S), options(MAP) | configuration | read-write | no |
| `/subsystem=ejb3` | `server-interceptors` | class(S), module(S) | configuration | read-write | no |
| `/subsystem=ejb3/remoting-profile=*` | `static-ejb-discovery` | uri(S), app-name(S), module-name(S), distinct-name(S) | configuration | read-write | no |
| `/subsystem=resource-adapters/resource-adapter=*` | `wm-security-mapping-groups` | from(S), to(S) | configuration | read-write | no |
| `/subsystem=resource-adapters/resource-adapter=*` | `wm-security-mapping-users` | from(S), to(S) | configuration | read-write | no |

### Configuration — deployment-scoped (5 occurrences, LOW priority)

Deployment resources are typically read-only in the console.

| Resource | Attribute | Sub-attributes | Storage | Access | Deprecated |
|---|---|---|---|---|---|
| `/deployment=*` | `content` | hash, path, relative-to, archive | configuration | read-write | no |
| `/subsystem=resource-adapters/resource-adapter=*/connection-definitions=*` | `wm-security-mapping-groups` | from(S), to(S) | configuration | read-write | no |
| `/subsystem=resource-adapters/resource-adapter=*/connection-definitions=*` | `wm-security-mapping-users` | from(S), to(S) | configuration | read-write | no |
| `/subsystem=resource-adapters/resource-adapter=*/admin-objects=*` | `wm-security-mapping-groups` | from(S), to(S) | configuration | read-write | no |
| `/subsystem=resource-adapters/resource-adapter=*/admin-objects=*` | `wm-security-mapping-users` | from(S), to(S) | configuration | read-write | no |

### Runtime (6 occurrences, LOW priority — read-only display)

| Resource | Attribute | Sub-attributes | Storage | Access | Deprecated |
|---|---|---|---|---|---|
| `/deployment=*/subsystem=ejb3/stateless-session-bean=*` | `timers` | schedule, info, persistent, calendar-timer, next-timeout, time-remaining | runtime | read-only | no |
| `/deployment=*/subsystem=ejb3/stateful-session-bean=*` | `timers` | (same) | runtime | read-only | no |
| `/deployment=*/subsystem=ejb3/singleton-bean=*` | `timers` | (same) | runtime | read-only | no |
| `/deployment=*/subsystem=ejb3/message-driven-bean=*` | `timers` | (same) | runtime | read-only | no |
| `/subsystem=elytron/key-store=*` | `loaded-providers` | name, info, version, services | runtime | read-only | no |
| `/subsystem=datasources/data-source=*/statistics=jdbc` | `installed-drivers` | deployment-name, driver-module-name, module-slot, driver-class-name, ... | runtime | read-only | no |

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=ee` | `global-modules` | 5 sub-attributes, available out of the box |
| `/subsystem=ejb3` | `client-interceptors`, `server-interceptors` | 2 sub-attributes each |
| `/subsystem=elytron/permission-set=*` | `permissions` | 4 sub-attributes |
| `/subsystem=elytron/authentication-context=*` | `match-rules` | 12 sub-attributes — outlier for testing wide records |
| `/subsystem=elytron/constant-permission-mapper=*` | `permission-sets` | 1 sub-attribute — minimal case |
| `/subsystem=resource-adapters/resource-adapter=*` | `wm-security-mapping-groups`, `wm-security-mapping-users` | 2 sub-attributes, two lists on one resource |

Deprecated and excluded: `permissions` on `/subsystem=elytron/constant-permission-mapper=*` — deprecated since model version 3.0.0. Use `permission-sets` instead.
