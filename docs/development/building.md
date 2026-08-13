# Building

## Maven Profiles

The build uses Maven profiles to select editions, packaging targets, and build options. Profiles are layered across three levels: the root `pom.xml` sets J2CL and environment properties, `op/pom.xml` activates modules, and `op/console/pom.xml` runs the appropriate Vite build and assembly.

### Edition Profiles

| Profile | Purpose |
|---------|-----------------------------------------------------------------------|
| `op`    | Activates the `op/` module tree (halOP edition)                       |
| `os`    | Activates the `os/` module tree (halOS edition — not yet implemented) |

### Packaging Profiles

Each packaging profile requires `op` as a prerequisite. They are mutually exclusive for local development.

| Profile        | Purpose                                                                | J2CL Compilation           |
|----------------|------------------------------------------------------------------------|-----------------------------|
| `standalone`   | Builds the Quarkus standalone edition                                  | `ADVANCED` (production)    |
| `feature-pack` | Builds the Galleon feature pack and the WildFly subsystem              | `ADVANCED` (production)    |
| `test-suite`   | Builds the test-suite edition for [dave](https://github.com/hal/dave) | `BUNDLE_JAR` (development) |

### Modifier Profiles

These can be combined with the profiles above.

| Profile       | Purpose                                                                    |
|---------------|----------------------------------------------------------------------------|
| `format`      | Auto-formats source files (editorconfig, import sort, license headers)     |
| `check`       | Validates source files (enforcer, editorconfig, import sort, license, checkstyle) |
| `native`      | GraalVM native image build (use with `-P op,standalone,native`)            |
| `jbang`       | Uber-JAR packaging for JBang execution (use with `-P op,standalone,jbang`) |
| `quick-build` | Skips tests and npm (also activatable via `-Dquickly`)                     |
| `release`     | Source/Javadoc JARs, GPG signing, Maven Central publishing                 |

### Common Combinations

```bash
mvn verify                                  # Code modules only (no edition)
mvn compile -P op                           # Compile halOP
mvn install -P op,standalone                # Standalone edition (JVM)
mvn install -P op,standalone,native         # Standalone edition (native binary)
mvn install -P op,feature-pack              # Galleon feature pack
mvn package -P op,test-suite                # Test suite container
mvn install -P quick-build                  # Fast build, skip tests
mvn process-sources -P format,op            # Auto-format halOP sources
mvn process-sources -P check,op             # Validate halOP sources
```

## Scripts

| Script | Purpose |
|---|---|
| `format.sh` | Auto-format source files (license headers, editorconfig, import sorting) using the `format` Maven profile |
| `check.sh` | Validate source files (enforcer, checkstyle, license headers, editorconfig, import sorting) using the `check` Maven profile |
| `bump-pfj.sh` | Bump PatternFly Java to a new version (Maven property, pnpm catalog, lockfile) |
| `versionBump.sh` | Bump the project version in Maven POMs |
| `release.sh` | Create a new release (version bump, changelog, tag, push, snapshot bump) |
| `unrelease.sh` | Undo a failed release (delete tag, revert version bump) |
