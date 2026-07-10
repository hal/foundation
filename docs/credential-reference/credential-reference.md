# Credential Reference

Design document for the composite attribute handling of `credential-reference` in halOP.

## Overview

A credential reference is a WildFly management model attribute that secures passwords and secrets. Instead of storing passwords in plain text in the server configuration, it allows referencing entries in a credential store — an encrypted, external password vault.

The attribute is an OBJECT with four sub-attributes:

| Sub-attribute | Type   | Description                                          |
|---------------|--------|------------------------------------------------------|
| `store`       | STRING | Name of the credential store holding the secret      |
| `alias`       | STRING | Alias identifying a specific entry within the store  |
| `clear-text`  | STRING | Plain-text password (visible in server configuration) |
| `type`        | STRING | Type of credential (rarely used)                     |

## Name Variants

The same structural pattern appears under different names across the management model (46 occurrences in WildFly 40). All variants share an identical structure and are matched by `CredentialReference.matches()` based on structure, not name.

| Variant                                | Subsystems                                  |
|----------------------------------------|---------------------------------------------|
| `credential-reference`                 | datasources, elytron, mail, messaging, undertow |
| `cluster-credential-reference`         | messaging-activemq server                   |
| `key-credential-reference`             | jgroups encryption protocols                |
| `key-password-credential-reference`    | audit syslog TLS                            |
| `keystore-password-credential-reference` | audit syslog TLS                          |
| `recovery-credential-reference`        | XA datasources, resource adapters           |
| `source-credential-reference`          | JMS bridges                                 |
| `target-credential-reference`          | JMS bridges                                 |

## Modes (Read/View)

When reading a resource, the credential reference is in one of three modes, determined by which sub-attributes have values:

| Mode              | Sub-attributes present | Meaning                                       |
|-------------------|------------------------|-----------------------------------------------|
| `STORE_REFERENCE` | `store` and/or `alias` | References a credential store entry (secure)   |
| `CLEAR_TEXT`      | only `clear-text`      | Password visible in server configuration       |
| `UNDEFINED`       | none                   | No credential configured                       |

These modes are modeled by `CredentialReference.Mode` and used by the view item provider.

## State Transitions (Form/Edit)

The form item provider must handle all transitions between modes. The auto-provision flow (writing `store` + `alias` + `clear-text` together) is a write-only concern — WildFly consumes the clear-text during the write, adds it to the credential store, and after reload only `store` + `alias` remain in `read-resource`.

### From UNDEFINED

| #  | Target          | User action                                     | Written value                                             |
|----|-----------------|--------------------------------------------------|-----------------------------------------------------------|
| 1  | CLEAR_TEXT      | Enters a plain-text password                     | `{clear-text: "secret"}`                                  |
| 2  | STORE_REFERENCE | Picks an existing store + alias                  | `{store: "x", alias: "y"}`                                |
| 3  | STORE_REFERENCE | Picks store, enters new alias + password         | `{store: "x", alias: "y", clear-text: "secret"}` (auto-provision) |

### From CLEAR_TEXT

| #  | Target          | User action                                     | Written value                                             |
|----|-----------------|--------------------------------------------------|-----------------------------------------------------------|
| 4  | CLEAR_TEXT      | Changes the password                             | `{clear-text: "newSecret"}`                               |
| 5  | STORE_REFERENCE | Switches to an existing store entry              | `{store: "x", alias: "y"}`                                |
| 6  | STORE_REFERENCE | Migrates current password into a store           | `{store: "x", alias: "y", clear-text: "secret"}` (auto-provision) |
| 7  | UNDEFINED       | Clears/undefines the credential                  | `undefine-attribute`                                      |

### From STORE_REFERENCE

| #  | Target          | User action                                     | Written value                                             |
|----|-----------------|--------------------------------------------------|-----------------------------------------------------------|
| 8  | STORE_REFERENCE | Changes store and/or alias                       | `{store: "x2", alias: "y2"}`                              |
| 9  | CLEAR_TEXT      | Switches to plain-text password                  | `{clear-text: "secret"}`                                  |
| 10 | UNDEFINED       | Clears/undefines the credential                  | `undefine-attribute`                                      |

### Auto-Provision (#3 and #6)

When all three sub-attributes are written together, WildFly:

1. Adds the `clear-text` value to the credential store under the given `alias`
2. Returns `"status" => "new-entry-added"` and `"operation-requires-reload" => true`
3. After reload, `read-resource` shows only `{store, alias}` — the clear-text is not persisted

The form should offer this as a natural extension of the store-reference mode (e.g. an optional "New password" field) rather than a separate mode.

### Credential Store vs. Alias

The `store` value references a credential store (`/subsystem=elytron/credential-store=*`). The `alias` identifies a specific entry within that store.

- **Store**: Normally the user picks from existing stores. However, halOP already supports creating dependent resources on the fly via `CapabilityReferenceSupport.newItem()` — a typeahead with a "create new" option that opens an inline add dialog/wizard. The same pattern can be used for the `store` field, allowing inline credential store creation.
- **Alias**: Can be an existing alias or a new one. When the alias is new, the user also provides the `clear-text` value, triggering WildFly's auto-provision flow.

### Alias Case Sensitivity

Credential store aliases are **case-insensitive**. The underlying Java `KeyStore` API normalizes aliases to lowercase. A credential reference may store `alias=exampleDS`, but `read-aliases` on the credential store returns `exampleds`. Both refer to the same entry. The FIP must not validate aliases case-sensitively against `read-aliases` results.

The form should:

- Use a capability-reference typeahead for `store` (with inline create support)
- Allow free-text entry for `alias` (can be existing or new)
- When `alias` is new, require the `clear-text` field to provide the password to store
- Compare aliases case-insensitively when checking for existing entries

## Implementation

### Composite Attribute Infrastructure

| Class                  | Package                           | Role                                                    |
|------------------------|-----------------------------------|---------------------------------------------------------|
| `CompositeAttribute`   | `o.j.h.ui.resource`              | `@FunctionalInterface` — structure-based predicate      |
| `CompositeAttributes`  | `o.j.h.ui.resource`              | Registry, provides `isComposite()` and shared instances  |
| `CredentialReference`  | `o.j.h.ui.resource`              | Structure matcher + `Mode` enum                         |

### Integration Points

- `ResourceAttribute.resourceAttributes()` checks `CompositeAttributes.isComposite()` to skip flattening
- `ViewItemProviders` registers a VIP using `CREDENTIAL_REFERENCE.matches()` (TODO: custom rendering)
- `FormItemProviders` registers a FIP using `CREDENTIAL_REFERENCE.matches()` (TODO: custom form item)

### View Item Provider (TODO)

The custom view should display the credential reference as a single consolidated item showing:

- **Store reference mode**: store name (clickable link to credential store resource) + alias
- **Clear text mode**: masked or plain value with a security indicator
- **Undefined**: "Not configured" in muted styling

### Form Item Provider (TODO)

The custom form should provide:

- A mode selector (clear text vs. credential store)
- Mode-specific input fields
- Auto-provision support when entering a new alias + password in store-reference mode

### Credential Store Operations (for FIP)

The FIP can use operations on `/subsystem=elytron/credential-store=*` to manage aliases:

| Operation | Parameters | Use in FIP |
|---|---|---|
| `read-aliases` | *(none)* | Populate alias typeahead/dropdown (values are lowercase) |
| `add-alias` | `alias`, `secret-value`, `entry-type` | Create a new alias explicitly (alternative to auto-provision) |
| `set-secret` | `alias`, `secret-value`, `entry-type` | Update an existing alias's password |
| `remove-alias` | `alias`, `entry-type` | Remove an alias |

The `/subsystem=elytron/secret-key-credential-store=*` also provides the `org.wildfly.security.credential-store` capability, so a credential reference's `store` value can point to either type. However, `secret-key-credential-store` manages **encryption keys**, not passwords — its operations are `generate-secret-key`, `export-secret-key`, `import-secret-key`, and `remove-alias`. It does **not** have `add-alias` or `set-secret`.

| Store type | Password management | Auto-provision support |
|---|---|---|
| `credential-store` | `add-alias`, `set-secret` | Yes — `clear-text` is stored via `add-alias` |
| `secret-key-credential-store` | Not available | No — only existing aliases can be referenced |

The FIP should:

- List both store types in the store typeahead (both provide the capability)
- Only offer the "new alias + password" auto-provision flow when the selected store is a regular `credential-store`
- When a `secret-key-credential-store` is selected, only allow referencing existing aliases (no password entry)

## References

- [WildFly Credential Store Guide](https://www.wildfly.org/guides/security-credential-store-for-passwords/)
- [JIRA: HAL Issues](https://issues.redhat.com/projects/HAL)
