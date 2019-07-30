(ns server.main
  (:require [org.httpkit.server :refer [run-server]]
            [reitit.ring :as ring]
            [ring.util.response :refer [redirect response]]
            [ring.middleware.reload :refer [wrap-reload]])
  (:require [iapetos.core :as prometheus]
            [iapetos.collector.ring :as ring-prom])
  (:require [taoensso.timbre :as log])

  (:use [server.ws] :reload)
  (:use [server.api] :reload)
  
  (:gen-class))

(defonce registry
  (-> (prometheus/collector-registry)
      (ring-prom/initialize)))

(def routes
  (ring/ring-handler
    (ring/router
      [["/health" {:get {:handler health}}]
       ["/ws" {:get {:handler ws}}]])))

(defn -main
  [& args] 
  #_(opencensus-clojure.reporting.jaeger/report "http://localhost:14268/api/traces" "my-service-name")
  (log/spy
    (run-server 
      ( -> #'routes
          (ring-prom/wrap-metrics registry {:path "/metrics"})
          (wrap-reload))
      {:port 8080})))
