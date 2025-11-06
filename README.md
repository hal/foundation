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

# HAL On Premise

## Get Started

### Development mode

In the root folder run

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
npm run dev
```

This will open a browser at http://localhost:1234. Changes to HTML and CSS will be detected by Parcel, and the browser reloads
the page automatically. Changes to the Java code will be detected by the J2CL Maven plugin, but you need to reload the browser
manually.

### Production mode

In the root folder, run

```shell
mvn clean install -P op,prod
```

This will create a standalone console served by a simple, Quarkus-based HTTP server. To start it, run

```shell
java -jar op/standalone/target/quarkus-app/quarkus-run.jar
```

Open a browser at http://localhost:9090.

## Container

The latest version is also available as a container image at https://quay.io/repository/halconsole/halop. Use

```shell
podman run -it -p 9090:9090 quay.io/halconsole/halop
```

to start it and open a browser at http://localhost:9090.

## Native Binaries

For each release native binaries are available at https://github.com/hal/foundation/releases. Download the binary for your platform and run it. Then open a browser at http://localhost:9090. You can specify these two properties:

- `-Dquarkus.http.port=<port>` to change the port
- `-Dbrowser.open=true` to open the browser automatically

# HAL OpenShift

Not yet implemented!

# Contributing

This is an open source project. That means that everybody can contribute. It's not hard to get started. So
start [contributing](CONTRIBUTING.md) today!

# Licenses

This project uses the following licenses:

* [Apache License 2.0](https://repository.jboss.org/licenses/apache-2.0.txt)
