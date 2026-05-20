# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- Fix release workflow

## [0.3.5] - 2026-05-20

### Changed

- Switch npm publishing to OIDC Trusted Publishing, removing the need for stored NPM tokens

### Fixed

- Fix Windows native binary build by skipping editorconfig check (CRLF/LF mismatch on Windows runners)

## [0.3.4] - 2026-05-20

### Added

- Add `OuiaSupport` interface to 10 custom HAL components (Skeleton, ErrorSkeleton, StabilityBanner, EndpointTable, EndpointForm, EndpointSelector, ModelBrowserTree, ModelBrowserDetail, ResourceToolbar, ResourceList) adding `data-ouia-component-type` attributes with `HalOP/<Name>` naming convention
- Add `op/ouia` module: NPM package `@halconsole/ouia` that generates TypeScript constants and functions from `Ids.java` for OUIA-based test automation
- Add OUIA documentation (`OUIA.md`) with component inventory, implementation plan, and ID sharing strategy
- Add npm publish step to release workflow for automated `@halconsole/ouia` publishing
- Add comprehensive API documentation (package-info, class-level, and method-level Javadoc with `@snippet` examples) across all code modules
- Add CycloneDX SBOM generation for NPM runtime dependencies in assembly JARs
- Add CI workflow to build and publish a JVM-mode test-suite container image (`quay.io/halconsole/hal-op:test-suite`) on every push to main
- Add `test-suite` Maven profile for test-suite builds with sourcemaps and readable JavaScript
- Add `TEST_SUITE` build type to `BuildType` enum
- Add dedicated `op/test-suite` Quarkus module for test-suite Docker images

### Changed

- Migrate to PatternFly Java 0.7.x
- Refactor AddressTemplate API to distinguish trusted (pre-encoded) and safe (auto-encoding) methods following SafeHtmlUtils naming conventions
- Update format and validate scripts to include feature-pack and standalone modules
- Separate build variants into self-contained Maven profiles: `-P op,feature-pack`, `-P op,standalone`, `-P op,test-suite` (remove `prod` profile)
- Rename pnpm scripts from `prod:*` to `build:*` for consistency with Maven profiles
- Move shared Quarkus config to `pluginManagement` in `op/pom.xml` to reduce duplication between standalone and test-suite modules

### Fixed

- Fix `@halconsole/ouia` package exports: re-export all OUIA ID constants and `ouia()` function from package root
- Remove unused imports across multiple modules
- Fix resource toolbar and update documentation links
- Fix tooltip cleanup when switching between expression and native mode in form items
- Fix capability reference value selection for async typeahead items
- Fix license header exclude pattern for generated `ids.ts` in the OUIA module that caused release builds to fail

### Upgrades

- Bump PatternFly Java to 0.8.0
- Bump Elemento to 2.4.10
- Bump Quarkus to 3.35.2
- Bump JBoss Parent to 53
- Bump J2CL Maven Plugin to 0.23.7
- Bump Wildfly Galleon Maven Plugin to 8.1.3.Final

## [0.3.3] - 2026-05-20

> [!WARNING]
> Not an official release. Please don't use!

## [0.3.2] - 2026-05-20

> [!WARNING]
> Not an official release. Please don't use!

## [0.3.1] - 2026-05-20

> [!WARNING]
> Not an official release. Please don't use!

## [0.3.0] - 2026-05-20

> [!WARNING]
> Not an official release. Please don't use!

## [0.2.7] - 2026-03-25

### Added

- Add configuration finder (still wip!)

### Upgrades

- Bump to PatternFly Java 0.6.15

## [0.2.6] - 2026-03-09

### Fixed

- Fix native binary build for Windows

## [0.2.5] - 2026-03-06

### Fixed

- Fix native binary build for Windows

## [0.2.4] - 2026-03-06

### Fixed

- Fix native binary build for Windows

## [0.2.3] - 2026-03-06

### Added

- Automatically update JBang catalog during release

### Fixed

- Fix native binary build for Windows

## [0.2.2] - 2026-03-06

### Added

- Add Galleon feature pack
- Add JBang integration

## [0.2.1] - 2026-03-05

> [!WARNING]
> Not an official release. Please don't use!

## [0.2.0] - 2026-03-05

> [!WARNING]
> Not an official release. Please don't use!

## [0.1.2] - 2026-02-23

### Fixed

- Fix multi-arch container build
- Fix native image build

## [0.1.1] - 2026-02-23

### Fixed

- Fix filtered resource selection in the statistics task

## [0.1.0] - 2026-02-21

### Added

- Statistics enabled task

### Upgrades

- Bump JBoss Parent to 52
- Bump to Elemento 2.4.9
- Bump to PatternFly Java 0.5.0

## [0.0.13] - 2026-02-05

### Changed

- Tweak native image build

## [0.0.12] - 2026-02-05

### Changed

- Tweak native image build

## [0.0.11] - 2026-02-05

### Fixed

- Fix native binary and image build (#190)

## [0.0.10] - 2026-02-05

### Added

- Start the statistics enabled task (not fully functional yet)
- Start the logging task (not fully functional yet)
- Add support for multi-typeahead components
- Add support to create dependent resources in typeahead components on the fly

### Changed

- Add `autocomplete=off` to all form inputs

### Fixed

- Enable deep SPA links to work in halOP standalone.

### Upgrades

- Bump Elemento to 2.4.8
- Bump PatternFly Java to 0.4.16

## [0.0.9] - 2025-12-18

### Fixed

- Fix stability bootstrap check
- Fix config file value on the dashboard

## [0.0.8] - 2025-12-18

### Added

- Add tasks demo page

### Changed

- Enhanced dashboard

### Upgrades

- Bump PatternFly Java to 0.4.3

## [0.0.7] - 2025-12-16

### Changed

- Enhanced dashboard

### Fixed

- Fix standalone image build

### ### Upgrades

- Bump PatternFly Java to 0.4.1

## [0.0.6] - 2025-11-20

### Added

- Resizable model browser
- Theme and contrast selector
- Endpoint selector
- Notification system (badge and drawer)

### ### Upgrades

- Bump Elemento to 2.4.1
- Bump PatternFly Java to 0.3.1

## [0.0.5] - 2025-11-09

### Changed

- Change image name for the halOP console to quay.io/halconsole/hal-op

### Removed

- Remove flag to open the browser for the halOP console (does not work nicely with native binaries)

## [0.0.4] - 2025-11-09

### Changed

- Optimize release workflow

## [0.0.3] - 2025-11-09

### Fixed

- Fix release workflow

## [0.0.2] - 2025-11-08

Test release w/o major changes.

## [0.0.1] - 2025-11-08

Initial release.

<!--
## Template

### Added

- for new features

### Changed

- for changes in existing functionality

### Fixed

- for any bug fixes

### Security

- in case of vulnerabilities

### Deprecated

- for soon-to-be removed features

### Removed

- for now removed features

### Upgrades

- for dependency upgrades
-->
[Unreleased]: https://github.com/hal/foundation/compare/v0.3.5...HEAD
[0.3.5]: https://github.com/hal/foundation/compare/v0.3.4...v0.3.5
[0.3.4]: https://github.com/hal/foundation/compare/v0.3.3...v0.3.4
[0.3.3]: https://github.com/hal/foundation/compare/v0.3.2...v0.3.3
[0.3.2]: https://github.com/hal/foundation/compare/v0.3.1...v0.3.2
[0.3.1]: https://github.com/hal/foundation/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/hal/foundation/compare/v0.2.7...v0.3.0
[0.2.7]: https://github.com/hal/foundation/compare/v0.2.6...v0.2.7
[0.2.6]: https://github.com/hal/foundation/compare/v0.2.5...v0.2.6
[0.2.5]: https://github.com/hal/foundation/compare/v0.2.4...v0.2.5
[0.2.4]: https://github.com/hal/foundation/compare/v0.2.3...v0.2.4
[0.2.3]: https://github.com/hal/foundation/compare/v0.2.2...v0.2.3
[0.2.2]: https://github.com/hal/foundation/compare/v0.2.1...v0.2.2
[0.2.1]: https://github.com/hal/foundation/compare/v0.2.0...v0.2.1
[0.2.0]: https://github.com/hal/foundation/compare/v0.1.2...v0.2.0
[0.1.2]: https://github.com/hal/foundation/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/hal/foundation/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/hal/foundation/compare/v0.0.13...v0.1.0
[0.0.13]: https://github.com/hal/foundation/compare/v0.0.12...v0.0.13
[0.0.12]: https://github.com/hal/foundation/compare/v0.0.11...v0.0.12
[0.0.11]: https://github.com/hal/foundation/compare/v0.0.10...v0.0.11
[0.0.10]: https://github.com/hal/foundation/compare/v0.0.9...v0.0.10
[0.0.9]: https://github.com/hal/foundation/compare/v0.0.8...v0.0.9
[0.0.8]: https://github.com/hal/foundation/compare/v0.0.7...v0.0.8
[0.0.7]: https://github.com/hal/foundation/compare/v0.0.6...v0.0.7
[0.0.6]: https://github.com/hal/foundation/compare/v0.0.5...v0.0.6
[0.0.5]: https://github.com/hal/foundation/compare/v0.0.4...v0.0.5
[0.0.4]: https://github.com/hal/foundation/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/hal/foundation/compare/v0.0.2...v0.0.3
[0.0.2]: https://github.com/hal/foundation/compare/v0.0.1...v0.0.2
[0.0.1]: https://github.com/hal/foundation/compare/vTemplate...v0.0.1
