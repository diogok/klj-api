
(ns server.api
  (:require [opencensus-clojure.trace :refer [span add-tag]])
  (:require [taoensso.timbre :as log])
  (:require [clojure.data.json :refer [read-str write-str]]))

(defn health 
  [_]
  (span "health"
    (log/spy
      {:headers {"Content-Type" "application/json; charset=utf-8"}
       :body (write-str {:ok true})})))