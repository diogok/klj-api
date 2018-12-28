(ns server.main
  (:require [org.httpkit.server :refer [run-server]]
            [bidi.ring :refer [make-handler]]
            [ring.util.response :refer [redirect response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]            
            [ring.middleware.not-modified :refer [wrap-not-modified]])
  (:use [server.index :reload true]
        [server.socket :reload true])
  
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
  (run-server 
    ( -> #'routes
        (wrap-resource "public")
        (wrap-content-type)      
        (wrap-not-modified))
    {:port 8080}))
