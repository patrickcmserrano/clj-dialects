(ns globe.web.globe
  (:require ["three" :as THREE]))

(def GLOBE-RADIUS 100)

(defn ll->xyz [lat lng r]
  (let [phi   (* (- 90 lat) (/ js/Math.PI 180))
        theta (* (+ lng 180) (/ js/Math.PI 180))]
    (THREE/Vector3.
     (* (- r) (js/Math.sin phi) (js/Math.cos theta))
     (*    r  (js/Math.cos phi))
     (*    r  (js/Math.sin phi) (js/Math.sin theta)))))

(defn earth-texture []
  (let [loader (THREE/TextureLoader.)
        tex    (.load loader "/textures/earth.jpg")]
    (aset tex "colorSpace" (.-SRGBColorSpace THREE))
    tex))

(defn grid-overlay-texture []
  (let [w 2048 h 1024
        cv (js/document.createElement "canvas")]
    (aset cv "width" w)
    (aset cv "height" h)
    (let [cx (.getContext cv "2d")]
      (set! (.-strokeStyle cx) "rgba(100,200,255,0.25)")
      (set! (.-lineWidth cx) 0.7)
      (doseq [t (range -80 81 10)]
        (let [y (* (/ (- 90 t) 180) h)]
          (.beginPath cx) (.moveTo cx 0 y) (.lineTo cx w y) (.stroke cx)))
      (doseq [g (range -150 181 30)]
        (let [x (* (/ (+ g 180) 360) w)]
          (.beginPath cx) (.moveTo cx x 0) (.lineTo cx x h) (.stroke cx)))
      ;; equador destacado
      (set! (.-strokeStyle cx) "rgba(100,220,255,0.55)")
      (set! (.-lineWidth cx) 1.5)
      (.beginPath cx) (.moveTo cx 0 (* h 0.5)) (.lineTo cx w (* h 0.5)) (.stroke cx)
      (let [tex (THREE/CanvasTexture. cv)]
        (aset tex "colorSpace" (.-SRGBColorSpace THREE))
        tex))))

(defn glow-texture [[r g b] size]
  (let [cv  (js/document.createElement "canvas")
        _   (set! (.-width cv) size)
        _   (set! (.-height cv) size)
        cx  (.getContext cv "2d")
        m   (/ size 2)
        grd (.createRadialGradient cx m m 0 m m m)]
    (.addColorStop grd 0    "rgba(255,255,255,0.95)")
    (.addColorStop grd 0.08 (str "rgba(" r "," g "," b ",0.9)"))
    (.addColorStop grd 0.35 (str "rgba(" r "," g "," b ",0.4)"))
    (.addColorStop grd 1    (str "rgba(" r "," g "," b ",0)"))
    (set! (.-fillStyle cx) grd)
    (.fillRect cx 0 0 size size)
    (THREE/CanvasTexture. cv)))

(defn make-atmosphere [radius]
  (THREE/Mesh.
   (THREE/SphereGeometry. (* radius 1.065) 48 48)
   (THREE/ShaderMaterial.
    #js{:vertexShader
        "varying vec3 vN;
         void main(){
           vN=normalize(normalMatrix*normal);
           gl_Position=projectionMatrix*modelViewMatrix*vec4(position,1.);
         }"
        :fragmentShader
        "varying vec3 vN;
         void main(){
           float i=pow(max(0.,0.76-dot(vN,vec3(0.,0.,1.))),2.4);
           gl_FragColor=vec4(0.1,0.52,1.,i*0.85);
         }"
        :side        THREE/BackSide
        :blending    THREE/AdditiveBlending
        :transparent true
        :depthWrite  false})))

(defn make-stars [n spread]
  (let [geo (THREE/BufferGeometry.)
        pos (js/Float32Array. (* n 3))]
    (dotimes [i (* n 3)]
      (aset pos i (* (- (js/Math.random) 0.5) spread)))
    (.setAttribute geo "position" (THREE/BufferAttribute. pos 3))
    (THREE/Points.
     geo
     (THREE/PointsMaterial.
      #js{:color 0xffffff :size 0.55 :transparent true :opacity 0.7}))))

(defn make-eco-sprite [eco n-dialetos]
  (let [{:keys [cor lat lng]} eco
        mat  (THREE/SpriteMaterial.
               #js{:map      (glow-texture cor 256)
                   :blending THREE/AdditiveBlending
                   :transparent true :depthWrite false})
        sp   (THREE/Sprite. mat)
        base (+ 8 (* 2 (js/Math.sqrt n-dialetos)))]
    (.set (.-scale sp) base base 1)
    (.copy (.-position sp) (ll->xyz lat lng (+ GLOBE-RADIUS 0.3)))
    (set! (.. sp -userData -base) base)
    (set! (.. sp -userData -eco) (clj->js eco))
    sp))

(defn dialeto-scale [stars]
  (+ 2.5 (* 0.75 (js/Math.pow (or stars 30) 0.38))))

(def pais->coords
  {"Estados Unidos" {:lat 40.7  :lng -74.0}
   "Países Baixos"  {:lat 52.4  :lng  4.9}
   "Suécia"         {:lat 59.3  :lng 18.1}
   "Rússia"         {:lat 55.8  :lng 37.6}
   "Portugal"       {:lat 38.7  :lng -9.1}
   "Argentina"      {:lat -34.6 :lng -58.4}
   "França"         {:lat 48.8  :lng  2.3}
   "Turquia"        {:lat 39.0  :lng 35.0}
   "Alemanha"       {:lat 52.5  :lng 13.4}
   "Polônia"        {:lat 52.2  :lng 21.0}
   "China"          {:lat 35.0  :lng 105.0}
   "Japão"          {:lat 36.0  :lng 138.0}
   "Austrália"      {:lat -25.0 :lng 133.0}
   "Brasil"         {:lat -22.9 :lng -43.2}
   "Canadá"         {:lat 49.3  :lng -123.1}})

(defn make-dialeto-sprite [{:keys [eco stars] :as dialeto} ecossistemas base-lat base-lng index total]
  (let [eco-data  (first (filter #(= (:id %) eco) ecossistemas))
        [r g b]   (:cor eco-data)
        angle     (+ (* (/ index total) 2 js/Math.PI)
                     (* (.charCodeAt (name eco) 0) 0.22))
        ring      (+ 2.5 (* 0.9 (js/Math.sqrt total)))
        lat       (+ base-lat (* (js/Math.cos angle) ring))
        lng       (+ base-lng (* (js/Math.sin angle) ring 1.45))
        mat       (THREE/SpriteMaterial.
                   #js{:map      (glow-texture [r g b] 64)
                       :blending THREE/AdditiveBlending
                       :transparent true :depthWrite false})
        sp        (THREE/Sprite. mat)
        base      (dialeto-scale stars)]
    (.set (.-scale sp) base base 1)
    (.copy (.-position sp) (ll->xyz lat lng (+ GLOBE-RADIUS 1.8)))
    (set! (.. sp -userData -dialeto) (clj->js dialeto))
    (set! (.. sp -userData -ecoData) (clj->js eco-data))
    (set! (.. sp -userData -base) base)
    sp))
