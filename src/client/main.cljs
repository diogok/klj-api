(ns client.main)

(defn create-canvas
  [] (.createElement js/document "canvas"))

(defn append-canvas
  [] (-> js/document
         (.-body)
         (.appendChild (create-canvas))))

(defn -main
  []  (set! (.-onload js/window)
        (fn [_] (append-canvas))))


