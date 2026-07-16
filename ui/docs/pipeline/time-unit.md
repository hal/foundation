# Time Unit

Matches `keepalive-time` OBJECT attributes — a pattern with `time` (LONG) and `unit` (STRING with allowed values) sub-attributes.

## Status

- **Implemented**: yes
- **Matcher**: `TimeUnitMatcher`
- **Provider**: `TimeUnitProvider`
- **Priority**: —

## Attributes — 8 occurrences

| Resource | Attribute | Storage | Access | Deprecated |
|---|---|---|---|---|
| `/subsystem=ejb3/thread-pool=*` | `keepalive-time` | configuration | read-write | no |
| `/subsystem=batch-jberet/thread-pool=*` | `keepalive-time` | configuration | read-write | no |
| `/subsystem=ee/managed-executor-service=*` | `keepalive-time` | configuration | read-write | no |
| `/subsystem=ee/managed-scheduled-executor-service=*` | `keepalive-time` | configuration | read-write | no |
| `/subsystem=infinispan/cache-container=*/thread-pool=blocking` | `keepalive-time` | configuration | read-write | no |
| `/subsystem=infinispan/cache-container=*/thread-pool=non-blocking` | `keepalive-time` | configuration | read-write | no |
| `/subsystem=infinispan/cache-container=*/thread-pool=expiration` | `keepalive-time` | configuration | read-write | no |
| `/subsystem=infinispan/cache-container=*/thread-pool=listener` | `keepalive-time` | configuration | read-write | no |

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=ejb3/thread-pool=*` | `keepalive-time` | EJB3 thread pool keepalive |
| `/subsystem=batch-jberet/thread-pool=*` | `keepalive-time` | Batch thread pool keepalive |
| `/subsystem=ee/managed-executor-service=*` | `keepalive-time` | EE managed executor keepalive |
| `/subsystem=infinispan/cache-container=*/thread-pool=blocking` | `keepalive-time` | Infinispan blocking thread pool |
