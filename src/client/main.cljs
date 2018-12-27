(ns client.main
  (:require [reagent.core :as r]))

(def ping (r/atom 0))

(defn ping-component
  [] [:p (str @ping "ms")])

(defn render
  [node] (r/render [ping-component] node))

(defn start
  [] (render
       (.appendChild 
         (.-body js/document)
         (.createElement js/document "div"))))

(defn main
  []  (set! (.-onload js/window)
        (fn [_] (start))))


