(ns client.main
  (:require [reagent.core :as r]))

(def ping (r/atom 0))
(def ws (atom nil))
(def last-ping (atom 0))

(defn now
  [] (.getTime (js/Date.)))


(defn ping-component
  [] [:p (str @ping "ms")])

(defn render
  [node] (r/render [ping-component] node))

(defn ws-url
  [] (str "ws://" (.-host js/location) "/ws"))

(defn opened
  [conn] (println "Connected!")
     (swap! ws (fn [_] conn))
     (do-ping))

(defn connected?
  [] (not (nil? @ws)))

(defn closed
  []  (println "bye"))

(defn message
  [msg] 
   (let [data (.parse js/JSON (.-data msg))
         obj (js->clj data :keywordize-keys true)
         cmd (:command obj)]
     (when (= "ping" cmd)
       (do (do-ping)
           (swap! ping (fn [_] (- (now) @last-ping)))))))

(defn connect
  [] (let [ws (js/WebSocket. (ws-url))]
         (set! (.-onopen ws) 
           (fn [_] (opened ws)))
         (set! (.-onclose ws)
            (fn [_] (closed)))
         (set! (.-onmessage ws)
            (fn [e] (message e)))))

(defn do-ping
  [] (when (connected?)
       (do
         (swap! last-ping (fn [_] (now)))
         (.send @ws (.stringify js/JSON (clj->js {:command :ping}))))))

(defn start
  [] (render
       (.appendChild 
         (.-body js/document)
         (.createElement js/document "div"))))

(defn main
  []  (set! (.-onload js/window)
        (fn [_] 
          (do 
            (start)
            (connect)))))

(main)
