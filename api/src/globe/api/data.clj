(ns globe.api.data
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def ^:private cache (atom nil))

(def data-path
  (or (System/getenv "DATA_PATH") "../data/dialects.edn"))

(defn load-dialects! []
  (let [f (io/file data-path)]
    (reset! cache {:data      (edn/read-string (slurp f))
                   :loaded-at (System/currentTimeMillis)})))

(defn get-dialects []
  (let [f    (io/file data-path)
        mtm  (.lastModified f)
        last (:loaded-at @cache 0)]
    (when (> mtm last) (load-dialects!))
    (:data @cache)))
