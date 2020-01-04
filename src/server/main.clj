(ns server.main
  (:require [aleph.http.server :refer [start-server]]
            [reitit.ring :as ring])
  (:require [clojure.tools.logging :as log]
            [ring.logger :refer [wrap-log-request-params
                                 wrap-log-request-start
                                 wrap-log-response]])
  (:require [server.api :as api])
  (:gen-class))

(defn ring-log-fn
  "Send logs from ring to log lib"
  [{:keys [level throwable message]}]
  (log/log level throwable message))

(def router
  (ring/router api/routes))

(def routes
  (ring/ring-handler router))

(defn -main
  [& args]
  (log/info "Starting server")
  (start-server 
   (-> routes
       (wrap-log-response {:log-fn ring-log-fn})
       (wrap-log-request-params {:log-fn ring-log-fn})
       (wrap-log-request-start {:log-fn ring-log-fn})) 
   {:port 8080}))
