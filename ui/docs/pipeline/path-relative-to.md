# Path / Relative-To

Matches resources that have a `path` attribute paired with a sibling `relative-to` attribute. These two attributes are edited together as a unit.

## Status

- **Implemented**: yes
- **Matcher**: `PathRelativeToMatcher`
- **Provider**: `PathRelativeToProvider`
- **Priority**: —

## Attributes — 31 occurrences

| Resource | Attribute | Storage | Access | Deprecated |
|---|---|---|---|---|
| `/path=*` | `path`, `relative-to` | configuration | read-write | no |
| `/subsystem=deployment-scanner/scanner=*` | `path`, `relative-to` | configuration | read-write | no |
| `/subsystem=elytron/credential-store=*` | `path`, `relative-to` | configuration | read-write | no |
| `/subsystem=elytron/file-audit-log=*` | `path`, `relative-to` | configuration | read-write | no |
| `/subsystem=elytron/filesystem-realm=*` | `path`, `relative-to` | configuration | read-write | no |
| `/core-service=management/access=audit/file-handler=*` | `path`, `relative-to` | configuration | read-write | no |

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/path=*` | `path`, `relative-to` | Top-level paths (e.g., `jboss.server.log.dir`) |
| `/subsystem=deployment-scanner/scanner=*` | `path`, `relative-to` | Deployment scanner directory |
| `/subsystem=elytron/credential-store=*` | `path`, `relative-to` | Credential store location |
| `/subsystem=elytron/file-audit-log=*` | `path`, `relative-to` | Elytron audit log location |
| `/subsystem=elytron/filesystem-realm=*` | `path`, `relative-to` | Filesystem realm location |
| `/core-service=management/access=audit/file-handler=*` | `path`, `relative-to` | Management audit file handler |
