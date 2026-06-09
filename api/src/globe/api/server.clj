(ns globe.api.server
  (:require [globe.api.data      :as data]
            [globe.api.routes    :as routes]
            [reitit.ring         :as ring]
            [ring.adapter.jetty  :as jetty]
            [ring.middleware.cors :refer [wrap-cors]])
  (:gen-class))

(defn make-app []
  (-> (ring/ring-handler
       (ring/router routes/routes)
       (ring/create-default-handler))
      (wrap-cors :access-control-allow-origin  [#".*"]
                 :access-control-allow-methods [:get])))

(defn -main [& _]
  (data/load-dialects!)
  (println "→ API rodando em http://localhost:3000")
  (jetty/run-jetty (make-app) {:port 3000 :join? true}))
