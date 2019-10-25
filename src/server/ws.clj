(ns server.ws
  (:require [taoensso.timbre :as log])
  (:require [aleph.http :as http]
            [manifold.stream :as stream]))

(defn message-fn
  [ch] (fn [data] 
         (log/spy (stream/put! ch data))))

(defn ws
  [req]
  (let [channel @(http/websocket-connection req)]
    (stream/consume channel (partial message-fn channel))))



