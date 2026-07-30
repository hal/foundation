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

## Modes

A `credential-reference` OBJECT has four sub-attributes: `store`, `alias`, `clear-text`, `type`. The `CredentialReferenceProvider.mode()` method derives three modes from the current value:

| Mode | Condition | Meaning |
|---|---|---|
| `STORE_REFERENCE` | `store` and/or `alias` defined | References an entry in a credential store |
| `CLEAR_TEXT` | only `clear-text` defined | Password visible in the management model/XML config |
| `UNDEFINED` | nothing set | Credential reference is empty |

## Credential Store Operations

The credential store resource (`/subsystem=elytron/credential-store=*`) provides these runtime operations relevant to the form:

| Operation | Purpose |
|---|---|
| `read-aliases` | List all aliases in a store |
| `add-alias(alias, secret-value)` | Create a new alias with a password |
| `set-secret(alias, secret-value)` | Update an existing alias's secret |
| `remove-alias(alias)` | Delete an alias |

Both `credential-store=*` and `secret-key-credential-store=*` provide the `org.wildfly.security.credential-store` capability.

See also https://docs.wildfly.org/41/WildFly_Elytron_Security.html#referencing-credentials

## Form Use Cases (STORE_REFERENCE Mode)

When the credential reference is in STORE_REFERENCE mode, the form must cover these use cases:

| Use case | store | alias | clear-text | type | Effect |
|---|---|---|---|---|---|
| No change | same | same | empty | same | no-op |
| Change store/alias | new | new | empty | — | Points to different existing entry |
| Create/rotate secret | any | any | **filled** | — | Server writes secret into store, then references it |
| Change type | same | same | empty | new | Changes credential type |

### The clear-text "upsert" shorthand

When `store` + `alias` + `clear-text` are all set in a `write-attribute`, WildFly interprets this as: "write the `clear-text` value into the credential store under that alias, then use the store reference." The `clear-text` value is consumed and not persisted in the resource's config — it's a one-shot mechanism. This covers:

- **Creating a brand-new alias** in an existing store
- **Rotating the secret** for an existing alias

### Store change edge case

When the user changes the store, the old alias may not exist in the new store. The user must either:

1. Change the alias to one that exists in the new store
2. Provide a password — the clear-text upsert creates the alias in the new store on the fly

Without either, the server rejects the `write-attribute` because it can't resolve the alias.

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
