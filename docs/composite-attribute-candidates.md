# Composite Attribute Candidates

Analysis of WildFly 40 management model OBJECT-type attributes that could benefit from custom VIP/FIP implementations, similar to the credential-reference composite attribute.

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
