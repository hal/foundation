# Test Automation

The console is designed to support automated testing through stable, predictable UI component identifiers and a companion NPM package for test suite development.

## OUIA Component IDs

All major UI components in the console are tagged with **OUIA (Open UI Automation) component IDs**. These identifiers provide stable, automation-friendly selectors that do not change when the internal DOM structure or CSS classes are refactored. This ensures that test suites remain reliable across console updates.

## @halconsole/ouia NPM Package

The **@halconsole/ouia** NPM package provides TypeScript constants and an ID builder function for use in Playwright and other browser automation test suites. This package ensures that your test code uses the same identifiers as the console itself, reducing the risk of selector mismatches and making tests easier to write and maintain.

By using the constants and builder function from the package, your test suite automatically stays in sync with the console's OUIA schema, even as new components are added or existing ones are refactored.
