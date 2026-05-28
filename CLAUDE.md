# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

HAL Foundation is the codebase for the next major version of the HAL management console for WildFly/JBoss. It produces two editions:

- **halOP** (HAL On Premise) — successor to the current HAL console, ships with WildFly or runs standalone
- **halOS** (HAL On OpenShift) — not yet implemented

Java source is compiled to JavaScript via J2CL, bundled with Vite, and optionally packaged in a Quarkus HTTP server for standalone deployment.

## Build Commands

```bash
# Full build with tests and checks
mvn clean verify

# Build halOP (most common during development)
mvn clean verify -P op

# Build standalone (production)
mvn install -P op,standalone

# Build Galleon feature pack (production)
mvn install -P op,feature-pack

# Build test suite
mvn package -P op,test-suite

# Quick build (skip tests and checks)
mvn install -P quick-build

# Native binary (requires GraalVM)
mvn install -P op,standalone,native -Dquarkus.native.container-build=false

# Run a single test class
mvn test -pl <module> -Dtest=<TestClassName>

# Run a single test method
mvn test -pl <module> -Dtest=<TestClassName>#<methodName>
```

## Maven Profiles

The build uses Maven profiles at three levels: the root `pom.xml` sets J2CL/environment **properties**, `op/pom.xml` activates **modules**, and `op/console/pom.xml` runs the right **Vite build** and **assembly**. Profile IDs are consistent across levels so a single `-P` flag cascades through all three.

### Edition Profiles

| Profile | Purpose |
|---|---|
| `op` | Activates the `op/` module tree (halOP edition) |
| `os` | Activates the `os/` module tree (halOS edition — not yet implemented) |

These are independent module activators and can be combined (the release build uses both).

### Packaging Profiles

Each requires `op` as a prerequisite. They are mutually exclusive for local development, but the release build activates all of them together.

| Profile | Purpose | J2CL Mode |
|---|---|---|
| `standalone` | Builds the Quarkus standalone server | `ADVANCED` (production) |
| `feature-pack` | Builds the Galleon feature pack + WildFly subsystem; sets `environment.base=/halop` | `ADVANCED` (production) |
| `test-suite` | Builds the test-suite Quarkus server (Docker-based) | `BUNDLE_JAR` (development) |

### Modifier Profiles

These are additive and can be combined with the packaging profiles above.

| Profile | Defined in | Purpose |
|---|---|---|
| `native` | `op/standalone/pom.xml` | GraalVM native image build (requires `-P op,standalone,native`) |
| `jbang` | `op/standalone/pom.xml` | Uber-JAR packaging for JBang execution |
| `quick-build` | root `pom.xml` | Skips all checks and tests (also activatable via `-Dquickly`) |
| `release` | root `pom.xml`, `bom/pom.xml` | Source/Javadoc JARs, GPG signing, Central publishing |

### Common Profile Combinations

```bash
mvn verify                                  # Code modules only (no edition)
mvn compile -P op                           # Compile halOP (CI verification)
mvn install -P op,standalone                # Standalone server (JVM)
mvn install -P op,standalone,native         # Standalone server (native binary)
mvn install -P op,feature-pack              # Galleon feature pack
mvn package -P op,test-suite                # Test suite container
mvn install -P quick-build                  # Fast build, skip everything
mvn deploy -P op,feature-pack,standalone,jbang,os,release  # Full release
```

## Galleon Provisioning

After building the feature pack, provision a WildFly server with the HAL console using the Galleon CLI:

```bash
galleon.sh provision op/feature-pack/target/provision.xml --dir=/path/to/wildfly
```

The `provision.xml` in `op/feature-pack/` uses `${project.version}` for the feature pack version. During the build, Maven resource filtering produces `target/provision.xml` with the resolved version. It provisions a full default WildFly standalone server and adds the `halop` layer on top.

Start the provisioned server with `--stability=experimental`.

## Development Mode

Two processes are needed simultaneously:

1. **J2CL watch** (Java → JS compilation): `mvn j2cl:watch -P op`
   Wait for `Build Complete: ready for browser refresh`
2. **Vite dev server** (HTML/CSS bundling): `cd op/console && pnpm run watch`
   Opens browser at http://localhost:1234

Java changes require manual browser refresh. HTML/CSS changes auto-reload via Vite HMR.

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
- `op/console` — J2CL-compiled SPA (Vite bundler, PatternFly 6)
- `op/standalone` — Quarkus HTTP server wrapping the SPA (production)
- `op/test-suite` — Quarkus HTTP server wrapping the SPA (test suite, Docker)
- `op/subsystem` — WildFly subsystem extension for the HAL console
- `op/feature-pack` — Galleon feature pack to provision HAL into WildFly

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
- Vite, Node.js, pnpm (frontend tooling)
- Quarkus (standalone server)
- JUnit 6 + Mockito (testing)

## Issue Tracker

Issues are tracked in JIRA: https://issues.redhat.com/projects/HAL
