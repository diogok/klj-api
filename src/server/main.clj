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
            [iapetos.collector.ring :as prom-ring :refer [wrap-metrics]])
  
  (:require [server.api :as api])
  
  (:gen-class))

(def router
  (ring/router api/routes))

(def routes
  (ring/ring-handler router))

(defonce metric-registry
  (-> (prometheus/collector-registry)
      (prom-ring/initialize)))

(defn ring-log-fn
  "Send logs from ring to log lib"
  [{:keys [level throwable message]}]
  (log/log level throwable message))

(defn metric-path-fn
  [req]
  (->>
   req
   (:uri)
   (r/match-by-path router)
   (:template)))

(defn -main
  [& args]
  (log/info "Starting server")
  (start-server 
   (-> routes
       (wrap-metrics metric-registry {:path "/metrics" :path-fn metric-path-fn})
       (wrap-log-response {:log-fn ring-log-fn})
       (wrap-log-request-params {:log-fn ring-log-fn})
       (wrap-log-request-start {:log-fn ring-log-fn}))
   {:port (Integer/valueOf ^String (env :port "8080"))}))
