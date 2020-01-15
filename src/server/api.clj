
(ns server.api
  (:require [cheshire.core :as json]))

(defn health
  [_]
    {:headers {"Content-Type" "application/json; charset=utf-8"}
      :body (json/generate-string {:ok true})})

(defn hello
  [req]
  {:headers {"Content-Type" "application/json; charset=utf-8"}
   :body (json/generate-string {:hello (:me (:path-params req))})})

(def routes
  [["/health" {:get {:handler health}}]
   ["/hello/:me" {:get {:handler hello}}]])
