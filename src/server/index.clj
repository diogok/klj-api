(ns server.index
  (:use [hiccup.core]
        [hiccup.page]))

(defn index
  [] (html5 
       (include-js "js/main.js")))

