# Stability Level Highlighting

WildFly's management model assigns a stability level to every resource, attribute, operation, operation parameter, and capability. The console uses these levels to visually highlight items that are less stable than what the server is configured to run at, so administrators can see at a glance which parts of the configuration are experimental or preview.

## Stability Levels

WildFly defines four stability levels, ordered from most stable to least stable:

| Level | Description |
|---|---|
| **default** | Features that are part of the standard supported API |
| **community** | Community-contributed features not yet part of the supported API |
| **preview** | Features available for evaluation that may change or be removed |
| **experimental** | Features under active development; not recommended for production use |

## Highlighting Rules

The console applies two kinds of stability highlights: a server-level banner and per-item labels.

### Server-Level Banner

When the connected server's stability level is higher than the stability level the console was built with, a banner appears at the top of the page. This warns administrators that the server is running with features beyond what the console was designed for.

For example, if the console was compiled with `community` stability (the default) and the server is started with `--stability=preview`, the banner appears. If the server runs at `community` or `default`, no banner is shown.

### Per-Item Labels

Individual resources, attributes, operations, operation parameters, and capabilities are highlighted with a stability label when **all** of the following conditions are met:

1. The item's stability level is **not null**
2. The item's stability level is **above community** — only `preview` and `experimental` items are ever highlighted; `default` and `community` items never receive a label
3. The item's stability level is **at least as high as the server's stability level** (order ≥ server order)

Items at `default` or `community` stability are considered part of the baseline and are never highlighted, regardless of how the server is configured.

### Nested Items

Management model elements are hierarchical: an attribute belongs to a resource, an operation parameter belongs to an operation which belongs to a resource, and so on. When checking whether a nested item should be highlighted, the console evaluates the **entire chain** from outermost to innermost.

The method signature reflects this hierarchy:

```java
highlightStability(resourceStability)
highlightStability(resourceStability, attributeStability)
highlightStability(resourceStability, operationStability)
highlightStability(resourceStability, operationStability, parameterStability)
highlightStability(resourceStability, capabilityStability)
```

For a nested item to be highlighted, **the last item in the chain** must satisfy all three conditions above, checked against every preceding level in the chain (including the server stability). Concretely, the innermost item is highlighted only if its stability order is greater than `community` **and** greater than or equal to the order of **every** level that comes before it (server, resource, operation, etc.).

## Real-World Examples

The following examples use real resources and attributes from WildFly 41.

### Example 1: Community Resource — Jakarta Data

The `/subsystem=jakarta-data` resource has `community` stability. Its `add` operation is also at `community` stability.

On a server running at `community` stability (the default for WildFly):

- **Resource header**: no label (`community` is at the threshold — never highlighted)
- **Operation `add`**: no label (`community` ≤ threshold)

`community` items are considered part of the baseline and are never highlighted, regardless of how the server is configured. The same applies to all `default` items.

### Example 2: Preview Resource — Unstable API Annotations

The `/subsystem=core-management/service=unstable-api-annotations` resource has `preview` stability. Its `add` operation is also at `preview` stability, while its `level` attribute is at `default` stability.

On a server running at `community` stability:

- **Resource header**: shows a `preview` label (`preview` > `community` **and** `preview` ≥ `community`)
- **Operation `add`**: shows a `preview` label (`preview` > `community` **and** `preview` ≥ `preview`)
- **Attribute `level`**: no label (`default` ≤ `community` threshold — never highlighted)

The resource and its preview operation are highlighted, signaling that this is a preview feature. But the `level` attribute inside it has `default` stability, so it blends in — it is the stable part of an otherwise preview resource.

### Example 3: Community Attribute on a Default Resource

The `/core-service=management/management-interface=http-interface` resource has `default` stability. Several of its attributes — `backlog`, `connection-high-water`, `connection-low-water` — have `community` stability, while most other attributes like `console-enabled` and `socket-binding` are at `default`.

On a server running at `community` stability:

- **Resource header**: no label (`default` ≤ threshold)
- **Attribute `backlog`**: no label (`community` ≤ threshold)
- **Attribute `console-enabled`**: no label (`default` ≤ threshold)

Even though the `community` attributes are technically less stable than the `default` resource, `community` is at the threshold and is never highlighted.

### Example 4: Community Operation on the Root Resource

The root resource `/` has `default` stability. The `reload-enhanced` operation on this resource has `community` stability.

On any server:

- **Operation `reload-enhanced`**: no label (`community` ≤ threshold — never highlighted regardless of server stability)

### Example 5: Preview Resource on an Experimental Server

The `/subsystem=core-management/service=unstable-api-annotations` resource has `preview` stability, with its `add` operation at `preview`.

On a server running at `experimental` stability:

- **Resource header**: no label (`preview` > `community`, but `preview` < `experimental` — the resource is more stable than the server)
- **Operation `add`**: no label (`preview` < `experimental`)

When the server runs at the highest instability level, only `experimental` items stand out. Everything at `preview` or below is comparatively stable and does not need a warning.

### Example 6: Community Attribute in Elytron OIDC

The `/subsystem=elytron-oidc-client/secure-deployment=*` resource has `default` stability. Its `authentication-request-format` attribute has `community` stability, while most other attributes like `client-id` and `provider-url` are at `default`.

On a server running at `community` stability:

Call: `highlightStability(default, community)`

- **Resource header**: no label (`default` ≤ threshold)
- **Attribute `authentication-request-format`**: no label (`community` ≤ threshold)
- **Attribute `client-id`**: no label (`default` ≤ threshold)

This illustrates that `community`-level attributes inside a `default` resource are still never highlighted — the `community` threshold filters them out before the chain comparison even matters.

## Summary Table

The following table shows whether highlighting occurs for each combination of server stability and item stability (for a single, non-nested item):

| | Item: default | Item: community | Item: preview | Item: experimental |
|---|---|---|---|---|
| **Server: default** | — | — | highlight | highlight |
| **Server: community** | — | — | highlight | highlight |
| **Server: preview** | — | — | highlight | highlight |
| **Server: experimental** | — | — | — | highlight |
