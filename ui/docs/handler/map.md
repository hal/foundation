# Map

Matches free-form key-value OBJECT attributes — OBJECTs with `value-type=STRING` that act as arbitrary key-value maps rather than structured records.

## Status

- **Implemented**: yes
- **Matcher**: `MapMatcher`
- **Provider**: `MapProvider`
- **Priority**: —

## Attributes — 222 occurrences

| Resource | Attribute | Storage | Access | Deprecated |
|---|---|---|---|---|
| `/subsystem=datasources/data-source=ExampleDS` | `valid-connection-checker-properties`, `exception-sorter-properties`, `reauth-plugin-properties`, `stale-connection-checker-properties`, `capacity-decrementer-properties`, `capacity-incrementer-properties` | configuration | read-write | no |
| `/subsystem=elytron` | `security-properties` | configuration | read-write | no |
| `/core-service=platform-mbean/type=runtime` | `system-properties` | runtime | read-only | no |
| `/subsystem=iiop-openjdk` | `properties` | configuration | read-write | no |
| `/core-service=management/process-state-listener=*` | `properties` | configuration | read-write | no |

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=datasources/data-source=ExampleDS` | `valid-connection-checker-properties`, `exception-sorter-properties`, `reauth-plugin-properties`, `stale-connection-checker-properties`, `capacity-decrementer-properties`, `capacity-incrementer-properties` | Multiple maps; available out of the box |
| `/subsystem=elytron` | `security-properties` | Subsystem-level security properties map |
| `/core-service=platform-mbean/type=runtime` | `system-properties` | Read-only JVM system properties map |
| `/subsystem=iiop-openjdk` | `properties` | IIOP ORB properties map |
| `/core-service=management/process-state-listener=*` | `properties` | Writable properties map |
