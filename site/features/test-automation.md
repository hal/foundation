# Test Automation

The console is designed to support automated testing through stable, predictable UI component identifiers. The companion project [dave](https://github.com/hal/dave) is the end-to-end test suite built with [Playwright](https://playwright.dev/) and TypeScript.

## OUIA Component IDs

All major UI components in the console are tagged with [OUIA (Open UI Automation)](https://ouia.readthedocs.io/) component IDs, following [PatternFly's testing conventions](https://www.patternfly.org/developer-resources/open-ui-automation). These identifiers provide stable, automation-friendly selectors that do not change when the internal DOM structure or CSS classes are refactored, ensuring that test suites remain reliable across console updates.

The OUIA IDs are defined in [`OuiaIds.java`](https://github.com/hal/foundation/blob/main/resources/src/main/java/org/jboss/hal/resources/OuiaIds.java) in this repository.

## Sync with dave

The dave test suite generates TypeScript constants from `OuiaIds.java` into [`src/selectors/ids.ts`](https://github.com/hal/dave/blob/main/src/selectors/ids.ts) using `pnpm sync:ouia`. This keeps test selectors in sync with the console without requiring a separate package release. A CI check (`pnpm sync:ci`) detects drift and fails if the generated file is out of date.

See the [dave documentation](https://hal.github.io/dave/) for details on the test suite architecture and sync workflow.
