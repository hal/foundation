# Relative-To (standalone)

Handles the single resource that has a `relative-to` attribute without a sibling `path` attribute.

## Status

- **Implemented**: yes
- **Matcher**: n/a (handled as special case)
- **Provider**: `RelativeToProvider`
- **Priority**: —

## Attributes — 1 occurrence

| Resource | Attribute | Storage | Access | Deprecated |
|---|---|---|---|---|
| `/subsystem=undertow/server=*/host=*/setting=access-log` | `relative-to` | configuration | read-write | no |

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=undertow/server=*/host=*/setting=access-log` | `relative-to` | The only resource with `relative-to` but no sibling `path` attribute |
