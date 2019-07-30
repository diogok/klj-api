(ns server.ws
  (:require [taoensso.timbre :as log])
  (:use [org.httpkit.server :only [send! with-channel on-close on-receive]]))

(defn close-fn
  [ch] (fn [_] nil))

(defn message-fn
  [ch] (fn [data] 
         (log/spy (send! ch data))))

(defn ws
  [req] 
   (with-channel req channel
     (on-close channel (close-fn channel))
     (on-receive channel (message-fn channel))))



