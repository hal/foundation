# halOP (HAL On Premise)

halOP is the on-premise edition of the HAL management console, serving as the successor to the [current HAL console](https://github.com/hal/console). It provides a modern web interface for managing WildFly and JBoss EAP servers.

## Deployment Modes

halOP supports multiple deployment modes to fit different environments and use cases.

### Bundled with WildFly

In this mode halOP is bundled with WildFly and can be used out of the box. halOP is available as an experimental [feature pack](https://central.sonatype.com/artifact/org.jboss.hal/hal-op-feature-pack) that can be provisioned with [Galleon](https://github.com/wildfly/galleon). The feature pack mounts an additional HTTP endpoint on the management interface at http://localhost:9990/halop.

**Maven Coordinates:**
```
org.jboss.hal:hal-op-feature-pack
```

**Provisioning Steps:**

1. Build or use the latest feature pack from Maven Central:
   ```bash
   mvn install -P op,feature-pack
   ```

2. Provision a WildFly server using the provided [`provision.xml`](https://github.com/hal/foundation/blob/main/op/feature-pack/provision.xml):
   ```bash
   galleon.sh provision op/feature-pack/target/provision.xml \
       --dir=$TMPDIR/wildfly \
       --stability-level=experimental
   ```

3. Prepare and start the server:
   ```bash
   cd $TMPDIR/wildfly
   bin/add-user.sh -u admin -p admin --silent
   bin/standalone.sh --stability=experimental
   ```

4. Open http://localhost:9990/halop

**Requirements:**
- WildFly must be started with `--stability=experimental` to enable the halOP feature pack

### Standalone (JVM)

halOP can run as a standalone single-page application ([SPA](https://en.wikipedia.org/wiki/Single-page_application)) without being part of a WildFly installation. It starts a local web server (powered by Quarkus) and serves the console on its own. The only requirement is access to a running WildFly management interface.

**Maven Coordinates:**
```
org.jboss.hal:hal-op-standalone
```

**Default URL:** http://localhost:9090

To customize the port, use `-Dquarkus.http.port=<port>`.

#### Build from Source

```bash
mvn install -P op,standalone
java -jar op/standalone/target/quarkus-app/quarkus-run.jar
```

#### Use from Maven Central

Download and run the latest release as an Uber-Jar:

```bash
mvn dependency:copy -Dartifact=org.jboss.hal:hal-op-standalone:0.4.0:jar:runner -DoutputDirectory=.
java -jar hal-op-standalone-0.4.0-runner.jar
```

#### JBang

halOP can be started using [JBang](https://jbang.dev/):

```bash
# Direct artifact reference
jbang org.jboss.hal:hal-op-standalone:0.4.0:runner

# Using the JBang catalog
jbang hal-op@hal

# Install as a command
jbang app install hal-op@hal
hal-op
```

### Standalone (Native)

halOP provides native binaries built with GraalVM for optimal startup time and memory footprint.

#### Build from Source

```bash
mvn install -P op,standalone,native
op/standalone/target/hal-op-standalone-0.4.0-runner
```

**Requirements:**
- Recent version of GraalVM installed
- See [Quarkus native image guide](https://quarkus.io/guides/building-native-image#configuring-graalvm) for setup details

#### Use from GitHub Releases

Native binaries for Linux, macOS, and Windows are attached to every [release](https://github.com/hal/foundation/releases). Download the binary for your platform, make it executable, and run it.

On macOS you may need to run:
```bash
chmod +x hal-op-*
xattr -d com.apple.quarantine hal-op-*
```

### Container

halOP is available as a container image at [quay.io/halconsole/hal-op](https://quay.io/repository/halconsole/hal-op).

**Container Image:**
```
quay.io/halconsole/hal-op
```

**Run with Podman or Docker:**
```bash
podman run -it -p 9090:9090 quay.io/halconsole/hal-op
```

## Development Mode

To contribute to halOP or develop features locally, run halOP in development mode. This requires two processes running simultaneously:

### Process 1: J2CL Watch

Transpiles Java code to JavaScript using J2CL:

```bash
mvn compile j2cl:watch -P op
```

Wait for the message:
```
[INFO] -----  Build Complete: ready for browser refresh  -----
```

### Process 2: Vite Dev Server

Serves HTML and CSS with hot module replacement:

```bash
cd op/console
npm run watch
```

This opens a browser at http://localhost:1234.

**Development Workflow:**
- **Java changes:** Detected by J2CL, requires manual browser refresh
- **HTML/CSS changes:** Detected by Vite, auto-reloads via HMR

## Next Steps

- Explore the [Features](../features/overview.md) available in halOP
- Learn about the [Architecture](../architecture/overview.md) and extension points
- Review [Getting Started](../index.md) for additional configuration options
