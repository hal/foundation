# Composite Attribute Candidates

Analysis of WildFly 40 management model OBJECT-type attributes that could benefit from custom VIP/FIP implementations, similar to the credential-reference composite attribute.

## Pipeline Design

### Overview

**Input:** Resource metadata (address, list of `AttributeDescription`s)
**Output:** Ordered list of view items or form items

The pipeline has three stages with clear separation of concerns. Each stage has a single responsibility and a well-defined input/output contract.

**Package:** `org.jboss.hal.ui.resource.pipeline` — built as a separate package alongside the existing code. The existing `ResourceAttribute`, `ViewItemProviders`, `FormItemProviders`, etc. remain untouched. Once the new pipeline is proven, the old code can be migrated.

### Stage 1: Claim — "Which attributes belong together?"

**Input:** `List<AttributeDescription>` (the full pool from metadata)
**Output:** `List<AttributeGroup>`

This stage is purely about grouping. It doesn't know about VIP or FIP. It doesn't flatten anything. It decides which attributes belong together and removes them from the pool.

Each `AttributeGroup` has a kind:

| Kind | Meaning | Attributes |
|---|---|---|
| `COMPOSITE` | One OBJECT attribute kept as a unit | 1 (the OBJECT attribute itself; sub-attributes are internal) |
| `SIBLING` | n top-level attributes claimed as a unit | n (e.g. `path` + `relative-to`) |
| `SINGLE` | One attribute, not claimed by any matcher | 1 |

The claim logic runs registered matchers in priority order:

1. **Composite matchers** scan the pool, claim matching OBJECT attributes (credential-reference, keepalive-time, file)
2. **Sibling group matchers** scan the remaining pool, claim groups of related attributes (path + relative-to)
3. **Everything left** becomes a `SINGLE` group

After this stage, every `AttributeDescription` belongs to exactly one `AttributeGroup`, and the pool is empty.

### Stage 2: Resolve — "How should each group be rendered?"

**Input:** `List<AttributeGroup>` from stage 1
**Output:** `List<ResolvedItem>` — each carrying its group + the chosen rendering strategy

For each group, ask registered providers: "Can you handle this?" First match wins, otherwise fall back to default rendering:

| Group kind | Provider match | Rendering strategy |
|---|---|---|
| `COMPOSITE` | Composite provider (credential-reference, keepalive-time, file) | Custom VIP/FIP for the composite |
| `SIBLING` | Sibling provider (path + relative-to) | Custom VIP/FIP for the group |
| `SINGLE` (OBJECT, simpleRecord) | No custom provider | **Flatten** into n individual items |
| `SINGLE` (special, e.g. standalone `relative-to`) | Standalone provider | Custom FIP (typeahead/dropdown) |
| `SINGLE` | No custom provider | Default type-based VIP/FIP |

**Key design decision:** Flattening moves from grouping to rendering. Currently flattening happens during grouping (`ResourceAttribute.resourceAttributes()`), but it's really a rendering strategy for "OBJECT attributes that nobody claimed." The decision "should this OBJECT be flattened?" only makes sense after we know nobody wants to render it as a unit.

### Stage 3: Build — "Create the actual UI components"

**Input:** `List<ResolvedItem>` from stage 2
**Output:** `List<ViewItem>` or `List<FormItem>`

Pure construction. Each resolved item knows its group and its provider. Call `provider.createViewItem(group)` or `provider.createFormItem(group)`.

### Data Types

```
AttributeGroup
  ├── kind: SINGLE | COMPOSITE | SIBLING
  ├── attributes: List<AttributeDescription>  // 1 for SINGLE/COMPOSITE, n for SIBLING
  └── name: String  // display name for the group

ItemProvider
  ├── matches(AttributeGroup): boolean
  ├── viewItem(AttributeGroup, context): ViewItem
  └── formItem(AttributeGroup, context): FormItem
```

### Comparison to Current Design

| Concern | Current | New pipeline |
|---|---|---|
| Flattening | Mixed with composite detection in `ResourceAttribute.resourceAttributes()` | Rendering fallback in stage 2 — only applies to unclaimed OBJECT attributes |
| Composite detection | Checked in `CompositeAttributes.isComposite()` and again in providers | Single point of truth in stage 1 claim registry |
| Sibling groups | Not supported | First-class concept in stage 1 via sibling matchers |
| `ResourceAttribute` | Wraps exactly 1 `AttributeDescription` | `AttributeGroup` wraps n — works for all cases |
| VIP/FIP registries | Separate `ViewItemProviders` and `FormItemProviders` with nearly identical structure | Unified `ItemProvider` with `viewItem()` and `formItem()` methods |
| Adding a new pattern | Requires changes across multiple classes | Add a matcher (stage 1) and a provider (stage 2) |

### Open Questions

1. **Ordering:** The pipeline should preserve attribute order from metadata. For sibling groups, the group should appear at the position of its "primary" attribute (e.g. `path` for path+relative-to). How to define "primary"? First claimed attribute? Explicitly marked by the matcher?

2. **Flattened sub-attributes:** When a simple-record OBJECT gets flattened in stage 2, the resulting items need the parent attribute name as a prefix for display (e.g. "Keepalive Time / Time"). The new pipeline needs to preserve this parent context.

3. **DMR write semantics:** Composites write one attribute. Sibling groups write n separate attributes. Flattened records write sub-paths of one attribute. The write strategy should be part of the `ItemProvider` — it knows how the group is structured and how to map form values back to DMR operations.

4. **Unified vs. separate VIP/FIP provider:** A unified `ItemProvider` reduces duplication but forces every provider to implement both `viewItem()` and `formItem()`. Default methods could fall back to standard rendering for one side. Alternative: keep them separate but share the matching logic via a common `AttributeGroup` predicate.

### Detection Priority

When multiple matchers could claim the same attribute in stage 1, priority order determines the winner. Once an attribute is claimed, it is removed from the pool and not visible to lower-priority matchers.

| Priority | Matcher | Detection | Example |
|---|---|---|---|
| 1 | **Composite** | Single OBJECT-type attribute with a known internal structure | `credential-reference`, `keepalive-time`, `file` |
| 2 | **Sibling group** | n top-level attributes that are semantically coupled, detected by scanning the attribute list | `path` + `relative-to` |
| 3 | **Standalone special** | A single attribute that deserves a custom FIP but does not participate in a composite or group | `relative-to` without a sibling `path` |
| 4 | **Default** | Everything else | Standard attribute VIP/FIP |

## Already Handled

### credential-reference family (49 occurrences, 9 name variants)

All matched by `CredentialReference.matches()` based on structure (`{store, alias, clear-text, type}`). Includes `shared-secret-reference` (9 occurrences in jgroups AUTH tokens) which has the identical structure.

| Variant | Occurrences | Resources (non-deployment) |
|---|---|---|
| `credential-reference` | 21 | datasources, elytron, mail, messaging, undertow |
| `shared-secret-reference` | 9 | `/subsystem=jgroups/stack=*/protocol=AUTH/token=*` |
| `key-credential-reference` | 9 | jgroups encryption protocols |
| `recovery-credential-reference` | 4 | XA datasources, resource adapters |
| `keystore-password-credential-reference` | 2 | audit syslog TLS |
| `key-password-credential-reference` | 1 | audit syslog TLS |
| `source-credential-reference` | 1 | JMS bridges |
| `target-credential-reference` | 1 | JMS bridges |
| `cluster-credential-reference` | 1 | messaging-activemq server |

## Recommended Candidates

### 1. `keepalive-time` — HIGH priority

**Structure:** `{time: LONG, unit: STRING}`

A duration value composed of a numeric amount and a time unit. Currently flattened into two separate fields ("Keepalive time / Time" and "Keepalive time / Unit"), losing the semantic connection.

**Allowed unit values:** `NANOSECONDS`, `MICROSECONDS`, `MILLISECONDS`, `SECONDS`, `MINUTES`, `HOURS`, `DAYS`

**VIP idea:** Show as a single consolidated value like "100 MILLISECONDS" or "10 SECONDS".

**FIP idea:** Number input + unit dropdown side by side in a single form item.

**Resources (19 occurrences):**

| Subsystem | Resources |
|---|---|
| batch-jberet | `/subsystem=batch-jberet/thread-pool=*` |
| ee | `/subsystem=ee/managed-executor-service=*`, `/subsystem=ee/managed-scheduled-executor-service=*` |
| ejb3 | `/subsystem=ejb3/thread-pool=*` |
| infinispan | `/subsystem=infinispan/cache-container=*/thread-pool=blocking`, `=expiration`, `=listener`, `=non-blocking`, `/remote-cache-container=*/thread-pool=async` |
| jca | `/subsystem=jca/distributed-workmanager=*/long-running-threads=*`, `short-running-threads=*`, `/workmanager=*/long-running-threads=*`, `short-running-threads=*`, `/workmanager=default/long-running-threads=*`, `short-running-threads=*` |
| jgroups | `/subsystem=jgroups/stack=*/transport=*/thread-pool=default` (+ TCP, TCP_NIO2, UDP variants) |

### 2. `file` — MEDIUM priority

**Structure:** `{path: STRING, relative-to: STRING}`

A filesystem path optionally relative to a named standard path. Currently flattened into two fields. The `relative-to` sub-attribute references standard paths like `jboss.server.log.dir`, `jboss.server.data.dir`, etc.

**VIP idea:** Show as a single resolved-looking path, e.g. "`server.log` relative to `jboss.server.log.dir`" or just "`path` / `relative-to`" inline.

**FIP idea:** Text input for path + dropdown of standard paths for `relative-to`.

**Resources (8 occurrences, all in logging subsystem):**

- `/subsystem=logging/file-handler=*`
- `/subsystem=logging/periodic-rotating-file-handler=*`
- `/subsystem=logging/periodic-size-rotating-file-handler=*`
- `/subsystem=logging/size-rotating-file-handler=*`
- `/subsystem=logging/logging-profile=*/file-handler=*`
- `/subsystem=logging/logging-profile=*/periodic-rotating-file-handler=*`
- `/subsystem=logging/logging-profile=*/periodic-size-rotating-file-handler=*`
- `/subsystem=logging/logging-profile=*/size-rotating-file-handler=*`

### 3. `relative-to` — HIGH priority (FIP only)

**Structure:** Simple `STRING` attribute (not a composite — no VIP needed)

A reference to a well-known/standard path or a user-defined path from `/path=*`. The value is used to resolve a sibling `path` attribute as relative to the named path. All attributes referencing this pattern are named with a `relative-to` suffix.

**Relationship to candidate #4:** This section documents the `relative-to` attribute in isolation — its FIP (typeahead/dropdown), expression support, and capability references. Candidate #4 documents the **sibling pair pattern** where `relative-to` is combined with its companion `path` attribute into a single visual/editing unit. The two are complementary: the `relative-to` FIP described here would be used as one half of the sibling pair FIP in candidate #4.

**Standard path values:** `jboss.home.dir`, `jboss.server.base.dir`, `jboss.server.data.dir`, `jboss.server.log.dir`, `jboss.server.temp.dir`, `jboss.server.config.dir`, `jboss.controller.temp.dir`

**FIP idea:** Typeahead/dropdown populated from `/path=*` children (user-defined paths) plus the well-known standard paths listed above. Similar approach to how credential-reference uses a capability-reference typeahead for credential stores. For the 4 attributes that allow expressions, the FIP needs a fallback to free-text input for `${...}` expression syntax.

**Matching logic:** Attribute name ends with `relative-to` (covers `relative-to`, `keystore-relative-to`, `object-store-relative-to`).

**Attribute name variants:**

| Name | Occurrences (non-deprecated) |
|---|---|
| `relative-to` | 24 |
| `keystore-relative-to` | 2 |
| `object-store-relative-to` | 1 |

**Expression support and capability references:**

Out of 32 total occurrences, 28 do **not** allow expressions — a dropdown/typeahead FIP works cleanly for the common case. Only 7 have an explicit `org.wildfly.management.path` capability reference. The expressions-allowed and capability-reference features are mutually exclusive across all occurrences.

| Attribute | Resource | Expressions | Capability |
|---|---|---|---|
| `keystore-relative-to` | `core-service=management/.../client-certificate-store` | no | — |
| `keystore-relative-to` | `core-service=management/.../truststore` | no | — |
| `object-store-relative-to` | `subsystem=transactions` | **yes** | — |
| `relative-to` | `core-service=management/.../file-handler=*` | no | — |
| `relative-to` | `core-service=management/.../periodic-rotating-file-handler=*` | no | — |
| `relative-to` | `core-service=management/.../size-rotating-file-handler=*` | no | — |
| `relative-to` | `/path=*` | no | `org.wildfly.management.path` |
| `relative-to` | `subsystem=deployment-scanner/scanner=*` | no | `org.wildfly.management.path` |
| `relative-to` | `subsystem=ee/global-directory=*` | no | — |
| `relative-to` | `subsystem=ejb3/file-passivation-store=*` | **yes** | — |
| `relative-to` | `subsystem=ejb3/.../file-data-store=*` | no | — |
| `relative-to` | `subsystem=elytron/credential-store=*` | no | — |
| `relative-to` | `subsystem=elytron/file-audit-log=*` | no | — |
| `relative-to` | `subsystem=elytron/filesystem-realm=*` | no | — |
| `relative-to` | `subsystem=elytron/jaas-realm=*` | no | — |
| `relative-to` | `subsystem=elytron/kerberos-security-factory=*` | no | — |
| `relative-to` | `subsystem=elytron/key-store=*` | no | — |
| `relative-to` | `subsystem=elytron/periodic-rotating-file-audit-log=*` | no | — |
| `relative-to` | `subsystem=elytron/provider-loader=*` | no | — |
| `relative-to` | `subsystem=elytron/secret-key-credential-store=*` | no | — |
| `relative-to` | `subsystem=elytron/size-rotating-file-audit-log=*` | no | — |
| `relative-to` | `subsystem=infinispan/.../distributed-cache/.../store=file` | no | `org.wildfly.management.path` |
| `relative-to` | `subsystem=infinispan/.../invalidation-cache/.../store=file` | no | `org.wildfly.management.path` |
| `relative-to` | `subsystem=infinispan/.../local-cache/.../store=file` | no | `org.wildfly.management.path` |
| `relative-to` | `subsystem=infinispan/.../replicated-cache/.../store=file` | no | `org.wildfly.management.path` |
| `relative-to` | `subsystem=infinispan/.../scattered-cache/.../store=file` | no | `org.wildfly.management.path` |
| `relative-to` | `subsystem=messaging-activemq/.../path=bindings-directory` | no | — |
| `relative-to` | `subsystem=messaging-activemq/.../path=journal-directory` | no | — |
| `relative-to` | `subsystem=messaging-activemq/.../path=large-messages-directory` | no | — |
| `relative-to` | `subsystem=messaging-activemq/.../path=paging-directory` | no | — |
| `relative-to` | `subsystem=undertow/.../setting=access-log` | **yes** | — |
| `relative-to` | `subsystem=undertow/.../setting=persistent-sessions` | **yes** | — |

**Resources (27 non-deprecated occurrences across 10 subsystems):**

| Subsystem | Resources |
|---|---|
| elytron | credential-store, key-store, filesystem-realm, file-audit-log, periodic-rotating-file-audit-log, size-rotating-file-audit-log, jaas-realm, kerberos-security-factory, provider-loader, secret-key-credential-store |
| messaging-activemq | `server=*/path=bindings-directory`, `journal-directory`, `large-messages-directory`, `paging-directory` |
| core-service (audit) | `file-handler=*`, `periodic-rotating-file-handler=*`, `size-rotating-file-handler=*` |
| logging | 8 file handlers (as sub-attribute of composite `file` object) |
| path | `/path=*` |
| deployment-scanner | `scanner=*` |
| ee | `global-directory=*` |
| ejb3 | `service=timer-service/file-data-store=*` |
| transactions | (as `object-store-relative-to`) |
| undertow | `server=*/host=*/setting=access-log`, `servlet-container=*/setting=persistent-sessions` |
| core-service (audit TLS) | 2x `keystore-relative-to` on syslog TLS authentication |

### 4. `path` + `relative-to` sibling group — HIGH priority

**Structure:** Two separate STRING attributes at the resource level that are semantically coupled: a `path` attribute and a `*relative-to` attribute that references a well-known or user-defined path.

This is the first instance of the **sibling group** concept described in the pipeline design above. Unlike composite attributes (a single OBJECT with nested sub-attributes), a sibling group claims n top-level attributes and renders them as one unit. The path+relative-to group always claims exactly 2 attributes, but the mechanism supports any n.

This is also the inverse of the composite `file` attribute (candidate #2): same semantic concept (path + relative-to), different level (top-level attributes vs. nested sub-attributes inside an OBJECT). The VIP/FIP could share the same UI component.

**Sibling group matcher logic:**

```
For each attribute A ending with "relative-to":
  prefix = A.name without the "relative-to" suffix  // e.g. "keystore-", "object-store-", or ""
  siblingPath = prefix + "path"
  if resource has attribute named siblingPath:
    → claim [siblingPath, A.name]
  else if prefix == "" and resource has "directory":
    → claim ["directory", A.name]  (undertow access-log exception)
```

**VIP idea:** Show as a single resolved-looking value, e.g. `"server.log" relative to "jboss.server.log.dir"` or just `"/opt/data"` when relative-to is undefined.

**FIP idea:** Text input for path + typeahead/dropdown for relative-to, rendered side by side as a single logical form group. Reads and writes two separate attributes but presents them as one unit.

**Naming variants:**

| Path Attr | Relative-To Attr | Occurrences |
|---|---|---|
| `path` | `relative-to` | 27 |
| `keystore-path` | `keystore-relative-to` | 2 |
| `object-store-path` | `object-store-relative-to` | 1 |
| `directory` | `relative-to` | 1 |

**Coverage:** 31 out of 32 standalone `*relative-to` occurrences have a sibling path attribute. The single exception (`ejb3/file-passivation-store=*`) has a deprecated `relative-to` attribute with no direct path sibling — it can be ignored.

**Resources (31 sibling groups across 11 subsystems):**

| Subsystem | Resources | Group |
|---|---|---|
| path | `/path=*` | `path` + `relative-to` |
| core-service (audit) | `file-handler=*`, `periodic-rotating-file-handler=*`, `size-rotating-file-handler=*` | `path` + `relative-to` |
| core-service (audit TLS) | `client-certificate-store`, `truststore` | `keystore-path` + `keystore-relative-to` |
| deployment-scanner | `scanner=*` | `path` + `relative-to` |
| ee | `global-directory=*` | `path` + `relative-to` |
| ejb3 | `service=timer-service/file-data-store=*` | `path` + `relative-to` |
| elytron | credential-store, key-store, filesystem-realm, file-audit-log, periodic-rotating-file-audit-log, size-rotating-file-audit-log, jaas-realm, kerberos-security-factory, provider-loader, secret-key-credential-store | `path` + `relative-to` |
| infinispan | 5x `cache-container=*/.../store=file` | `path` + `relative-to` |
| messaging-activemq | `server=*/path=bindings-directory`, `journal-directory`, `large-messages-directory`, `paging-directory` | `path` + `relative-to` |
| transactions | (root subsystem) | `object-store-path` + `object-store-relative-to` |
| undertow | `server=*/host=*/setting=access-log` | `directory` + `relative-to` |
| undertow | `servlet-container=*/setting=persistent-sessions` | `path` + `relative-to` |

## Not Recommended (for now)

### `filter` (logging) — LOW priority, HIGH complexity

**Structure:** Complex recursive OBJECT with 10 sub-attributes, some of which are nested OBJECTs themselves (`replace`, `not`, `any`, `all`, `level-range`). Not a `simpleRecord`.

**Resources (8):** logging handlers and loggers (`console-handler`, `file-handler`, `async-handler`, `custom-handler`, `logger`, `root-logger`, etc.)

**Why not:** The recursive tree structure (filters within filters) would need a builder/tree UI, far more complex than a composite attribute form item. The effort-to-value ratio is poor.

### `schedule` (EJB timers) — LOW priority

**Structure:** `{second, minute, hour, day-of-week, day-of-month, month, year, timezone, start, end}` — a cron-like schedule specification.

**Resources:** Only deployment-scoped EJB timer resources (no non-deployment occurrences). These are runtime/read-only — users don't edit them.

**Why not:** Read-only runtime attributes on deployment resources. A cron-like VIP display would be nice but low impact.

### `*-column` (Infinispan JDBC) — LOW priority

**Structure:** `{name: STRING, type: STRING}` — a database column definition.

**Attributes:** `id-column`, `data-column`, `segment-column`, `timestamp-column` (4 attributes x 5 cache types = 20 occurrences).

**Resources:** All under `/subsystem=infinispan/cache-container=*/*/store=jdbc/table=string`.

**Why not:** Simple two-field pair that works fine when flattened. Limited to one specific resource type.

### `properties` / `configuration` / `params` — DIFFERENT pattern

**Structure:** Generic `OBJECT` (free-form key-value map, not a structured record).

**Occurrences:** `properties` (105), `configuration` (12), `params` (13).

**Why not:** These are not structured records — they're arbitrary key-value maps. They need a key-value editor, not a composite attribute handler. A different extension point (map editor FIP) would be more appropriate.
