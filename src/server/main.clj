(ns server.main
  (:require [org.httpkit.server :refer [run-server]]
            [bidi.ring :refer [make-handler]]
            [ring.util.response :refer [redirect response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]            
            [ring.middleware.not-modified :refer [wrap-not-modified]])
  (:require [taoensso.timbre :as log])
  (:use [server.index]
        [server.socket] :reload)
  
  (:gen-class))
   
(defn html 
  [page] (fn [_] 
           {:headers {"Content-Type" "text/html; charset=utf-8"}
            :body (page)}))

(def routes
  (make-handler  
    ["/" {"" (html index)
          "ws" socket}]))

(defn -main
  [& args] 
  (log/spy
    (run-server 
      ( -> #'routes
          (wrap-reload)
          (wrap-resource "public")
          (wrap-content-type)      
          (wrap-not-modified)
          )
      {:port 8080})))
