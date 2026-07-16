# Default Item Provider

Handles all simple-type attributes: STRING (including enum dropdowns with allowed values), BOOLEAN, INT, LONG, DOUBLE, BYTES, and LIST of simple type (e.g., LIST of STRING). This is the catch-all provider that covers the vast majority of management model attributes.

## Status

- **Implemented**: yes
- **Matcher**: n/a (default fallback — no matcher needed)
- **Provider**: `DefaultItemProvider`
- **Priority**: —

## Attributes

### STRING — 2,128 occurrences

| Resource | Attribute | Notes |
|---|---|---|
| `/subsystem=datasources/data-source=ExampleDS` | `connection-url`, `jndi-name`, `driver-name` | Free-text STRING fields |
| `/subsystem=logging/console-handler=CONSOLE` | `named-formatter` | References a formatter name |
| `/subsystem=elytron/key-store=*` | `type`, `path` | Key store configuration |

### STRING with allowed values (enum dropdown)

| Resource | Attribute | Allowed values |
|---|---|---|
| `/subsystem=logging/console-handler=CONSOLE` | `level` | ALL, FINEST, FINER, TRACE, DEBUG, FINE, CONFIG, INFO, WARN, WARNING, ERROR, SEVERE, FATAL, OFF |
| `/subsystem=logging/console-handler=CONSOLE` | `target` | console, System.out, System.err |
| `/subsystem=datasources/data-source=ExampleDS` | `transaction-isolation` | TRANSACTION_NONE, TRANSACTION_READ_COMMITTED, TRANSACTION_READ_UNCOMMITTED, TRANSACTION_REPEATABLE_READ, TRANSACTION_SERIALIZABLE |
| `/subsystem=datasources/data-source=ExampleDS` | `flush-strategy` | FailingConnectionOnly, InvalidIdleConnections, IdleConnections, Gracefully, EntirePool, ... |
| `/subsystem=logging/async-handler=*` | `overflow-action` | BLOCK, DISCARD |
| `/subsystem=ejb3/strict-max-bean-instance-pool=*` | `timeout-unit` | NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS, MINUTES, HOURS, DAYS |
| `/core-service=management/access=authorization` | `provider` | simple, rbac |

### BOOLEAN — 1,380 occurrences

| Resource | Attribute | Notes |
|---|---|---|
| `/subsystem=logging/console-handler=CONSOLE` | `autoflush`, `enabled` | Standard toggles |
| `/core-service=management/access=audit/logger=audit-log` | `enabled`, `log-boot`, `log-read-only` | Three booleans on one resource |
| `/core-service=management/management-interface=http-interface` | `console-enabled` | Controls whether the console is accessible |

### INT — 946 occurrences

| Resource | Attribute | Notes |
|---|---|---|
| `/core-service=management/management-interface=http-interface` | `backlog`, `connection-high-water`, `connection-low-water`, `no-request-timeout` | Multiple INT attributes on one resource |
| `/core-service=management/service=configuration-changes` | `max-history` | Single INT, easy to verify |
| `/subsystem=elytron/secret-key-credential-store=*` | `key-size` | INT with allowed values: 128, 192, 256 |

### LONG — 638 occurrences

| Resource | Attribute | Notes |
|---|---|---|
| `/core-service=platform-mbean/type=operating-system` | `total-memory-size`, `free-memory-size`, `total-swap-space-size` | Runtime/read-only |
| `/core-service=platform-mbean/type=compilation` | `total-compilation-time` | Runtime |
| `/core-service=platform-mbean/type=class-loading` | `total-loaded-class-count`, `unloaded-class-count` | Runtime |

### DOUBLE — 36 occurrences

| Resource | Attribute | Notes |
|---|---|---|
| `/core-service=platform-mbean/type=operating-system` | `system-load-average`, `process-cpu-load`, `cpu-load` | Runtime/read-only; only 36 DOUBLE attributes in the whole model |

### BYTES — 1 occurrence

| Resource | Attribute | Notes |
|---|---|---|
| `/deployment-overlay=*/content=*` | `content` | The only BYTES attribute in the model |

### LIST of simple type — 255 occurrences

| Resource | Attribute | Storage | Access | Notes |
|---|---|---|---|---|
| `/core-service=management/management-interface=http-interface` | `allowed-origins` | configuration | read-write | Writable LIST of STRING |
| `/subsystem=messaging-activemq/connection-factory=*` | `entries`, `connectors`, `deserialization-allow-list`, `deserialization-block-list` | configuration | read-write | Multiple writable LISTs on one resource |
| `/subsystem=ejb3/cache=*` | `aliases` | configuration | read-write | Writable LIST of STRING |
| `/subsystem=elytron/aggregate-providers=*` | `providers` | configuration | read-write | Writable LIST of STRING |
| `/core-service=platform-mbean/type=runtime` | `input-arguments` | runtime | read-only | Read-only LIST of STRING |
| `/core-service=server-environment` | `permissible-stability-levels` | runtime | read-only | Read-only LIST of STRING |

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=datasources/data-source=ExampleDS` | `connection-url`, `jndi-name`, `driver-name`, `transaction-isolation`, `flush-strategy` | STRING + enum; available out of the box |
| `/subsystem=logging/console-handler=CONSOLE` | `named-formatter`, `level`, `target`, `autoflush`, `enabled` | STRING, enum, BOOLEAN on one resource |
| `/core-service=management/access=audit/logger=audit-log` | `enabled`, `log-boot`, `log-read-only` | Three booleans |
| `/core-service=management/management-interface=http-interface` | `backlog`, `connection-high-water`, `connection-low-water`, `no-request-timeout`, `console-enabled`, `allowed-origins` | INT, BOOLEAN, LIST on one resource |
| `/core-service=management/service=configuration-changes` | `max-history` | Single INT |
| `/subsystem=elytron/secret-key-credential-store=*` | `key-size` | INT with allowed values |
| `/core-service=platform-mbean/type=operating-system` | `total-memory-size`, `free-memory-size`, `system-load-average`, `process-cpu-load` | LONG + DOUBLE, runtime/read-only |
| `/core-service=platform-mbean/type=compilation` | `total-compilation-time` | Runtime LONG |
| `/core-service=platform-mbean/type=class-loading` | `total-loaded-class-count`, `unloaded-class-count` | Runtime LONGs |
| `/deployment-overlay=*/content=*` | `content` | BYTES |
| `/subsystem=messaging-activemq/connection-factory=*` | `entries`, `connectors`, `deserialization-allow-list`, `deserialization-block-list` | Multiple writable LISTs |
| `/subsystem=ejb3/cache=*` | `aliases` | Writable LIST of STRING |
| `/subsystem=elytron/aggregate-providers=*` | `providers` | Writable LIST of STRING |
| `/core-service=platform-mbean/type=runtime` | `input-arguments` | Read-only LIST of STRING |
| `/core-service=server-environment` | `permissible-stability-levels` | Read-only LIST of STRING |
