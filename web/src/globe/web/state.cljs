(ns globe.web.state
  (:require [reagent.core :as r]))

(defonce app
  (r/atom {:dialetos     []
           :ecossistemas []
           :selecionado  nil
           :auto-rotate  true
           :carregando   true}))

(defn selecionar! [dialeto]
  (swap! app assoc :selecionado dialeto :auto-rotate false))

(defn fechar-painel! []
  (swap! app assoc :selecionado nil :auto-rotate true))

(defn focar-em! [lat lng]
  (swap! app assoc :foco {:lat lat :lng lng} :auto-rotate false))

(defn dialetos-por-eco [eco-id]
  (filter #(= (:eco %) eco-id) (:dialetos @app)))

(defn n-dialetos-por-eco [eco-id]
  (count (dialetos-por-eco eco-id)))
