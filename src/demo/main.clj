(ns demo.main
  (:require [klj-api.core :as kapi])
  (:require [demo.api :as api])
  
  (:gen-class))

(def router (kapi/make-router api/routes))

(defn -main
  [& _]
  (kapi/start-server router))
