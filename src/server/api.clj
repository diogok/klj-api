
(ns server.api
  (:require [opencensus-clojure.trace :refer [span add-tag]])
  (:require [taoensso.timbre :as log])
  (:require [clojure.data.json :refer [read-str write-str]])
  
  (:use [server.ws] :reload))

(defn health 
  [_]
    {:headers {"Content-Type" "application/json; charset=utf-8"}
      :body (write-str {:ok true})})

(def routes
  [["/health" {:get {:handler health}}]
       ["/ws" {:get {:handler ws}}]])