(ns globe.api.routes
  (:require [globe.api.data    :as data]
            [cognitect.transit :as transit]
            [clojure.java.io   :as io]))

(defn transit-response [body]
  (let [out (java.io.ByteArrayOutputStream.)
        w   (transit/writer out :json)]
    (transit/write w body)
    {:status  200
     :headers {"Content-Type"                "application/transit+json"
               "Cache-Control"               "public, max-age=3600"
               "Access-Control-Allow-Origin" "*"}
     :body    (.toString out "UTF-8")}))

(def routes
  [["/api/dialects"
    {:get  (fn [_] (transit-response (data/get-dialects)))
     :name ::dialects}]

   ["/api/health"
    {:get  (fn [_] {:status 200 :body "ok"})
     :name ::health}]])
