# HAL Foundation

This repository contains the foundation for the next major version of the HAL management console (halOP) and the upcoming
OpenShift version (halOS). To distinguish between the two editions, we introduce the following names:

- halOP: HAL on premise
- halOS: HAL on OpenShift

The work on the new consoles is in an early state and very much in progress.

# Technical Stack

- [Java 21](https://jdk.java.net/java-se-ri/21)
- [J2CL](https://github.com/google/j2cl)
- [Crysknife CDI](https://github.com/crysknife-io/crysknife)
- [Elemento](https://github.com/hal/elemento)
- [PatternFly Java](https://github.com/patternfly-java)
- [Maven](https://maven.apache.org/), [Parcel](https://parceljs.org/), and [PNPM](https://pnpm.io/)

# halOP (HAL On Premise)

This edition is the successor of the current [HAL management console](https://github.com/hal/console).

## Get Started

There are many ways to get started with halOP, but essentially there are two ways to use the console:

1. Bundled – as part of a WildFly installation
2. Standalone – as a single-page application ([SPA](https://en.wikipedia.org/wiki/Single-page_application)) running on its own

### Bundled

In this mode halOP is bundled with WildFly and can be used out of the box. halOP is available as an
experimental [feature pack](https://central.sonatype.com/artifact/org.jboss.hal/hal-op-feature-pack)
that can be provisioned with [Galleon](https://github.com/wildfly/galleon). The feature pack mounts an additional HTTP endpoint
on the management interface at http://localhost:9990/halop.

1. Build or use the latest feature pack from Maven Central [
   `org.jboss.hal:hal-op-feature-pack:0.2.7`](https://central.sonatype.com/artifact/org.jboss.hal/hal-op-feature-pack)

    ```shell
    mvn install -P prod,op,feature-pack
    ```

2. Provision a WildFly server. You can use the provided [
   `provision.xml`](https://github.com/hal/foundation/blob/main/op/feature-pack/provision.xml) as an example. It provisions a
   default standalone server plus the halOP feature pack. If you haven't built the feature pack in step 1 and used the latest
   version from Maven Central, you have to run `mvn process-resources` in `op/feature-pack` in order to resolve the versions
   in [`provision.xml`](https://github.com/hal/foundation/blob/main/op/feature-pack/provision.xml).

    ```shell
    galleon.sh provision op/feature-pack/target/provision.xml \
        --dir=$TMPDIR/wildfly \
        --stability-level=experimental
    ```

3. Prepare and start the server

    ```shell
    cd $TMPDIR/wildfly
    bin/add-user.sh -u admin -p admin --silent
    bin/standalone.sh --stability=experimental
    ```

4. Open http://localhost:9990/halop

### Standalone

halOP can run on its own. In this mode halOP starts a local web server and serves the console on its own without being part of a
WildFly installation. halOP is "just" a single-page application ([SPA](https://en.wikipedia.org/wiki/Single-page_application))
without any server side dependencies. The only requirement is a management interface of a running WIldFly instance.

By default, the standalone mode runs at http://localhost:9090. If you want to customize the port, please use
`-Dquarkus.http.port=<port>`.

There are many ways to run halOP in standalone mode:

#### Build and run on your own (JVM)

```shell
mvn install -P prod,op,standalone
java -jar op/standalone/target/quarkus-app/quarkus-run.jar
```

#### Build and run on your own (native)

```shell
mvn install -P prod,op,standalone,native
op/standalone/target/hal-op-standalone-0.2.7-runner
```

Please make sure that you have a recent version of GraalVM installed.
See https://quarkus.io/guides/building-native-image#configuring-graalvm for details.

#### Use the latest release (JVM)

The latest release is deployed as Uber-Jar using the `runner` classifier to Maven Central: [
`org.jboss.hal:hal-op-standalone:0.2.7`](https://central.sonatype.com/artifact/org.jboss.hal/hal-op-standalone). You can
download and run it with

```shell
mvn dependency:copy -Dartifact=org.jboss.hal:hal-op-standalone:0.2.7:jar:runner -DoutputDirectory=.
java -jar hal-op-standalone-0.2.7-runner.jar
```

#### Use the latest release (native)

Native binaries for Linux, macOS, and Windows are attached to every [release](https://github.com/hal/foundation/releases).
Download the binary for your platform, make it executable, and run it. To make the binary executable, you might need to run
something like this (depending on your OS):

```shell
chmod +x hal-op-*
xattr -d com.apple.quarantine hal-op-*
```

#### JBang

halOP can also be started using [JBang](https://jbang.dev/).

```shell
jbang org.jboss.hal:hal-op-standalone:0.2.7:runner
```

If you want it even simpler, you can make use of
the [JBang catalog](https://www.jbang.dev/documentation/jbang/latest/alias_catalogs.html#catalogs) for halOP:

```shell
jbang hal-op@hal
```

Finally, you can also install it as a command using `jbang app install hal-op@hal`. Then all you have to do is to run

```shell
hal-op
```

and you'll always be up to date.

#### Container

halOP is also available as a container image at https://quay.io/repository/halconsole/hal-op.

```shell
podman run -it -p 9090:9090 quay.io/halconsole/hal-op
```

## Development

If you want to contribute to halOP, follow these steps to start halOP in development mode. In the development mode, the Java
code is transpiled to JavaScript using J2CL. The HTML and CSS are transpiled to JavaScript
using Parcel. Changes to HTML and CSS will be detected by Parcel, and the browser reloads the page automatically.
Changes to the Java code will be detected by the J2CL Maven plugin, but you need to reload the browser manually.

To start halOP in development mode, run

```shell
mvn j2cl:watch -P op
```

and wait until you see the message

```
[INFO] -----  Build Complete: ready for browser refresh  -----
```

In another shell run

```shell
cd op/console
npm run watch
```

This will open a browser at http://localhost:1234.

# halOS (HAL on OpenShift)

This version of HAL integrates with the OpenShift console. It can be used to manage WildFly insstances running on OpenShift.

halOS is not yet implemented!

# Contributing

This is an open-source project. That means that everybody can contribute. It's not hard to get started. So
start [contributing](CONTRIBUTING.md) today!

# Licenses

This project uses the following licenses:

* [Apache License 2.0](https://repository.jboss.org/licenses/apache-2.0.txt)
