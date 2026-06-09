(ns globe.web.data
  (:require [cljs.reader :as reader]
            [globe.web.state :as state]))

(defn fetch-dialects! [static-path]
  (-> (js/fetch static-path)
      (.then #(.text %))
      (.then (fn [body]
               (let [data (reader/read-string body)]
                 (swap! state/app assoc
                        :dialetos     (:dialetos data)
                        :ecossistemas (:ecossistemas data)
                        :carregando   false))))
      (.catch (fn [e]
                (js/console.error "Falha ao carregar dados" e)
                (swap! state/app assoc :carregando false)))))
