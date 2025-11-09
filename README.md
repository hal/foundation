# HAL Foundation

This repository contains the foundation for the next major version of the HAL management console (halOP) and the upcoming
OpenShift version (halOS).

- halOP: HAL on premise
- halOS: HAL OpenShift

The work is in a very early state and very much in progress.

# Technical Stack

- [Java 21](https://jdk.java.net/java-se-ri/21)
- [J2CL](https://github.com/google/j2cl)
- [Crysknife CDI](https://github.com/crysknife-io/crysknife)
- [Elemento](https://github.com/hal/elemento)
- [PatternFly Java](https://github.com/patternfly-java)
- [Maven](https://maven.apache.org/), [Parcel](https://parceljs.org/), and [NPM](https://www.npmjs.com/)

# halOP (HAL On Premise)

This version of HAL is shipped with WildFly or can be run as a standalone application to connect to arbitrary WildFly
instances.

## Development

In the development mode, the Java code is transpiled on the fly to JavaScript using J2CL. The HTML and CSS are transpiled to
JavaScript using Parcel. Changes to HTML and CSS will be detected by Parcel, and the browser reloads the page automatically.
Changes to the Java code will be detected by the J2CL Maven plugin, but you need to reload the browser manually.

To start HAL on premise in development mode, run

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

## Standalone

You can run halOP on its own. This starts a local web server and serves the console on its own without being part of a
WildFly installation. halOP is “just” a single-page application (SPA) without any server side dependencies. The only
requirement is a management interface of a running WIldFly instance.

### Java

To build halOP as a Java application, run

```shell
mvn install -P op,prod
```

This will package the transpiled HTML, CSS and JavaScript resources into a Quarkus-based HTTP server. To start it, run

```shell
java -jar op/standalone/target/quarkus-app/quarkus-run.jar
```

and open a browser at http://localhost:9090.

### Native Binaries

To build the native binary of halOP, run

```shell
mvn install -P op,prod,native -Dquarkus.native.container-build=false
```

Please make sure that you have a recent version of GraalVM installed.
See https://quarkus.io/guides/building-native-image#configuring-graalvm for details.

Native binaries for Linux, macOS and Windows are also attached to every [release](https://github.com/hal/foundation/releases).
Download the binary for your platform and run it. Then open a browser at http://localhost:9090.

## Container

halOP is also available as a container image at https://quay.io/repository/halconsole/hal-op. Use

```shell
podman run -it -p 9090:9090 quay.io/halconsole/hal-op
```

to start it and open a browser at http://localhost:9090.

## Customization

If you want to customize the port of halOP (Java-based and native), please use `-Dquarkus.http.port=<port>` to change the port.

# halOS (HAL on OpenShift)

This version of HAL integrates with the OpenShift console. It can be used to manage WildFly insstances running on OpenShift.

halOS is not yet implemented!

# Contributing

This is an open source project. That means that everybody can contribute. It's not hard to get started. So
start [contributing](CONTRIBUTING.md) today!

# Licenses

This project uses the following licenses:

* [Apache License 2.0](https://repository.jboss.org/licenses/apache-2.0.txt)
