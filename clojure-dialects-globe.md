# clojure-dialects-globe

> *"Built with four Clojure dialects. One for each platform it runs on."*

Uma visualização 3D interativa dos dialetos do Clojure e seus ecossistemas, demonstrando nativamente a proposta que representa: **um único paradigma compilando para qualquer plataforma**.

---

## Tese

O projeto é recursivo por natureza. A visualização *sobre* Clojure compilar para todo lugar *é* Clojure compilando para todo lugar. Cada camada usa o dialeto correto para o seu contexto:

| Dialeto        | Plataforma alvo              | Papel no sistema              |
|----------------|------------------------------|-------------------------------|
| Babashka       | CLI / CI/CD                  | Pipeline de dados, scraping   |
| Clojure JVM    | Servidor                     | API HTTP, cache, Transit      |
| ClojureScript  | Browser                      | Globo 3D via Three.js (npm)   |
| ClojureDart    | iOS / Android / Desktop      | Globo 3D via three_js (pub)   |

ClojureDart aparece como **ponto #13** no próprio globo. Quem abre o app mobile está usando exatamente a tecnologia que o marcador ClojureDart representa.

---

## Arquitetura

```
┌──────────────────────────────────────────────────────────────┐
│                        GitHub API                            │
└───────────────────────────┬──────────────────────────────────┘
                            │ (cron / CI)
                            ▼
┌──────────────────────────────────────────────────────────────┐
│  scripts/  — Babashka                                        │
│  fetch_stars.bb  →  gera  data/dialects.edn                  │
└───────────────────────────┬──────────────────────────────────┘
                            │ (lê EDN)
                            ▼
┌──────────────────────────────────────────────────────────────┐
│  api/  — Clojure JVM                                         │
│  Reitit + Ring + Transit · porta 3000                        │
│  GET /api/dialects  →  Transit-JSON                          │
└────────────────┬─────────────────────┬───────────────────────┘
                 │ HTTP                │ HTTP
        ┌────────▼──────┐    ┌────────▼──────────┐
        │  web/         │    │  flutter/          │
        │  ClojureScript│    │  ClojureDart       │
        │  Three.js/npm │    │  three_js/pub.dev  │
        │  Browser      │    │  iOS · Android     │
        │               │    │  Desktop · Web     │
        └───────────────┘    └────────────────────┘
```

---

## Estrutura de Diretórios

```
clojure-dialects-globe/
│
├── data/
│   ├── base.edn              # Dados estáticos: coords, cores, descrições
│   └── dialects.edn          # Gerado: base + stars live do GitHub
│
├── scripts/                  # Babashka
│   ├── bb.edn
│   ├── fetch_stars.bb        # Busca stars via GitHub API
│   └── generate_data.bb      # Mescla base.edn + stars → dialects.edn
│
├── api/                      # Clojure JVM
│   ├── deps.edn
│   └── src/globe/api/
│       ├── server.clj        # Entrypoint Ring + Reitit
│       ├── routes.clj        # Definição das rotas
│       └── data.clj          # Leitura e cache do EDN
│
├── web/                      # ClojureScript
│   ├── shadow-cljs.edn
│   ├── package.json
│   ├── public/
│   │   └── index.html
│   └── src/globe/web/
│       ├── core.cljs         # Init, setup Three.js
│       ├── globe.cljs        # Cena 3D: globo, atmosfera, marcadores
│       ├── data.cljs         # Fetch Transit da API
│       ├── state.cljs        # Atoms e lógica de seleção
│       └── ui.cljs           # Reagent: painel, legenda, tooltip
│
└── flutter/                  # ClojureDart
    ├── pubspec.yaml
    └── src/globe/
        ├── core.cljd         # MaterialApp, entrypoint Flutter
        ├── scene.cljd        # Cena 3D: globo, atmosfera, marcadores
        ├── data.cljd         # Fetch HTTP + parse Transit
        ├── state.cljd        # Atoms reativos
        └── ui.cljd           # Widgets Flutter: painel, legenda
```

---

## Modelo de Dados Compartilhado

O EDN é o formato canônico. Todos os componentes partem dele.

```clojure
;; data/base.edn — mantido à mão, versionado no git
{:meta
 {:projeto  "clojure-dialects-globe"
  :versao   "1.0.0"
  :fonte    "https://github.com/clojurestar/clojure-cc"}

 :ecossistemas
 [{:id          :jvm
   :nome        "JVM"
   :cor         [245 158 11]
   :lat         38
   :lng         -95
   :descricao   "Java Virtual Machine"}
  {:id          :js
   :nome        "JavaScript"
   :cor         [251 191 36]
   :lat         51
   :lng         10
   :descricao   "Browser / Node.js / VS Code"}
  {:id          :c-cpp
   :nome        "C / C++"
   :cor         [239 68 68]
   :lat         33
   :lng         108
   :descricao   "Compilação nativa C / C++"}
  {:id          :go
   :nome        "Go"
   :cor         [20 184 166]
   :lat         28
   :lng         122
   :descricao   "Ecossistema Go"}
  {:id          :python
   :nome        "Python"
   :cor         [99 149 255]
   :lat         -15
   :lng         -52
   :descricao   "Ecossistema Python"}
  {:id          :rust
   :nome        "Rust"
   :cor         [249 115 22]
   :lat         62
   :lng         18
   :descricao   "Rust / LLVM"}
  {:id          :beam
   :nome        "BEAM"
   :cor         [168 85 247]
   :lat         38
   :lng         35
   :descricao   "Erlang / BEAM VM"}
  {:id          :dotnet
   :nome        ".NET"
   :cor         [139 92 246]
   :lat         50
   :lng         16
   :descricao   "Microsoft .NET / CLR"}
  {:id          :dart
   :nome        "Dart"
   :cor         [52 211 153]
   :lat         8
   :lng         110
   :descricao   "Dart / Flutter — mobile, desktop, web"}
  {:id          :lua
   :nome        "Lua"
   :cor         [34 197 94]
   :lat         -22
   :lng         -43
   :descricao   "Lua — criada na PUC-Rio 🇧🇷"}
  {:id          :php
   :nome        "PHP"
   :cor         [129 120 220]
   :lat         44
   :lng         14
   :descricao   "PHP ecosystem"}
  {:id          :wasm
   :nome        "Wasm"
   :cor         [148 163 184]
   :lat         -28
   :lng         133
   :descricao   "WebAssembly"}
  {:id          :lisp
   :nome        "Lisp"
   :cor         [236 72 153]
   :lat         38
   :lng         23
   :descricao   "Common Lisp / GNU Guile"}
  {:id          :outros
   :nome        "Outros"
   :cor         [107 114 128]
   :lat         5
   :lng         20
   :descricao   "Perl, AppleScript e outras plataformas"}]

 :dialetos
 [{:nome    "Clojure"
   :eco     :jvm
   :rank    1
   :host    "JVM"
   :repo    "clojure/clojure"
   :release "2026-04-22"
   :desc    "O original; Lisp dinâmico e funcional na JVM"}
  {:nome    "ClojureScript"
   :eco     :js
   :rank    2
   :host    "JavaScript"
   :repo    "clojure/clojurescript"
   :release "2026-05-07"
   :desc    "Clojure compilado para JS via Google Closure"}
  ;; ... demais 37 dialetos
  ]}
```

O campo `:stars` não existe no `base.edn`. O Babashka o injeta ao gerar `dialects.edn`.

---

## Componente 1 — Babashka (`scripts/`)

**Responsabilidade:** buscar stars atualizadas do GitHub e gerar o `dialects.edn` completo.

```clojure
;; scripts/bb.edn
{:paths ["."]}
```

```clojure
;; scripts/fetch_stars.bb
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

(defn fetch-repo-stars
  "Retorna stargazers_count para um repo 'owner/repo'."
  [repo]
  (try
    (-> (http/get (str "https://api.github.com/repos/" repo)
                  {:headers (github-headers)})
        :body
        (json/parse-string true)
        :stargazers_count)
    (catch Exception _
      (println "Aviso: falha ao buscar" repo)
      nil)))

(defn enrich-dialeto
  "Adiciona :stars ao mapa de um dialeto."
  [{:keys [repo] :as dialeto}]
  (let [stars (when repo (fetch-repo-stars repo))]
    (cond-> dialeto
      stars (assoc :stars stars))))

;; Entry point
(let [base     (edn/read-string (slurp "data/base.edn"))
      enriquecidos (->> (:dialetos base)
                        (mapv enrich-dialeto))
      resultado (-> base
                    (assoc :dialetos enriquecidos)
                    (assoc-in [:meta :atualizado-em] (str (java.time.LocalDate/now))))]
  (spit "data/dialects.edn" (pr-str resultado))
  (println "✓ data/dialects.edn gerado com"
           (count enriquecidos) "dialetos"))
```

**Execução:**
```bash
GITHUB_TOKEN=ghp_... bb scripts/fetch_stars.bb
```

**GitHub Actions (cron diário):**
```yaml
# .github/workflows/update-data.yml
name: Update dialect stars

on:
  schedule:
    - cron: "0 6 * * *"   # 06:00 UTC diariamente
  workflow_dispatch:

jobs:
  update:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: turtlequeue/setup-babashka@v1
        with: { babashka-version: "1.12.218" }
      - run: bb scripts/fetch_stars.bb
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
      - uses: stefanzweifel/git-auto-commit-action@v5
        with:
          commit_message: "data: atualiza stars [skip ci]"
          file_pattern: "data/dialects.edn"
```

---

## Componente 2 — Clojure JVM (`api/`)

**Responsabilidade:** servir `dialects.edn` via HTTP com encoding Transit, CORS e cache em memória.

```clojure
;; api/deps.edn
{:paths ["src"]
 :deps
 {org.clojure/clojure          {:mvn/version "1.12.4"}
  metosin/reitit               {:mvn/version "0.7.2"}
  ring/ring-jetty-adapter      {:mvn/version "1.12.2"}
  ring-cors/ring-cors          {:mvn/version "0.1.13"}
  com.cognitect/transit-clj    {:mvn/version "1.0.333"}
  org.clojure/tools.logging    {:mvn/version "1.3.0"}
  ch.qos.logback/logback-classic {:mvn/version "1.5.6"}}
 :aliases
 {:dev {:extra-deps {com.bhauman/rebel-readline {:mvn/version "0.1.4"}}}
  :run {:main-opts ["-m" "globe.api.server"]}}}
```

```clojure
;; src/globe/api/data.clj
(ns globe.api.data
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]))

(def ^:private cache (atom nil))

(defn load-dialects!
  "Carrega dialects.edn e armazena em cache. Thread-safe."
  []
  (let [f (io/file "data/dialects.edn")]
    (reset! cache {:data      (edn/read-string (slurp f))
                   :loaded-at (System/currentTimeMillis)})))

(defn get-dialects
  "Retorna dados em cache, recarregando se o arquivo foi modificado."
  []
  (let [f    (io/file "data/dialects.edn")
        mtm  (.lastModified f)
        last (:loaded-at @cache 0)]
    (when (> mtm last) (load-dialects!))
    (:data @cache)))
```

```clojure
;; src/globe/api/routes.clj
(ns globe.api.routes
  (:require [globe.api.data      :as data]
            [cognitect.transit   :as transit]
            [clojure.java.io     :as io]))

(defn transit-response [body]
  (let [out (io/ByteArrayOutputStream.)
        w   (transit/writer out :json)]
    (transit/write w body)
    {:status  200
     :headers {"Content-Type"                 "application/transit+json"
               "Cache-Control"                "public, max-age=3600"
               "Access-Control-Allow-Origin"  "*"}
     :body    (.toString out "UTF-8")}))

(def routes
  [["/api/dialects"
    {:get  (fn [_] (transit-response (data/get-dialects)))
     :name ::dialects}]

   ["/api/health"
    {:get  (fn [_] {:status 200 :body "ok"})
     :name ::health}]])
```

```clojure
;; src/globe/api/server.clj
(ns globe.api.server
  (:require [globe.api.data    :as data]
            [globe.api.routes  :as routes]
            [reitit.ring       :as ring]
            [ring.adapter.jetty :as jetty]
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
```

**Execução:**
```bash
cd api && clojure -M:run
```

---

## Componente 3 — ClojureScript (`web/`)

**Responsabilidade:** renderizar o globo 3D interativo no browser via Three.js.

### Configuração

```clojure
;; web/shadow-cljs.edn
{:source-paths ["src"]
 :dependencies [[reagent "2.0.0"]
                [re-frame "1.4.0"]
                [com.cognitect/transit-cljs "0.8.280"]]
 :builds
 {:globe
  {:target     :browser
   :output-dir "public/js"
   :modules    {:main {:init-fn globe.web.core/init!}}
   :devtools   {:http-root "public"
                :http-port 3000
                :repl-preamble "(enable-console-print!)"}}}}
```

```json
// web/package.json
{
  "name": "clojure-dialects-globe-web",
  "dependencies": {
    "three": "^0.175.0",
    "shadow-cljs": "^2.28.0"
  }
}
```

### Helpers geométricos

```clojure
;; src/globe/web/globe.cljs
(ns globe.web.globe
  (:require ["three" :as THREE]))

(def GLOBE-RADIUS 100)

(defn ll->xyz
  "Latitude/longitude para vetor 3D na superfície do globo."
  [lat lng r]
  (let [phi   (* (- 90 lat) (/ js/Math.PI 180))
        theta (* (+ lng 180) (/ js/Math.PI 180))]
    (THREE/Vector3.
     (* (- r) (js/Math.sin phi) (js/Math.cos theta))
     (*    r  (js/Math.cos phi))
     (*    r  (js/Math.sin phi) (js/Math.sin theta)))))

(defn grid-texture
  "Canvas texture com grade lat/lng para o globo."
  []
  (let [w 2048 h 1024
        cv (js/document.createElement "canvas")
        _  (set! (.-width cv) w)
        _  (set! (.-height cv) h)
        cx (.getContext cv "2d")]
    (set! (.-fillStyle cx) "#030b18")
    (.fillRect cx 0 0 w h)
    ;; Grade menor (10°)
    (set! (.-strokeStyle cx) "rgba(16,70,150,0.09)")
    (set! (.-lineWidth cx) 0.6)
    (doseq [t (range -80 81 10) :when (not= 0 (mod t 30))]
      (let [y (* (/ (- 90 t) 180) h)]
        (.beginPath cx) (.moveTo cx 0 y) (.lineTo cx w y) (.stroke cx)))
    ;; Grade maior (30°)
    (set! (.-strokeStyle cx) "rgba(28,110,200,0.22)")
    (set! (.-lineWidth cx) 1.1)
    (doseq [t (range -60 61 30) :when (not= t 0)]
      (let [y (* (/ (- 90 t) 180) h)]
        (.beginPath cx) (.moveTo cx 0 y) (.lineTo cx w y) (.stroke cx)))
    (doseq [g (range -150 181 30)]
      (let [x (* (/ (+ g 180) 360) w)]
        (.beginPath cx) (.moveTo cx x 0) (.lineTo cx x h) (.stroke cx)))
    ;; Equador
    (set! (.-strokeStyle cx) "rgba(50,160,255,0.55)")
    (set! (.-lineWidth cx) 1.5)
    (.beginPath cx) (.moveTo cx 0 (* h 0.5)) (.lineTo cx w (* h 0.5)) (.stroke cx)
    (THREE/CanvasTexture. cv)))

(defn glow-texture
  "Sprite com gradiente radial para marcadores de ecossistema."
  [[r g b] size]
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

(defn make-atmosphere
  "Esfera de atmosfera com shader GLSL de rim glow."
  [radius]
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

(defn make-stars
  "Campo de estrelas como BufferGeometry Points."
  [count spread]
  (let [geo (THREE/BufferGeometry.)
        pos (js/Float32Array. (* count 3))]
    (dotimes [i (* count 3)]
      (aset pos i (* (- (js/Math.random) 0.5) spread)))
    (.setAttribute geo "position" (THREE/BufferAttribute. pos 3))
    (THREE/Points.
     geo
     (THREE/PointsMaterial.
      #js{:color 0xffffff :size 0.55 :transparent true :opacity 0.7}))))

(defn make-eco-sprite
  "Sprite glow para território de ecossistema."
  [{:keys [cor lat lng]} n-dialetos]
  (let [mat   (THREE/SpriteMaterial.
               #js{:map      (glow-texture cor 256)
                   :blending THREE/AdditiveBlending
                   :transparent true :depthWrite false})
        sp    (THREE/Sprite. mat)
        base  (+ 18 (* n-dialetos 5.5))]
    (.set (.-scale sp) base base 1)
    (.copy (.-position sp) (ll->xyz lat lng (+ GLOBE-RADIUS 0.3)))
    (set! (.. sp -userData -base) base)
    sp))

(defn dialeto-scale
  "Tamanho base do marcador proporcional a sqrt(stars)."
  [stars]
  (+ 2.5 (* 0.75 (js/Math.pow (or stars 30) 0.38))))

(defn make-dialeto-sprite
  "Sprite glow para marcador individual de dialeto."
  [{:keys [eco stars] :as dialeto} ecossistemas index total]
  (let [eco-data  (first (filter #(= (:id %) eco) ecossistemas))
        [r g b]   (:cor eco-data)
        angle     (+ (* (/ index total) 2 js/Math.PI)
                     (* (.charCodeAt (name eco) 0) 0.22))
        ring      (+ 4 (* index 0.85))
        lat       (+ (:lat eco-data) (* (js/Math.cos angle) ring))
        lng       (+ (:lng eco-data) (* (js/Math.sin angle) ring 1.45))
        mat       (THREE/SpriteMaterial.
                   #js{:map      (glow-texture [r g b] 64)
                       :blending THREE/AdditiveBlending
                       :transparent true :depthWrite false})
        sp        (THREE/Sprite. mat)
        base      (dialeto-scale stars)]
    (.set (.-scale sp) base base 1)
    (.copy (.-position sp) (ll->xyz lat lng (+ GLOBE-RADIUS 1.8)))
    (set! (.. sp -userData -dialeto) dialeto)
    (set! (.. sp -userData -eco)     eco-data)
    (set! (.. sp -userData -base)    base)
    sp))
```

### Estado e lógica de seleção

```clojure
;; src/globe/web/state.cljs
(ns globe.web.state
  (:require [reagent.core :as r]))

(defonce app
  (r/atom {:dialetos     []
           :ecossistemas []
           :selecionado  nil
           :auto-rotate  true
           :carregando   true}))

(defn selecionar! [dialeto]
  (swap! app assoc :selecionado dialeto))

(defn fechar-painel! []
  (swap! app assoc :selecionado nil))

(defn dialetos-por-eco [eco-id]
  (->> (:dialetos @app)
       (filter #(= (:eco %) eco-id))))

(defn n-dialetos-por-eco [eco-id]
  (count (dialetos-por-eco eco-id)))
```

### Fetch de dados via Transit

```clojure
;; src/globe/web/data.cljs
(ns globe.web.data
  (:require [cognitect.transit :as t]
            [globe.web.state   :as state]))

(defn fetch-dialects! [api-base]
  (-> (js/fetch (str api-base "/api/dialects"))
      (.then #(.text %))
      (.then (fn [body]
               (let [reader (t/reader :json)
                     data   (t/read reader body)]
                 (swap! state/app assoc
                        :dialetos     (:dialetos data)
                        :ecossistemas (:ecossistemas data)
                        :carregando   false))))
      (.catch (fn [e]
                (js/console.error "Falha ao carregar dados" e)))))
```

### Entrypoint da cena

```clojure
;; src/globe/web/core.cljs
(ns globe.web.core
  (:require ["three"           :as THREE]
            [globe.web.globe   :as g]
            [globe.web.state   :as state]
            [globe.web.data    :as data]
            [globe.web.ui      :as ui]
            [reagent.dom       :as rdom]))

(defonce scene-ref (atom nil))

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
        raycaster (THREE/Raycaster.)
        mouse     (THREE/Vector2.)]

    ;; Cena estática
    (.add scene globe-g)
    (.add globe-g (THREE/Mesh.
                   (THREE/SphereGeometry. g/GLOBE-RADIUS 72 72)
                   (THREE/MeshPhongMaterial.
                    #js{:map              (g/grid-texture)
                        :shininess        8
                        :specular         (THREE/Color. 0x112244)
                        :emissive         (THREE/Color. 0x020810)
                        :emissiveIntensity 0.4})))
    (.add globe-g (g/make-atmosphere g/GLOBE-RADIUS))
    (.add scene (g/make-stars 12000 3200))
    (.add scene (THREE/AmbientLight. 0x0d1f44 1.2))
    (.add scene (doto (THREE/DirectionalLight. 0x5588cc 1.4)
                  (-> .-position (.set 1.5 0.4 1))))

    ;; Marcadores — adicionados quando os dados chegam
    (add-watch state/app ::markers
      (fn [_ _ old new]
        (when (and (not (:carregando new)) (:carregando old))
          (let [{:keys [dialetos ecossistemas]} new
                por-eco (group-by :eco dialetos)]
            ;; Territórios de ecossistema
            (doseq [eco ecossistemas]
              (when-let [ds (get por-eco (:id eco))]
                (.add globe-g (g/make-eco-sprite eco (count ds)))))
            ;; Dialetos individuais
            (doseq [eco ecossistemas]
              (let [ds  (get por-eco (:id eco) [])
                    n   (count ds)]
                (doseq [[i d] (map-indexed vector ds)]
                  (.add globe-g (g/make-dialeto-sprite d ecossistemas i n)))))))))

    {:renderer renderer :scene scene :camera camera
     :globe-g globe-g :raycaster raycaster :mouse mouse}))

(defn animation-loop! [{:keys [renderer scene camera globe-g] :as ctx}]
  (let [t     (/ (.now js/Date) 1000)
        state @state/app]
    (when (:auto-rotate state)
      (set! (.. globe-g -rotation -y)
            (+ (.. globe-g -rotation -y) 0.0018)))
    ;; Pulso nos sprites
    (doseq [child (array-seq (.-children globe-g))
            :when (instance? THREE/Sprite child)]
      (let [base (.. child -userData -base)
            sel? (= (.. child -userData -dialeto) (:selecionado state))
            p    (if sel?
                   (+ 1 (* 0.45 (js/Math.sin (* t 5))))
                   (+ 1 (* 0.07 (js/Math.sin (* t 1.6)))))]
        (.set (.-scale child) (* base p) (* base p) 1)))
    (.render renderer scene camera)))

(defn init! []
  (let [ctx (build-scene!)]
    (reset! scene-ref ctx)
    (data/fetch-dialects! "http://localhost:3000")
    (rdom/render [ui/root] (js/document.getElementById "ui-root"))
    (letfn [(loop []
              (js/requestAnimationFrame loop)
              (animation-loop! ctx))]
      (loop))))
```

---

## Componente 4 — ClojureDart (`flutter/`)

**Responsabilidade:** renderizar o mesmo globo nativamente em iOS, Android e Desktop via Flutter + `three_js`.

### Configuração

```yaml
# flutter/pubspec.yaml
name: clojure_dialects_globe
description: Clojure dialect globe — Flutter native

environment:
  sdk: ">=3.0.0 <4.0.0"
  flutter: ">=3.22.0"

dependencies:
  flutter:
    sdk: flutter
  three_js: ^0.0.29           # Three.js port para Dart/Flutter
  http: ^1.2.0                # Fetch HTTP
  msgpack_dart: ^2.2.0        # Transit-JSON parsing
```

### Helpers geométricos (espelho do ClojureScript)

```clojure
;; flutter/src/globe/scene.cljd
(ns globe.scene
  (:require ["package:three_js/three_js.dart" :as t3]
            ["dart:math" :as math]))

(def GLOBE-RADIUS 100.0)

(defn ll->xyz
  "Lat/lng para Vector3 na superfície do globo — mesmo algoritmo do CLJS."
  [lat lng r]
  (let [phi   (* (- 90.0 lat) (/ math/pi 180.0))
        theta (* (+ lng 180.0) (/ math/pi 180.0))]
    (t3/Vector3.
     (* (- r) (math/sin phi) (math/cos theta))
     (*    r  (math/cos phi))
     (*    r  (math/sin phi) (math/sin theta)))))

(defn make-globe []
  (t3/Mesh.
   (t3/SphereGeometry. GLOBE-RADIUS 72 72)
   (t3/MeshPhongMaterial.
    #dart{:color     (t3/Color. 0x04101e)
          :shininess 8.0})))

(defn make-atmosphere [radius]
  (t3/Mesh.
   (t3/SphereGeometry. (* radius 1.065) 48 48)
   (t3/ShaderMaterial.
    #dart{:vertexShader
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
          :side        t3/BackSide
          :blending    t3/AdditiveBlending
          :transparent true
          :depthWrite  false})))

(defn dialeto-scale [stars]
  (+ 2.5 (* 0.75 (math/pow (or stars 30.0) 0.38))))
```

### Fetch de dados

```clojure
;; flutter/src/globe/data.cljd
(ns globe.data
  (:require ["package:http/http.dart" :as http]
            ["dart:convert" :as convert]
            [globe.state :as state]))

(defn fetch-dialects!
  "Busca dados da API JVM e atualiza o state atom."
  [api-base]
  (dart:async
   (let [url      (Uri/parse (str api-base "/api/dialects"))
         response (await (http/get url))
         body     (.-body response)
         ;; Transit-JSON simplificado — em produção usar codec completo
         data     (convert/jsonDecode body)]
     (swap! state/app assoc
            :dialetos     (get data "dialetos")
            :ecossistemas (get data "ecossistemas")
            :carregando   false))))
```

### Widget Flutter com Three.js

```clojure
;; flutter/src/globe/core.cljd
(ns globe.core
  (:require ["package:flutter/material.dart"  :as m]
            ["package:flutter/widgets.dart"   :as w]
            ["package:three_js/three_js.dart" :as t3]
            [globe.scene :as scene]
            [globe.state :as state]
            [globe.data  :as data]))

(defn globe-widget []
  (let [three (t3/ThreeJS.)]
    ;; Setup da cena Three.js dentro do widget Flutter
    (set! (.-init three)
          (fn []
            (let [globe-g (t3/Group.)]
              (.add (.-scene three) globe-g)
              (.add globe-g (scene/make-globe))
              (.add globe-g (scene/make-atmosphere scene/GLOBE-RADIUS))
              (.add (.-scene three)
                    (t3/AmbientLight. (t3/Color. 0x0d1f44) 1.2))
              (set! (.-isometric (.-camera three)) false)
              (.set (.-position (.-camera three)) 0.0 0.0 280.0)
              (set! (.. three -scene -background)
                    (t3/Color. 0x020817))
              (swap! state/app assoc :globe-group globe-g))))

    (set! (.-animate three)
          (fn []
            (let [gg (:globe-group @state/app)]
              (when gg
                (set! (.. gg -rotation -y)
                      (+ (.. gg -rotation -y) 0.0018))))))

    (m/Scaffold.
     (.-body m/Scaffold)
     (t3/threeBuilder three))))

(defn main []
  (m/runApp
   (m/MaterialApp.
    (.-title m/MaterialApp) "Clojure Dialects Globe"
    (.-theme m/MaterialApp)
    (m/ThemeData.
     (.-brightness m/ThemeData) m/Brightness.dark)
    (.-home m/MaterialApp) (globe-widget))))
```

---

## Infraestrutura e Deploy

### Desenvolvimento local

```bash
# Terminal 1 — API JVM
cd api && clojure -M:run

# Terminal 2 — Web CLJS (hot reload)
cd web && npx shadow-cljs watch globe

# Terminal 3 — Flutter
cd flutter && flutter run -d chrome        # web
             flutter run -d <device-id>   # iOS/Android
             flutter run -d macos          # Desktop
```

### Produção

```
┌─────────────────────────────────────────────────────────┐
│  VPS / OCI Free Tier (nexus)                            │
│                                                         │
│  api/          → systemd → porta 3000 (interno)         │
│  web/public/   → nginx   → porta 443  (HTTPS)          │
│                  proxy /api/ → localhost:3000           │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  App stores / distribuição                              │
│                                                         │
│  iOS / Android  → flutter build appbundle              │
│  macOS / Linux  → flutter build <target>               │
│  Web Flutter    → flutter build web                    │
└─────────────────────────────────────────────────────────┘
```

**systemd para a API:**
```ini
# /etc/systemd/system/clojure-globe-api.service
[Unit]
Description=Clojure Dialects Globe API
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/clojure-dialects-globe/api
ExecStart=/usr/local/bin/clojure -M:run
Restart=on-failure
Environment=JAVA_OPTS="-Xms128m -Xmx256m"

[Install]
WantedBy=multi-user.target
```

---

## Ordem de Implementação

### Sprint 1 — Fundação de dados (Babashka)
- `data/base.edn` com todos os 39 dialetos e 14 ecossistemas
- `scripts/fetch_stars.bb` funcionando com GITHUB_TOKEN
- `data/dialects.edn` sendo gerado corretamente
- GitHub Action de cron configurada

### Sprint 2 — API JVM
- Servidor Ring + Reitit servindo Transit-JSON
- Cache com invalidação por mtime do arquivo
- Endpoint `/api/health` para monitoring
- Deploy no nexus via systemd

### Sprint 3 — Web (ClojureScript)
- Setup shadow-cljs + Reagent
- Globo base: esfera + grid texture + atmosfera GLSL
- Stars + câmera com drag manual
- Fetch dos dados da API
- Marcadores de ecossistema (sprites glow)
- Marcadores de dialeto individuais
- Interação: hover tooltip, click → painel lateral
- Painel com info do dialeto, barra de stars, release

### Sprint 4 — Flutter (ClojureDart)
- Setup ClojureDart + three_js via pubspec
- Paridade visual com a versão web
- Fetch HTTP da mesma API JVM
- UI Flutter nativa: painel lateral, legenda
- Build iOS, Android e macOS

### Sprint 5 — Polimento e OSS
- README com vídeo demonstrando as 4 plataformas
- Docstring em todos os namespaces públicos
- CHANGELOG.md
- GitHub Discussions aberto
- Post técnico: "Building a multi-platform globe with four Clojure dialects"

---

## Mensagem do Projeto

```
Built with four Clojure dialects. One for each platform it runs on.

  Babashka    →  CLI scripting       (data pipeline)
  Clojure JVM →  Server              (API, cache)
  ClojureScript→ Browser             (Three.js globe)
  ClojureDart →  iOS/Android/Desktop (three_js globe)

No JavaScript. No Dart. No Python. Just Clojure.
```
