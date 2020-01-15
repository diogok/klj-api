(ns server.main
  (:require [aleph.http.server :refer [start-server]]
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
  
  (:require [server.api :as api])
  
  (:gen-class))

(def router
  (ring/router api/routes))

(def routes
  (ring/ring-handler router))

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
  [req]
  (->>
   req
   (:uri)
   (r/match-by-path router)
   (:template)))

(defn path-fn [req]
  (or (get-path req) (:uri req)))

(defn setup-tracing
  []

  (if (= "enabled" (env :tracing))
    (opencensus-clojure.trace/configure-tracer {:probability 1.0})
    (opencensus-clojure.trace/configure-tracer {:probability 0.0}))
  
  (when (= "enabled" (env :trace-log))
    (io.opencensus.exporter.trace.logging.LoggingTraceExporter/register))

  (if-let [jaeger-host (env :jaeger-host)]
    (io.opencensus.exporter.trace.jaeger.JaegerTraceExporter/createAndRegister
     (str jaeger-host "/api/traces")
     (env :app_name "myapp")))

  (if-let [zipkin-host (env :zipkin-host)]
    (io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter/createAndRegister
     (str zipkin-host "/api/traces")
     (env :app_name "myapp"))))

(defn -main
  [& args]
  (log/info "Starting server")
  (setup-tracing)
  (start-server 
   (-> routes
       (wrap-metrics metric-registry {:path "/metrics" :path-fn path-fn})
       (wrap-tracing path-fn)
       (wrap-log-response {:log-fn ring-log-fn})
       (wrap-log-request-params {:log-fn ring-log-fn})
       (wrap-log-request-start {:log-fn ring-log-fn}))
   {:port (Integer/valueOf ^String (env :port "8080"))}))
