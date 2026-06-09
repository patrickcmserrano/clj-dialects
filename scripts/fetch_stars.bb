#!/usr/bin/env bb

(ns fetch-stars
  (:require [babashka.http-client :as http]
            [cheshire.core         :as json]
            [clojure.edn           :as edn]
            [clojure.java.io       :as io]))

(def token (System/getenv "GITHUB_TOKEN"))

(defn github-headers []
  {"Authorization" (str "Bearer " token)
   "Accept"        "application/vnd.github+json"})

(defn fetch-repo-stars [repo]
  (when token
    (try
      (-> (http/get (str "https://api.github.com/repos/" repo)
                    {:headers (github-headers)})
          :body
          (json/parse-string true)
          :stargazers_count)
      (catch Exception _
        (println "Aviso: falha ao buscar" repo)
        nil))))

(defn enrich-dialeto [{:keys [repo stars] :as dialeto}]
  (let [live (when repo (fetch-repo-stars repo))]
    (cond-> dialeto
      live  (assoc :stars live)
      ;; mantém :stars do base.edn se fetch falhar
      (and (nil? live) stars) identity)))

(let [base         (edn/read-string (slurp "data/base.edn"))
      enriquecidos (->> (:dialetos base)
                        (mapv enrich-dialeto))
      resultado    (-> base
                       (assoc :dialetos enriquecidos)
                       (assoc-in [:meta :atualizado-em] (str (java.time.LocalDate/now))))]
  (spit "data/dialects.edn" (pr-str resultado))
  (let [com-stars (count (filter :stars enriquecidos))]
    (println (str "✓ data/dialects.edn gerado com " (count enriquecidos)
                  " dialetos (" com-stars " com stars)"))))
