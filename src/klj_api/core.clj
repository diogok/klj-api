(ns klj-api.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [reitit.ring :as ring]
            [reitit.core :as r])

  (:require [environ.core :refer [env]])

  (:require [clojure.tools.logging :as log]
            [ring.logger :refer [wrap-log-request-params
                                 wrap-log-request-start
                                 wrap-log-response]])

  (:require [iapetos.core :as prometheus]
            #_[iapetos.collector.jvm :as prom-jvm]
            [iapetos.collector.ring :as prom-ring :refer [wrap-metrics]])

  (:require [opencensus-clojure.ring.middleware :refer [wrap-tracing]])

  (:gen-class))

#_"TODO: how to add JVM metrics without braking native?"
(defonce metric-registry
  (-> (prometheus/collector-registry)
      #_(prom-jvm/initialize)
      (prom-ring/initialize)))

(defn ring-log-fn
  "Send logs from ring to log lib"
  [{:keys [level throwable message]}]
  (log/log level throwable message))

(defn get-path
  "Extract path template from router for this request"
  [router req]
  (->>
   req
   (:uri)
   (r/match-by-path router)
   (:template)))

(defn path-fn 
  "Extract path template from router for this request. 
   Or return path itself if no template found"
  [router req]
  (or (get-path router req) (:uri req)))

(defn setup-tracing
  "Setup tracing with zipkin or jaeger depending on env variables"
  []

  (if (= "enabled" (env :tracing))
    (opencensus-clojure.trace/configure-tracer {:probability 1.0})
    (opencensus-clojure.trace/configure-tracer {:probability 0.0}))

  (when (= "enabled" (env :trace-log))
    (io.opencensus.exporter.trace.logging.LoggingTraceExporter/register))

  (when-let [jaeger-host (env :jaeger-host)]
    (io.opencensus.exporter.trace.jaeger.JaegerTraceExporter/createAndRegister
     (str jaeger-host "/api/traces")
     (env :app_name "myapp")))

  (when-let [zipkin-host (env :zipkin-host)]
    (io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter/createAndRegister
     (str zipkin-host "/api/traces")
     (env :app_name "myapp"))))

(defn wrap-context
  "Add :context map to request"
  [handler context]
  (fn [request]
    (handler (assoc request :context context))))

(defn start-server
  "Start server for the routes and blocks"
  [routes]
  (log/info "Starting server")
  (setup-tracing)
  (let [router  (ring/router routes)
        handler (ring/ring-handler router)]
   (run-jetty
    (-> handler
        (wrap-context {:metrics-registy metric-registry
                       :router router})
        (wrap-metrics metric-registry {:path "/metrics" :path-fn (partial path-fn router)})
        (wrap-tracing (partial path-fn router))
        (wrap-log-response {:log-fn ring-log-fn})
        (wrap-log-request-params {:log-fn ring-log-fn})
        (wrap-log-request-start {:log-fn ring-log-fn}))
     {:port (Integer/valueOf ^String (env :port "8080"))})))
