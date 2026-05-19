# @halconsole/ouia

Shared IDs and utilities for automated testing of the [HAL management console](https://github.com/hal/foundation) (halOP) for [WildFly](https://www.wildfly.org/).

This package is generated from the halOP Java source and provides TypeScript constants and functions that match the OUIA component IDs used in the console UI. It enables test suites to reference UI elements using stable, deterministic selectors.

## Installation

```bash
npm install @halconsole/ouia
# or
pnpm add @halconsole/ouia
```

## Usage

### Static IDs

```typescript
import { COOKIE, MAIN_ID } from "@halconsole/ouia";

// Use as Playwright OUIA selector
const main = page.locator(`[data-ouia-component-id="${MAIN_ID}"]`);
```

### Dynamic IDs

Some IDs are built from runtime values (e.g., resource names). Use the exported functions to construct them:

```typescript
import { hostServer } from "@halconsole/ouia";

const id = hostServer("primary", "server-one");
// → "primary-server-one"

const element = page.locator(`[data-ouia-component-id="${id}"]`);
```

### ID Builder

The `buildId` function is a TypeScript port of Elemento's `Id.build()`. It sanitizes and joins strings into valid HTML IDs:

```typescript
import { buildId } from "@halconsole/ouia";

buildId("my", "component", "id");
// → "my-component-id"

buildId("User Profile");
// → "user-profile"
```

## OUIA

This package is designed for use with [OUIA](https://www.patternfly.org/developer-resources/open-ui-automation) (Open UI Automation). halOP uses [PatternFly](https://www.patternfly.org/) components that support OUIA attributes:

- `data-ouia-component-type` — identifies the component kind (set automatically)
- `data-ouia-component-id` — stable instance identifier (uses IDs from this package)
- `data-ouia-safe` — signals the component is ready for interaction

To enable OUIA in the browser, set `localStorage.setItem("ouia", "true")` before running tests.

## Versioning

Package versions are aligned with halOP releases. Use the version that matches the halOP version you are testing against.

## License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
