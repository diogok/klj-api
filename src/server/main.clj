(ns server.main
  (:require [org.httpkit.server :refer [run-server]]
            [reitit.ring :as ring]
            [ring.util.response :refer [redirect response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.logger :refer [wrap-log-response]])
  (:require [iapetos.core :as prometheus]
            [iapetos.collector.ring :refer [wrap-metrics initialize]])
  (:require [taoensso.timbre :as log]
            [taoensso.timbre.appenders.3rd-party.logstash :refer [logstash-appender]])
  (:require [environ.core :refer [env]])
  (:require [opencensus-clojure.ring.middleware :refer [wrap-tracing]])
  (:require [clojure.string :as str])

  (:require [server.api :as api] :reload)
  
  (:gen-class))

(defonce registry
  (-> (prometheus/collector-registry)
      (initialize)))

(def routes
  (ring/ring-handler
    (ring/router api/routes)))

(defn ring-logger-fn
  [{:keys [level throwable message]}]
    (log/info level throwable message))

(defn tracing-fn [req] 
  (-> req :uri (str/replace #"/" "_")))

(defn -main
  [& args] 
  #_(opencensus-clojure.reporting.jaeger/report "http://localhost:14268/api/traces" "my-service-name")

  (if-let [logstash-host (env :logstash-host)]
    (log/merge-config!
      {:appenders
        {:logstash (logstash-appender logstash-host (Integer/valueOf (env :logstash-port "5432")))}}))

  (log/spy
    (run-server 
      (-> #'routes
          (wrap-metrics registry {:path "/metrics"})
          (wrap-log-response {:log-fn ring-logger-fn})
          (wrap-tracing tracing-fn)
          (wrap-reload))
      {:port (Integer/valueOf (env :port "8080"))})))
