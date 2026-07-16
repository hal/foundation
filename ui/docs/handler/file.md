# File

Matches the `file` OBJECT attribute on logging handlers — an OBJECT with `path` (STRING) and `relative-to` (STRING) sub-attributes.

## Status

- **Implemented**: yes
- **Matcher**: `FileMatcher`
- **Provider**: `FileProvider`
- **Priority**: —

## Attributes — 8 occurrences

| Resource | Attribute | Storage | Access | Deprecated |
|---|---|---|---|---|
| `/subsystem=logging/file-handler=*` | `file` | configuration | read-write | no |
| `/subsystem=logging/periodic-rotating-file-handler=*` | `file` | configuration | read-write | no |
| `/subsystem=logging/size-rotating-file-handler=*` | `file` | configuration | read-write | no |
| `/subsystem=logging/periodic-size-rotating-file-handler=*` | `file` | configuration | read-write | no |

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=logging/file-handler=*` | `file` | OBJECT with `path` + `relative-to` sub-attributes |
| `/subsystem=logging/periodic-rotating-file-handler=FILE` | `file` | Available in default config; also has `suffix` STRING |
| `/subsystem=logging/size-rotating-file-handler=*` | `file` | Also has `rotate-size`, `max-backup-index` |
| `/subsystem=logging/periodic-size-rotating-file-handler=*` | `file` | Combined rotation handler |
