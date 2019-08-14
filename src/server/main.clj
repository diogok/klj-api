(ns server.main
  (:require [org.httpkit.server :refer [run-server]]
            [reitit.ring :as ring]
            [reitit.core :as r]
            [ring.util.response :refer [redirect response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.logger :refer [wrap-log-response]])

  (:require [clojure.string :as str])

  (:require [cheshire.core :as json])

  (:require [iapetos.core :as prometheus]
            [iapetos.collector.jvm :as prom-jvm]
            [iapetos.collector.ring :as prom-ring :refer [wrap-metrics]])

  (:require [taoensso.timbre :as log])

  (:require [environ.core :refer [env]])

  (:require [opencensus-clojure.ring.middleware :refer [wrap-tracing]])

  (:require [sentry-clj.core :as sentry])

  (:require [manifold.stream :as s]
            [aleph.udp :as udp])

  (:require [server.api :as api] :reload)
  
  (:gen-class))

(defonce registry
  (-> (prometheus/collector-registry)
      (prom-jvm/initialize)
      (prom-ring/initialize)))

(def router
  (ring/router api/routes))

(def routes
  (ring/ring-handler router))

(defn ring-logger-fn
  [{:keys [level throwable message]}]
    (log/info level throwable message))

(defn tracing-fn [req]
  (if-let [route (r/match-by-path router (req :uri))]
    (:template route)
    (:uri req)))

(defn metric-path-fn
  [req] 
  (->>
    req
    (:uri)
    (r/match-by-path router)
    (:template)))

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
             :application (env :app "app")
             :app_version (env :app-version "dev")
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

(defn wrap-sentry
  [handler]
   (fn [req]
     (try 
       (handler req)
       (catch Exception e
         (do
           (sentry/send-event {:throwable e
                               :environment (env :env "dev")
                               :release (env :version "dev")})
           {:status 500 :body (json/generate-string {:ok false})})
         ))))

(defn -main
  [& args] 
  #_(opencensus-clojure.reporting.jaeger/report "http://localhost:14268/api/traces" "my-service-name")

  (if-let [logstash-host (env :log-host)]
    (log/merge-config!
      {:appenders
        {:logstash (logstash-appender)}}))

  (if-let [sentry-dsn (env :sentry)]
    (sentry/init! sentry-dsn))

  (log/spy
    (run-server 
      (-> #'routes
          (wrap-sentry)
          (wrap-log-response {:log-fn ring-logger-fn})
          (wrap-tracing tracing-fn)
          (wrap-metrics registry {:path "/metrics" :path-fn metric-path-fn})
          (wrap-reload))
      {:port (Integer/valueOf (env :port "8080"))})))
