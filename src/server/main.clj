(ns server.main
  (:require [org.httpkit.server :refer [run-server]]
            [reitit.ring :as ring]
            [ring.util.response :refer [redirect response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.logger :refer [wrap-log-response]])
  (:require [iapetos.core :as prometheus]
            [iapetos.collector.ring :refer [wrap-metrics initialize]])
  (:require [taoensso.timbre :as log])
  (:require [environ.core :refer [env]])
  (:require [opencensus-clojure.ring.middleware :refer [wrap-tracing]])
  (:require [clojure.string :as str])

  (:require [manifold.stream :as s]
            [aleph.udp :as udp])
  (:require [cheshire.core :as json])

  (:require [server.api :as api] :reload) #_"Your API go here"
  
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

(defn log-line
  [data]
  (merge (:context data)
            {:level (:level data)
             :namespace (:?ns-str data)
             :file (:?file data)
             :line (:?line data)
             :stacktrace (:?err data)
             :hostname (force (:hostname_ data))
             :message (force (:msg_ data))
             "@timestamp" (:instant data)}))

(defn logstash-appender
  [] 
  (let [conn (udp/socket {})]
   {:enabled? true
    :async? false
    :fn (fn [data]
          (s/put! @conn
            {:host (env :log-host "logstash")
             :port (Integer/valueOf (env :log-port "5432"))
             :message (json/generate-string (log-line data))}))
   }))

(defn -main
  [& args] 
  #_(opencensus-clojure.reporting.jaeger/report "http://localhost:14268/api/traces" "my-service-name")

  (if-let [logstash-host (env :log-host)]
    (log/merge-config!
      {:appenders
        {:logstash (logstash-appender)}}))

  (log/spy
    (run-server 
      (-> #'routes
          (wrap-metrics registry {:path "/metrics"})
          (wrap-log-response {:log-fn ring-logger-fn})
          (wrap-tracing tracing-fn)
          (wrap-reload))
      {:port (Integer/valueOf (env :port "8080"))})))
