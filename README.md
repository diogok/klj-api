# Demo Clojure + Clojurescript project

Just to keep up to date with current frameworks.

## Dependencies

- [Clojure deps tools](https://clojure.org/guides/deps_and_cli)

## Featuring

- Clojure 1.9

### Utils

- environ for config
- timbre for logs
- core.async

### HTTP

- Ring
- http-kit (http+websocket)
- reitit for routing
- hiccup for SSR

### CLJS

- ClojureScript
- reagent for React UI
- garden for CSS

### The project

- Static assets as resources/publi
- CLJS at src/client
- CLJ server at src/server
- server/main.clj start the server and set the URL routes
- server/index.clj renders the index HTML using hiccup
- server/socket.clj sets the websocket comms
- client/main.cljs start the client, with a react component to display ping time from the websocket


## Tasks

`clj repl` start a generic REPL

`clj -A:jar` to create a jar

`clj -A:uberjar` uberjar standalone

`clj -A:nightlight` Run nightlight embeded edit

`clj -A:liquid` Run liquid editor

`clj -A:run` Run the HTTP/WS server, with auto-reload, but no cljs build

`clj -A:cljs-prod` Generate cljs production ready assets

`clj -A:cljs-repl` Start a cljs repl

`clj -A:cljs-dev` CLJs build with auto-reload

## License

MIT

