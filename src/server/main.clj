(ns server.main
  (:require [aleph.http.server :refer [start-server]]
            [reitit.ring :as ring])
  (:require [server.api :as api])
  (:gen-class))

(def router
  (ring/router api/routes))

(def routes
  (ring/ring-handler router))

(defn -main
  [& args]
  (println "starting")
  (start-server routes {:port 8080}))
