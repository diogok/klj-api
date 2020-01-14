# Demo Clojure Server project

Just to keep up to date with current frameworks.

## Dependencies

- [Clojure deps tools](https://clojure.org/guides/deps_and_cli)

## Featuring

- Clojure 1.10
- Dockerfile with multistage build
- Dockerfile with with native-image using graalvm

The docker image use openjdk 13 with ZGC for low latency GC set to 85% of RAM available to the container.

The native docker image uses graalvm with Java 11, and has minimal size.

### Utils

- environ for config
- clojure.logging using log4j
- prometheus metrics
- opentracing with jaeger, zipking or log

### HTTP

- Ring
- aleph (http)
- reitit for routing
- cheshire for json

### Environment configurations:

TODO

## Tasks

`clj repl` start a generic REPL

`clj -A:jar` to create a jar

`clj -A:uberjar` uberjar standalone

`clj run` start the server with autoreload

`docker build -f Dockerfile -t diogok/klj-api .` to build docker image

`docker build -f Dockerfile.native -t diogok/klj-api:native .` to build graalvm native docker image

## License

MIT
