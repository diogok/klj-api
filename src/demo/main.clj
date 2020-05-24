(ns demo.main
  (:require [klj-api.core :as kapi])
  (:require [demo.api :as api])
  
  (:gen-class))

(defn -main
  [& _]
  (kapi/start-server api/routes))
