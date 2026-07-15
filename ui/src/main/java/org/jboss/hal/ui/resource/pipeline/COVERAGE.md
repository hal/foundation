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

### 1. LIST of OBJECT — 51 occurrences (MEDIUM impact)

Lists where each element is a structured object. These need a table/list editor with per-row structured editing. Deprecated attributes are excluded from this analysis.

All have `stability: default`. No nested LIST-of-OBJECT within sub-attributes (except `mechanism-configurations` whose `mechanism-realm-configurations` sub-attribute is itself a LIST).

Deprecated and excluded:
- `permissions` on `constant-permission-mapper` — deprecated since model version 3.0.0. Use `permission-sets` instead.

#### By storage and scope

| | Standalone | Deployment-scoped | Total |
|---|---|---|---|
| **Configuration** | 28 | 5 | **33** |
| **Runtime** | 6 | 12 | **18** |

#### Configuration — standalone (28 occurrences, HIGH priority)

These are the most impactful gaps — editable attributes in non-deployment resources. Classified by structural complexity (sub-attribute types verified via model graph `CONSISTS_OF` relationships).

##### A. List\<simple record\> — 19 occurrences

Each list element is a flat object with simple-type sub-attributes (STRING, BOOLEAN, INT, LONG, DOUBLE) or free-form key-value map OBJECTs (already covered by MapProvider). A single list-editor dialog would cover all of these.

| Attribute | Resource(s) | Sub-attributes |
|---|---|---|
| `certificate-revocation-lists` | elytron/trust-manager=\* | path(S), relative-to(S) |
| `client-interceptors` | ejb3 | class(S), module(S) |
| `client-mappings` | socket-binding-group=\*/socket-binding=\* | source-network(S), destination-address(S), destination-port(I) |
| `filters` | elytron/configurable-http-server-mechanism-factory=\* | pattern-filter(S), enabling(B) |
| `filters` | elytron/configurable-sasl-server-factory=\* | predefined-filter(S), pattern-filter(S), enabling(B) |
| `filters` | elytron/mechanism-provider-filtering-sasl-server-factory=\* | mechanism-name(S), provider-name(S), provider-version(D), version-comparison(S) |
| `global-modules` | ee | name(S), slot(S), annotations(B), services(B), meta-inf(B) |
| `incoming-interceptors` | messaging-activemq/server=\* | name(S), module(S) |
| `match-rules` | elytron/authentication-context=\* | match-abstract-type(S), match-abstract-type-authority(S), match-host(S), match-local-security-domain(S), match-no-user(B), match-path(S), match-port(I), match-protocol(S), match-urn(S), match-user(S), authentication-configuration(S), ssl-context(S) |
| `outgoing-interceptors` | messaging-activemq/server=\* | name(S), module(S) |
| `permission-sets` | elytron/constant-permission-mapper=\* | permission-set(S) |
| `permissions` | elytron/permission-set=\* | class-name(S), module(S), target-name(S), action(S) |
| `realms` | elytron/security-domain=\* | realm(S), principal-transformer(S), role-decoder(S), role-mapper(S) |
| `resolvers` | elytron/expression=encryption | name(S), credential-store(S), secret-key(S) |
| `server-auth-modules` | elytron/jaspi-configuration=\* | class-name(S), module(S), flag(S), options(MAP) |
| `server-interceptors` | ejb3 | class(S), module(S) |
| `static-ejb-discovery` | ejb3/remoting-profile=\* | uri(S), app-name(S), module-name(S), distinct-name(S) |
| `wm-security-mapping-groups` | resource-adapters/resource-adapter=\* | from(S), to(S) |
| `wm-security-mapping-users` | resource-adapters/resource-adapter=\* | from(S), to(S) |

##### B. List\<simple record + nested List\> — 8 occurrences

Each list element has simple-type sub-attributes plus one or more nested LIST sub-attributes. The nested LISTs are either of simple type (LIST of STRING) or of simple record (LIST of OBJECT with only simple-type fields).

| Attribute | Resource(s) | Simple sub-attrs | Nested LIST sub-attrs | Nested LIST element type |
|---|---|---|---|---|
| `capabilities` | core-service=capability-registry | name(S), dynamic(B), scope(S) | registration-points | LIST of STRING |
| `constant-headers` | management-interface=http-interface | path(S) | headers | LIST of {name(S), value(S)} |
| `mechanism-configurations` | elytron/http-authentication-factory=\* | mechanism-name(S), host-name(S), protocol(S), pre-realm-principal-transformer(S), post-realm-principal-transformer(S), final-principal-transformer(S), realm-mapper(S), credential-security-factory(S) | mechanism-realm-configurations | LIST of {realm-name(S), pre-realm-principal-transformer(S), post-realm-principal-transformer(S), final-principal-transformer(S), realm-mapper(S)} |
| `mechanism-configurations` | elytron/sasl-authentication-factory=\* | (same as above) | mechanism-realm-configurations | (same as above) |
| `permission-mappings` | elytron/simple-permission-mapper=\* | match-all(B) | principals, roles, permissions, permission-sets | principals: LIST of STRING; roles: LIST of STRING; permissions: LIST of {class-name(S), module(S), target-name(S), action(S)}; permission-sets: LIST of {permission-set(S)} |
| `possible-capabilities` | core-service=capability-registry | name(S), dynamic(B) | registration-points | LIST of STRING |
| `role-map` | elytron/mapped-role-mapper=\* | from(S) | to | LIST of STRING |
| `services` | discovery/static-provider=\* | abstract-type(S), abstract-type-authority(S), uri(S), uri-scheme-authority(S) | attributes | LIST of {name(S), value(S)} |

##### C. List\<record with nested OBJECTs\> — 1 occurrence

More complex structures with nested OBJECT sub-attributes (simple records, not free-form maps).

| Attribute | Resource(s) | Structure | Notes |
|---|---|---|---|
| `principal-query` | elytron/jdbc-realm=\* | sql(S), data-source(S), attribute-mapping(LIST of {to(S), index(I)}), clear-password-mapper(OBJ: {password-index(I)}), bcrypt-mapper(OBJ: {password-index(I), salt-index(I), iteration-count-index(I), hash-encoding(S), salt-encoding(S)}), salted-simple-digest-mapper(OBJ), simple-digest-mapper(OBJ), scram-mapper(OBJ), modular-crypt-mapper(OBJ) | All nested OBJECTs are simple records. Also has a nested LIST of simple record. |

#### Configuration — deployment-scoped (5 occurrences, LOW priority)

Deployment resources are typically read-only in the console.

| Attribute | Resource(s) | Sub-attributes |
|---|---|---|
| `content` | deployment=\* | hash, path, relative-to, archive |
| `wm-security-mapping-groups` | resource-adapters (3 resources: standalone + 2 deployment) | from, to |
| `wm-security-mapping-users` | resource-adapters (3 resources: standalone + 2 deployment) | from, to |

Note: `wm-security-mapping-groups` and `wm-security-mapping-users` each appear in 3 resources (1 standalone + 2 deployment), so they also appear in the standalone count above.

#### Runtime (18 occurrences, LOW priority — read-only display)

| Attribute | Occurrences | Scope | Sub-attributes |
|---|---|---|---|
| `timers` | 6 | deployment | schedule, info, persistent, calendar-timer, next-timeout, time-remaining |
| `rest-resource-paths` | 2 | deployment | resource-path, consumes, produces, java-method, resource-methods |
| `sub-resource-locators` | 2 | deployment | resource-class, resource-path, consumes, produces, java-method, resource-methods |
| `remove-methods` | 2 | deployment | bean-method, retain-if-exception |
| `local-certificates` | 2 | standalone | type, algorithm, format, public-key, sha-1-digest, sha-256-digest, encoded, subject, issuer, not-before, not-after, serial-number, signature-algorithm, signature, version |
| `peer-certificates` | 2 | standalone | (same as local-certificates) |
| `installed-drivers` | 1 | standalone | deployment-name, driver-module-name, module-slot, driver-class-name, driver-datasource-class-name, driver-xa-datasource-class-name, driver-major-version, driver-minor-version, jdbc-compliant, profile, driver-name, datasource-class-info |
| `loaded-providers` | 1 | standalone | name, info, version, services |
| `capabilities` | 1 | standalone | name, dynamic, scope, registration-points |
| `possible-capabilities` | 1 | standalone | name, dynamic, registration-points |

### 2. Complex/recursive OBJECTs — 7 occurrences (LOW impact)

OBJECTs with nested OBJECT or LIST sub-attributes — not `simpleRecord()`. **None are deprecated.**

Deprecated and excluded:
- `filter` on all logging handler types, logger, and root-logger (8 occurrences) — deprecated since model version 1.2.0 (`DEPRECATED_SINCE` relationship in model graph). "Use filter-spec." `filter-spec` is a simple STRING already covered by `DefaultItemProvider`.

#### Configuration — standalone (6 occurrences)

| Attribute | Occurrences | Stability | Notes |
|---|---|---|---|
| `identity-mapping` | 1 | default | Deep nesting in elytron/ldap-realm: sub-OBJECTs (`user-password-mapper`, `otp-credential-mapper`, `x509-credential-mapper`) + sub-LISTs (`attribute-mapping`, `new-identity-attributes`) |
| `jwt` | 1 | default | In elytron/token-realm: nested OBJECT (`key-map`) + LISTs (`audience`, `issuer`) |
| `attributes` (undertow) | 1 | default | 35 nested OBJECTs (access log format attributes) in undertow console-access-log |
| `any` (interface) | 1 | default | Interface selection criteria with nested LISTs (`inet-address`, `nic`, `nic-match`, `subnet-match`) |
| `not` (interface) | 1 | default | Same structure as `any` — negated interface selection criteria |
| `new-item-template` | 1 | default | In elytron/ldap-key-store: contains sub-LIST (`new-item-attributes`) |

#### Runtime (1 occurrence)

| Attribute | Occurrences | Stability | Notes |
|---|---|---|---|
| `last-gc-info` | 1 | community | In platform-mbean/garbage-collector: nested OBJECTs (`memory-usage-before-gc`, `memory-usage-after-gc`). Read-only, `community` stability. |

### Summary of uncovered attributes

| Category | Config (standalone) | Config (deployment) | Runtime | Total | Deprecated (excluded) |
|---|---|---|---|---|---|
| LIST of OBJECT | 28 | 5 | 18 | **51** | 1 (`permissions` on constant-permission-mapper) |
| Complex OBJECT | 6 | 0 | 1 | **7** | 8 (logging `filter`) |
| **Total** | **34** | **5** | **19** | **58** | **9** |

Note: The 58 non-deprecated attribute occurrences (attribute × resource) map to ~340 total attributes when counting sub-attributes within each LIST/OBJECT structure. Deprecation verified via `DEPRECATED_SINCE` relationship in the WildFly 40 model graph.

## Coverage by storage type

| Storage | Covered | Not covered | Coverage |
|---|---|---|---|
| Configuration (4,118) | ~3,909 | ~209 | **~95%** |
| Runtime (1,685) | ~1,478 | ~207 | **~88%** |

Runtime attributes are read-only, so even uncovered ones render acceptably as plain text or JSON display.
