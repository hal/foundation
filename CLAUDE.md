# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HAL Foundation is the codebase for the next major version of the HAL management console for WildFly/JBoss. It produces two editions:

- **halOP** (HAL On Premise) — successor to the current HAL console, ships with WildFly or runs standalone
- **halOS** (HAL On OpenShift) — not yet implemented

Java source is compiled to JavaScript via J2CL, bundled with Parcel, and optionally packaged in a Quarkus HTTP server for standalone deployment.

## Build Commands

```bash
# Full build with tests and checks
mvn clean verify

# Build halOP (most common during development)
mvn clean verify -P op

# Production build
mvn install -P op,prod

# Quick build (skip tests and checks)
mvn install -P quick-build

# Native binary (requires GraalVM)
mvn install -P op,prod,native -Dquarkus.native.container-build=false

# Run a single test class
mvn test -pl <module> -Dtest=<TestClassName>

# Run a single test method
mvn test -pl <module> -Dtest=<TestClassName>#<methodName>
```

## Development Mode

Two processes are needed simultaneously:

1. **J2CL watch** (Java → JS compilation): `mvn j2cl:watch -P op`
   Wait for `Build Complete: ready for browser refresh`
2. **Parcel watch** (HTML/CSS bundling): `cd op/console && npm run watch`
   Opens browser at http://localhost:1234

Java changes require manual browser refresh. HTML/CSS changes auto-reload via Parcel.

## Code Formatting & Validation

```bash
./format.sh      # Auto-format (license headers, editorconfig, imports)
./validate.sh    # Check formatting without modifying
```

Enforced by: Checkstyle (WildFly ruleset), license-maven-plugin (Apache 2.0 headers), impsort-maven-plugin (import ordering).

Style: 4-space indent, UTF-8, max line length 128, LF line endings.

## Module Structure

All code modules live under `code-parent` for shared dependency management:

| Module | Purpose |
|---|---|
| `core` | Notifications, CRUD operations, label building |
| `db` | Local database (PouchDB-based) |
| `dmr` | DMR protocol communication with WildFly domain controller |
| `environment` | Environment info and configuration |
| `event` | Event system base classes |
| `meta` | Metadata registry, statement context, resource address resolution |
| `model` | Domain-driven model classes |
| `resources` | Constants, string IDs, resource definitions |
| `task` | Task interface and repository |
| `ui` | UI formatters, model browser, filters |

Application modules:
- `op/console` — J2CL-compiled SPA (Parcel bundler, PatternFly 6)
- `op/standalone` — Quarkus HTTP server wrapping the SPA

Supporting modules: `bom` (dependency versions), `build-config` (checkstyle/license rules).

## Architecture

**Dependency Injection**: Crysknife CDI (Jakarta CDI-compatible, works with J2CL). Uses `@ApplicationScoped`, `@Inject` annotations.

**Bootstrap Flow** (`op/console`): Sequential initialization via Elemento Flow — SetLogLevel → SelectEndpoint → SingleSignOnSupport → ReadEnvironment → ReadHostNames → FindDomainController → ReadStability → LoadSettings → SetTitle.

**UI Framework**: PatternFly Java bindings over PatternFly 6 components, with Elemento for DOM abstraction.

**Data Layer**: DMR operations for WildFly management API communication. PouchDB-based local database for caching. Async/Promise-based API pattern throughout.

**Key Services**:
- `Dispatcher` — executes DMR operations against WildFly
- `MetadataRepository` — stores WildFly management model metadata
- `StatementContext` — resolves placeholders in resource addresses
- `CrudOperations` — abstract CRUD layer over DMR
- `Notifications` — event-driven notification system

## Tech Stack

- Java 21, Maven 3.9.9+ (use `./mvnw` wrapper)
- J2CL v0.23 (Java → JavaScript transpilation)
- Crysknife CDI, Elemento, PatternFly Java
- Parcel, Node.js, pnpm (frontend tooling)
- Quarkus (standalone server)
- JUnit 5 + Mockito (testing)

## Issue Tracker

Issues are tracked in JIRA: https://issues.redhat.com/projects/HAL
