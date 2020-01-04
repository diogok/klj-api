
(ns server.api)

(defn health
  [_]
    {:headers {"Content-Type" "application/json; charset=utf-8"}
      :body "(json/generate-string {:ok true})"})

(defn hello
  [_]
  {:headers {"Content-Type" "application/json; charset=utf-8"}
   :body "ok2"})

(def routes
  [["/health" {:get {:handler health}}]
   ["/hello/:me" {:get {:handler hello}}]])
