# Credential Reference

Matches the `credential-reference` family of OBJECT attributes — a well-known WildFly pattern with sub-attributes `store`, `alias`, `type`, `clear-text`. Some resources use named variants (e.g., `key-credential-reference`).

## Status

- **Implemented**: yes
- **Matcher**: `CredentialReferenceMatcher`
- **Provider**: `CredentialReferenceProvider`
- **Priority**: —

## Attributes — 49 occurrences

| Resource | Attribute | Storage | Access | Deprecated |
|---|---|---|---|---|
| `/subsystem=datasources/data-source=*` | `credential-reference` | configuration | read-write | no |
| `/subsystem=datasources/xa-data-source=*` | `credential-reference`, `recovery-credential-reference` | configuration | read-write | no |
| `/subsystem=elytron/key-store=*` | `credential-reference` | configuration | read-write | no |
| `/subsystem=elytron/key-manager=*` | `credential-reference` | configuration | read-write | no |
| `/subsystem=elytron/credential-store=*` | `credential-reference` | configuration | read-write | no |
| `/subsystem=messaging-activemq/server=*/bridge=*` | `credential-reference` | configuration | read-write | no |
| `/subsystem=jgroups/stack=*/protocol=SYM_ENCRYPT` | `key-credential-reference` | configuration | read-write | no |

## Test Resources

| Resource | Attributes | Notes |
|---|---|---|
| `/subsystem=datasources/data-source=ExampleDS` | `credential-reference` | Available out of the box |
| `/subsystem=datasources/xa-data-source=*` | `credential-reference`, `recovery-credential-reference` | Two credential-references on one resource |
| `/subsystem=elytron/key-store=*` | `credential-reference` | Elytron key store credential |
| `/subsystem=elytron/key-manager=*` | `credential-reference` | Elytron key manager credential |
| `/subsystem=elytron/credential-store=*` | `credential-reference` | Credential store's own credential |
| `/subsystem=messaging-activemq/server=*/bridge=*` | `credential-reference` | Messaging bridge credential |
| `/subsystem=jgroups/stack=*/protocol=SYM_ENCRYPT` | `key-credential-reference` | Named variant |
