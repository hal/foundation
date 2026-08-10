# HAL Foundation

This repository contains the foundation for the next major version of the HAL management console (halOP) and the upcoming
OpenShift version (halOS). To distinguish between the two editions, we introduce the following names:

- halOP: HAL on premise
- halOS: HAL on OpenShift

The work on the new consoles is in an early state and very much in progress.

## Technical Stack

- [Java 21](https://jdk.java.net/java-se-ri/21)
- [J2CL](https://github.com/google/j2cl)
- [Crysknife CDI](https://github.com/crysknife-io/crysknife)
- [Elemento](https://github.com/hal/elemento)
- [PatternFly Java](https://github.com/patternfly-java)
- [Maven](https://maven.apache.org/), [Vite](https://vite.dev/), and [PNPM](https://pnpm.io/)

## Documentation

Full documentation is available at [hal.github.io/foundation](https://hal.github.io/foundation/), covering:

- [Features](https://hal.github.io/foundation/features/overview.html) — what halOP can do today
- [Architecture](https://hal.github.io/foundation/architecture/overview.html) — how the system is built
- [Editions](https://hal.github.io/foundation/editions/halop.html) — deployment modes and getting started
- [Building](https://hal.github.io/foundation/development/building.html) — Maven profiles, build commands, and scripts

## Quick Start

```bash
# Build halOP standalone (JVM)
mvn install -P op,standalone
java -jar op/standalone/target/quarkus-app/quarkus-run.jar

# Or use JBang
jbang hal-op@hal

# Or use a container
podman run -it -p 9090:9090 quay.io/halconsole/hal-op
```

See the [halOP documentation](https://hal.github.io/foundation/editions/halop.html) for all deployment options.

## Development

```bash
# J2CL watch (terminal 1)
mvn compile j2cl:watch -P op

# Vite dev server (terminal 2)
cd op/console && pnpm run watch
```

See the [Building](https://hal.github.io/foundation/development/building.html) page for Maven profiles and build options.

## Contributing

This is an open-source project. That means that everybody can contribute. It's not hard to get started. So
start [contributing](CONTRIBUTING.md) today!

## Licenses

This project uses the following licenses:

* [Apache License 2.0](https://repository.jboss.org/licenses/apache-2.0.txt)
