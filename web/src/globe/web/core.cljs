(ns globe.web.core
  (:require ["three"              :as THREE]
            ["react-dom/client"   :as rdom-client]
            [globe.web.globe      :as g]
            [globe.web.state      :as state]
            [globe.web.data       :as data]
            [globe.web.ui         :as ui]
            [reagent.core         :as r]))

(defonce scene-ref (atom nil))
(defonce drag-state (atom {:dragging false :last-x 0 :last-y 0 :idle-timer nil}))

(defn- reset-auto-rotate! []
  (when-let [t (:idle-timer @drag-state)]
    (js/clearTimeout t))
  (swap! drag-state assoc
         :idle-timer (js/setTimeout #(swap! state/app assoc :auto-rotate true) 3500)))

(defn- on-mousedown [e]
  (swap! drag-state assoc :dragging true :last-x (.-clientX e) :last-y (.-clientY e))
  (swap! state/app assoc :auto-rotate false))

(defn- on-mousemove [e globe-g raycaster camera]
  (when (:dragging @drag-state)
    (let [dx (- (.-clientX e) (:last-x @drag-state))
          dy (- (.-clientY e) (:last-y @drag-state))]
      (set! (.. ^js globe-g -rotation -y) (+ (.. ^js globe-g -rotation -y) (* dx 0.005)))
      (set! (.. ^js globe-g -rotation -x) (+ (.. ^js globe-g -rotation -x) (* dy 0.005)))
      (swap! drag-state assoc :last-x (.-clientX e) :last-y (.-clientY e))))
  ;; hover tooltip
  (let [mouse (THREE/Vector2.
               (- (* (/ (.-clientX e) js/window.innerWidth) 2) 1)
               (- 1 (* (/ (.-clientY e) js/window.innerHeight) 2)))]
    (.setFromCamera ^js raycaster mouse camera)
    (let [sprites (filter #(instance? THREE/Sprite %) (array-seq (.-children ^js globe-g)))
          hits    (.intersectObjects ^js raycaster (clj->js sprites))]
      (if (> (.-length hits) 0)
        (let [ud (.-userData (.-object (aget hits 0)))
              d  (js->clj (aget ud "dialeto") :keywordize-keys true)
              eco (js->clj (aget ud "eco") :keywordize-keys true)]
          (when (or d eco)
            (reset! ui/tooltip-state {:dialeto d :eco eco :x (.-clientX e) :y (.-clientY e)})))
        (reset! ui/tooltip-state nil)))))

(defn- on-mouseup [_]
  (when (:dragging @drag-state)
    (swap! drag-state assoc :dragging false)
    (reset-auto-rotate!)))

(defn- on-click [e globe-g raycaster camera]
  (let [mouse (THREE/Vector2.
               (- (* (/ (.-clientX e) js/window.innerWidth) 2) 1)
               (- 1 (* (/ (.-clientY e) js/window.innerHeight) 2)))]
    (.setFromCamera ^js raycaster mouse camera)
    (let [sprites (filter #(instance? THREE/Sprite %) (array-seq (.-children ^js globe-g)))
          hits    (.intersectObjects ^js raycaster (clj->js sprites))]
      (when (> (.-length hits) 0)
        (let [ud (.-userData (.-object (aget hits 0)))
              d  (js->clj (aget ud "dialeto") :keywordize-keys true)
              eco (js->clj (aget ud "eco") :keywordize-keys true)]
          (cond
            d (state/selecionar! d)
            eco (state/focar-em! (:lat eco) (:lng eco))))))))

(defn- add-markers! [globe-g]
  (add-watch state/app ::markers
    (fn [_ _ old new]
      (when (and (not (:carregando new)) (:carregando old))
        (let [{:keys [dialetos ecossistemas]} new
              por-eco (group-by :eco dialetos)]
          (doseq [eco ecossistemas]
            (when-let [ds (get por-eco (:id eco))]
              (.add globe-g (g/make-eco-sprite eco (count ds)))))
          ;; Agrupar dialetos por localização resolvida (coordenadas reais)
          (let [dialetos-com-coords
                (for [d dialetos
                      :let [eco (first (filter #(= (:id %) (:eco d)) ecossistemas))
                            coords (or (get g/pais->coords (:pais d))
                                       {:lat (:lat eco) :lng (:lng eco)})]]
                  (assoc d :resolved-coords coords))
                por-local (group-by :resolved-coords dialetos-com-coords)]
            (doseq [[{:keys [lat lng]} ds] por-local]
              (let [n (count ds)]
                (doseq [[i d] (map-indexed vector ds)]
                  (.add globe-g (g/make-dialeto-sprite d ecossistemas lat lng i n)))))))))))

(defn build-scene! []
  (let [canvas    (js/document.getElementById "globe-canvas")
        renderer  (doto (THREE/WebGLRenderer. #js{:canvas canvas :antialias true})
                    (.setSize js/window.innerWidth js/window.innerHeight)
                    (.setPixelRatio (min (.-devicePixelRatio js/window) 2))
                    (.setClearColor 0x020817 1))
        scene     (THREE/Scene.)
        camera    (doto (THREE/PerspectiveCamera. 42
                          (/ js/window.innerWidth js/window.innerHeight) 0.1 10000)
                    (-> .-position (.set 0 0 280)))
        globe-g   (THREE/Group.)
        raycaster (THREE/Raycaster.)]

    (.add scene globe-g)
    ;; globo com textura foto-realista (terra + oceanos)
    (.add globe-g (THREE/Mesh.
                   (THREE/SphereGeometry. g/GLOBE-RADIUS 72 72)
                   (THREE/MeshPhongMaterial.
                    #js{:map               (g/earth-texture)
                        :shininess         12
                        :specular          (THREE/Color. 0x224466)
                        :emissive          (THREE/Color. 0x030d1a)
                        :emissiveIntensity 0.25})))
    ;; overlay de grade lat/lng transparente
    (.add globe-g (THREE/Mesh.
                   (THREE/SphereGeometry. (* g/GLOBE-RADIUS 1.001) 72 72)
                   (THREE/MeshBasicMaterial.
                    #js{:map         (g/grid-overlay-texture)
                        :transparent true
                        :opacity     1.0
                        :depthWrite  false})))
    (.add globe-g (g/make-atmosphere g/GLOBE-RADIUS))
    (.add scene (g/make-stars 12000 3200))
    (.add scene (THREE/AmbientLight. 0x0d1f44 1.2))
    (.add scene (doto (THREE/DirectionalLight. 0x5588cc 1.4)
                  (-> .-position (.set 1.5 0.4 1))))

    (add-markers! globe-g)

    (.addEventListener canvas "mousedown" on-mousedown)
    (.addEventListener canvas "mousemove" #(on-mousemove % globe-g raycaster camera))
    (.addEventListener js/window "mouseup" on-mouseup)
    (.addEventListener canvas "click" #(on-click % globe-g raycaster camera))

    (.addEventListener js/window "resize"
      (fn []
        (.setSize ^js renderer js/window.innerWidth js/window.innerHeight)
        (set! (.-aspect ^js camera) (/ js/window.innerWidth js/window.innerHeight))
        (.updateProjectionMatrix ^js camera)))

    {:renderer renderer :scene scene :camera camera :globe-g globe-g}))

(defn animation-loop! [{:keys [renderer scene camera globe-g]}]
  (let [t     (/ (.now js/Date) 1000)
        st    @state/app]
    (if-let [{:keys [lat lng]} (:foco st)]
      (let [target-ry (- (* 0.5 js/Math.PI)
                         (* (+ lng 180) (/ js/Math.PI 180)))
            target-rx (* lat (/ js/Math.PI 180))
            curr-ry   (.. ^js globe-g -rotation -y)
            curr-rx   (.. ^js globe-g -rotation -x)
            dy        (* (- target-ry curr-ry) 0.07)
            dx        (* (- target-rx curr-rx) 0.07)]
        (set! (.. ^js globe-g -rotation -y) (+ curr-ry dy))
        (set! (.. ^js globe-g -rotation -x) (+ curr-rx dx))
        (when (and (< (js/Math.abs (- target-ry curr-ry)) 0.004)
                   (< (js/Math.abs (- target-rx curr-rx)) 0.004))
          (swap! state/app dissoc :foco)))
      (when (:auto-rotate st)
        (set! (.. ^js globe-g -rotation -y)
              (+ (.. ^js globe-g -rotation -y) 0.0018))))
    (doseq [child (array-seq (.-children ^js globe-g))
            :when (instance? THREE/Sprite child)]
      (let [base (aget (.-userData ^js child) "base")
            d    (js->clj (aget (.-userData ^js child) "dialeto") :keywordize-keys true)
            sel? (= d (:selecionado st))
            p    (if sel?
                   (+ 1 (* 0.45 (js/Math.sin (* t 5))))
                   (+ 1 (* 0.07 (js/Math.sin (* t 1.6)))))]
        (.set (.-scale ^js child) (* base p) (* base p) 1)))
    (.render ^js renderer scene camera)))

(defn init! []
  (let [ctx (build-scene!)]
    (reset! scene-ref ctx)
    (data/fetch-dialects! "data/dialects.edn")
    (-> (.createRoot rdom-client (js/document.getElementById "ui-root"))
        (.render (r/as-element [ui/root])))
    (letfn [(loop []
              (js/requestAnimationFrame loop)
              (animation-loop! ctx))]
      (loop))))
