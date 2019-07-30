# Demo Clojure Server project

Just to keep up to date with current frameworks.

## Dependencies

- [Clojure deps tools](https://clojure.org/guides/deps_and_cli)

## Featuring

- Clojure 1.10
- Dockerfile with multistage build

### Utils

- environ for config
- timbre for logs
- core.async
- prometheus metrics
- opentracing with jaeger

### HTTP

- Ring
- http-kit (http+websocket)
- reitit for routing

## Tasks

`clj repl` start a generic REPL

`clj -A:jar` to create a jar

`clj -A:uberjar` uberjar standalone

`clj run` start the server with autoreload

## License

MIT
s