(ns globe.web.ui
  (:require [clojure.string  :as str]
            [reagent.core    :as r]
            [globe.web.globe   :as g]
            [globe.web.state :as state]))

(defn- foco-dialeto [dialeto eco]
  (if-let [{:keys [lat lng]} (get g/pais->coords (:pais dialeto))]
    [lat lng]
    [(:lat eco) (:lng eco)]))

(defn stars-bar [stars max-stars]
  (let [pct (* 100 (/ (or stars 0) max-stars))]
    [:div {:style {:background "#0d1f44" :border-radius "4px" :height "6px" :width "100%"}}
     [:div {:style {:background "#3b82f6"
                    :height "100%"
                    :width (str pct "%")
                    :border-radius "4px"
                    :transition "width 0.4s ease"}}]]))

(defn origem-badge [{:keys [bandeira pais criador]}]
  (when pais
    [:div {:style {:display "flex" :align-items "center" :gap "6px"
                   :background "rgba(255,255,255,0.04)"
                   :border "1px solid rgba(255,255,255,0.08)"
                   :border-radius "6px"
                   :padding "6px 10px"
                   :margin-bottom "16px"}}
     (when bandeira
       [:span {:style {:font-size "20px" :line-height "1"}} bandeira])
     [:div
      [:div {:style {:font-size "13px" :color "#e2e8f0" :font-weight "500"}} pais]
      (when criador
        [:div {:style {:font-size "11px" :color "#64748b" :margin-top "1px"}} criador])]]))

(defn painel-lateral []
  (let [{:keys [selecionado ecossistemas]} @state/app
        max-stars 11000]
    (when selecionado
      (let [eco (first (filter #(= (:id %) (:eco selecionado)) ecossistemas))]
        [:div {:style {:position   "fixed"
                       :top        0
                       :right      0
                       :width      "320px"
                       :height     "100%"
                       :background "rgba(2,8,23,0.95)"
                       :border-left "1px solid rgba(59,130,246,0.2)"
                       :padding    "24px"
                       :color      "#e2e8f0"
                       :overflow-y "auto"
                       :z-index    "20"}}
         [:button {:on-click state/fechar-painel!
                   :style {:background "none" :border "none" :color "#64748b"
                           :cursor "pointer" :font-size "20px" :float "right"}}
          "×"]
         [:div {:style {:margin-top "8px"}}
          [:div {:style {:font-size "11px" :color "#64748b" :text-transform "uppercase"
                         :letter-spacing "0.1em"}}
           (str "Rank #" (:rank selecionado))]
          [:h2 {:style {:font-size "24px" :font-weight "700" :margin "4px 0 8px"
                        :color "#f8fafc"}}
           (:nome selecionado)]
          [:div {:style {:display "inline-block"
                         :background (str "rgba("
                                          (str/join "," (:cor eco))
                                          ",0.15)")
                         :color (str "rgb(" (str/join "," (:cor eco)) ")")
                         :border (str "1px solid rgba("
                                      (str/join "," (:cor eco))
                                      ",0.4)")
                         :padding "2px 10px" :border-radius "12px"
                         :font-size "12px" :margin-bottom "16px"}}
           (:nome eco)]
          [origem-badge selecionado]
          [:p {:style {:color "#94a3b8" :font-size "14px" :line-height "1.6"
                       :margin-bottom "20px"}}
           (:desc selecionado)]
          [:div {:style {:margin-bottom "16px"}}
           [:div {:style {:display "flex" :justify-content "space-between"
                          :font-size "12px" :color "#64748b" :margin-bottom "4px"}}
            [:span "Stars"]
            [:span (or (:stars selecionado) "—")]]
           [stars-bar (:stars selecionado) max-stars]]
          [:table {:style {:width "100%" :font-size "13px" :border-collapse "collapse"}}
           [:tbody
            [:tr
             [:td {:style {:color "#64748b" :padding "4px 0"}} "Host"]
             [:td {:style {:color "#e2e8f0" :text-align "right"}} (:host selecionado)]]
            [:tr
             [:td {:style {:color "#64748b" :padding "4px 0"}} "Release"]
             [:td {:style {:color "#e2e8f0" :text-align "right"}} (:release selecionado)]]
            (when (:repo selecionado)
              [:tr
               [:td {:style {:color "#64748b" :padding "4px 0"}} "Repo"]
               [:td {:style {:text-align "right"}}
                [:a {:href   (str "https://github.com/" (:repo selecionado))
                     :target "_blank"
                     :style  {:color "#3b82f6" :text-decoration "none"}}
                 (:repo selecionado)]]])]]]]))))

(defn lista-dialetos []
  (let [{:keys [dialetos ecossistemas carregando]} @state/app
        n-total (count dialetos)]
    [:div {:style {:position   "fixed"
                   :top        0
                   :left       0
                   :width      "260px"
                   :height     "100%"
                   :background "rgba(2,8,23,0.92)"
                   :border-right "1px solid rgba(59,130,246,0.15)"
                   :display    "flex"
                   :flex-direction "column"
                   :z-index    "10"}}
     ;; título
     [:div {:style {:padding     "20px 16px 14px"
                    :border-bottom "1px solid rgba(59,130,246,0.1)"
                    :flex-shrink "0"}}
      [:div {:style {:font-size "10px" :color "#475569" :text-transform "uppercase"
                     :letter-spacing "0.12em" :margin-bottom "4px"}}
       "Clojure Dialects Globe"]
      [:div {:style {:font-size "18px" :font-weight "700" :color "#f8fafc"}}
       (if carregando
         "Carregando..."
         (str n-total " dialetos detectados"))]
      (when (not carregando)
        [:div {:style {:font-size "11px" :color "#475569" :margin-top "2px"}}
         (str (count ecossistemas) " ecossistemas")])]
     ;; lista por ecossistema
     [:div {:style {:overflow-y "auto" :flex "1" :padding "8px 0"}}
      (for [eco ecossistemas
            :let [ds (filter #(= (:eco %) (:id eco)) dialetos)]
            :when (seq ds)]
        ^{:key (:id eco)}
        [:div {:style {:margin-bottom "4px"}}
         ;; cabeçalho do ecossistema
         [:div {:style {:display      "flex"
                        :align-items  "center"
                        :gap          "6px"
                        :padding      "5px 16px 3px"
                        :cursor       "pointer"}
                :on-click (fn [] (state/focar-em! (:lat eco) (:lng eco)))}
          [:div {:style {:width "7px" :height "7px" :border-radius "50%" :flex-shrink "0"
                         :background (str "rgb(" (str/join "," (:cor eco)) ")")}}]
          [:span {:style {:color "#94a3b8" :font-size "11px" :font-weight "600"
                          :text-transform "uppercase" :letter-spacing "0.08em"}}
           (:nome eco)]
          [:span {:style {:color "#334155" :font-size "10px" :margin-left "auto"}}
           (count ds)]]
         ;; dialetos do ecossistema
         (for [d (sort-by #(- (or (:stars %) 0)) ds)]
           ^{:key (:nome d)}
           [:div {:style    {:display      "flex"
                             :align-items  "center"
                             :gap          "7px"
                             :padding      "4px 16px 4px 28px"
                             :cursor       "pointer"
                             :transition   "background 0.15s"}
                  :on-click (fn []
                              (let [[lat lng] (foco-dialeto d eco)]
                                (state/focar-em! lat lng))
                              (state/selecionar! d))
                  :on-mouse-enter (fn [e]
                                    (set! (.. e -currentTarget -style -background)
                                          "rgba(59,130,246,0.08)"))
                  :on-mouse-leave (fn [e]
                                    (set! (.. e -currentTarget -style -background)
                                          "transparent"))}
            (when (:bandeira d)
              [:span {:style {:font-size "12px" :line-height "1"}} (:bandeira d)])
            [:span {:style {:color "#cbd5e1" :font-size "12px" :flex "1"
                            :white-space "nowrap" :overflow "hidden"
                            :text-overflow "ellipsis"}}
             (:nome d)]
            (when (:stars d)
              [:span {:style {:color "#334155" :font-size "10px" :flex-shrink "0"}}
               (let [s (:stars d)]
                 (if (>= s 1000)
                   (str (js/Math.round (/ s 100)) "k" )
                   (str s)))])])])]]))

(defn tooltip [{:keys [dialeto eco x y]}]
  (when (or dialeto eco)
    [:div {:style {:position "fixed"
                   :left (str (+ x 12) "px")
                   :top  (str (- y 8) "px")
                   :background "rgba(2,8,23,0.92)"
                   :border "1px solid rgba(59,130,246,0.3)"
                   :border-radius "6px"
                   :padding "8px 12px"
                   :pointer-events "none"
                   :white-space "nowrap"
                   :z-index "30"}}
     (if dialeto
       [:div
        [:div {:style {:display "flex" :align-items "center" :gap "6px"}}
         (when (:bandeira dialeto)
           [:span {:style {:font-size "14px"}} (:bandeira dialeto)])
         [:span {:style {:color "#f8fafc" :font-size "13px" :font-weight "600"}}
          (:nome dialeto)]]
        (when (:stars dialeto)
          [:div {:style {:color "#64748b" :font-size "11px" :margin-top "2px"}}
           (str "★ " (:stars dialeto))])]
       ;; Ecosystem tooltip
       [:div
        [:div {:style {:color "#3b82f6" :font-size "10px" :text-transform "uppercase"
                       :letter-spacing "0.05em" :font-weight "700" :margin-bottom "2px"}}
         "Ecossistema"]
        [:div {:style {:color "#f8fafc" :font-size "13px" :font-weight "600"}}
         (:nome eco)]
        [:div {:style {:color "#64748b" :font-size "11px" :margin-top "2px"}}
         (:descricao eco)]])]))

(defonce tooltip-state (r/atom nil))

(defn loading-overlay []
  (when (:carregando @state/app)
    [:div {:style {:position "fixed" :inset 0
                   :display "flex" :align-items "center" :justify-content "center"
                   :background "rgba(2,8,23,0.7)"
                   :color "#64748b" :font-size "14px"
                   :z-index "40"}}
     "Carregando dialetos..."]))

(defn root []
  [:div
   [loading-overlay]
   [lista-dialetos]
   [painel-lateral]
   [tooltip @tooltip-state]])
