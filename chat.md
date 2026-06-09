

Skip to content .clj .fnl .lpy .jank .lg .cljs .carp Search clojurestar/clojure-cc 7 2 Clojure Dialects Try Dialects Browser REPL Table of contents Other Curated Lists Notes ★ Tags Dialect Host Release Description 1 10927 Clojure JVM 2026-04-22 dd395ea The original; dynamic, functional Lisp on t

pasted

Eu gostaria de visualizar os dialetos clojure como territorios em um mapa global usando threejs em uma experiencia imersia de dados

Jun 7

Visualizou mapa imersivo de dialetos Clojure com Three.js
Visualizou mapa imersivo de dialetos Clojure com Three.js
Aqui está o globo 3D imersivo. O que foi construído:

Visuais

Globo com textura procedural de grade lat/lng, linhas do equador, meridianos e círculos polares
Atmosfera dupla via GLSL shaders (rim glow azul elétrico no BackSide + inner glow no FrontSide)
10 mil estrelas com rotação paralaxe lenta
Dados mapeados geograficamente com lógica temática — Lua em lat/lng do Rio de Janeiro (PUC-Rio, onde foi criada 🇧🇷), Go no Leste Asiático, Rust na Escandinávia, BEAM no Oriente Médio (telecom), JVM na América do Norte, etc.

Interação

Arrastar para rotacionar (mouse e touch), auto-rotate retoma após 3.5s
Hover → tooltip com nome e stars
Click em qualquer dialeto → painel lateral desliza com rank, ecossistema, host, descrição, barra de stars relativa ao Clojure original, e data da última release
Legend clicável no canto inferior esquerdo para ir direto ao primeiro dialeto do ecossistema
Codificação visual

Tamanho dos marcadores proporcional a √stars
Territórios pulsam com ritmo diferente por ecossistema
Dialeto selecionado pulsa 5× mais rápido e 45% maior

Clojure dialects globe
Code · HTML 
Como poderiamos criar isso exclusivamente usando clojure?

Jun 7

Avaliou múltiplas abordagens Clojure para visualização 3D
Avaliou múltiplas abordagens Clojure para visualização 3D
Boa pergunta — três caminhos principais, com diferenças claras de tradeoff:

1. ClojureScript + Three.js (recomendado)
O caminho mais direto. Você mantém o mesmo poder visual, mas o código vira Clojure idiomático de verdade. O setup é shadow-cljs + npm:

shadow-cljs.edn

clojure
{:source-paths ["src"]
 :dependencies [[reagent "1.2.0"]]
 :builds
 {:globe
  {:target     :browser
   :output-dir "public/js"
   :modules    {:main {:init-fn globe.core/init!}}
   :devtools   {:http-root "public" :http-port 3000}}}}
package.json — Three.js via npm normal:

json
{ "dependencies": { "three": "^0.175.0" } }
Agora o grande ganho: o código muda de caráter. Veja a transformação nos padrões centrais:

Dados como EDN puro:

clojure
(def ecossistemas
  [{:id :jvm    :nome "JVM"        :cor [245 158 11] :lat  38 :lng -95}
   {:id :js     :nome "JavaScript" :cor [251 191 36] :lat  51 :lng  10}
   {:id :rust   :nome "Rust"       :cor [249 115 22] :lat  62 :lng  18}
   {:id :lua    :nome "Lua"        :cor [ 34 197 94] :lat -22 :lng -43}  ; PUC-Rio 🇧🇷
   ;; ...
   ])

(def dialetos
  [{:nome "Clojure"    :eco :jvm  :stars 10927 :rank 1 :host "JVM"}
   {:nome "Babashka"   :eco :jvm  :stars  4530 :rank 5 :host "GraalVM native"}
   {:nome "ClojureDart":eco :dart :stars  1621 :rank 13 :host "Dart / Flutter"}
   ;; ...
   ])
Helpers como funções puras (sem efeito colateral):

clojure
(defn ll->xyz
  "Lat/lng para vetor 3D no globo."
  [lat lng r]
  (let [phi   (* (- 90 lat) (/ js/Math.PI 180))
        theta (* (+ lng 180) (/ js/Math.PI 180))]
    (THREE/Vector3.
     (* (- r) (js/Math.sin phi) (js/Math.cos theta))
     (*    r  (js/Math.cos phi))
     (*    r  (js/Math.sin phi) (js/Math.sin theta)))))

(defn dialetos-por-eco [eco-id]
  (filter #(= (:eco %) eco-id) dialetos))

(defn escala-marker [stars]
  (+ 2.5 (* 0.75 (js/Math.pow (or stars 30) 0.38))))
Interop Three.js via . e new:

clojure
(defn make-globe-mesh []
  (THREE/Mesh.
   (THREE/SphereGeometry. 100 72 72)
   (THREE/MeshPhongMaterial.
    #js{:shininess        8
        :specular         (THREE/Color. 0x112244)
        :emissive         (THREE/Color. 0x020810)
        :emissiveIntensity 0.4})))

(defn make-atmosphere []
  (THREE/Mesh.
   (THREE/SphereGeometry. 106.5 48 48)
   (THREE/ShaderMaterial.
    #js{:vertexShader   atmo-vert-glsl    ; strings GLSL definidas como def
        :fragmentShader atmo-frag-glsl
        :side           THREE/BackSide
        :blending       THREE/AdditiveBlending
        :transparent    true
        :depthWrite     false})))
Estado global via atom + Reagent:

clojure
(defonce state
  (r/atom {:selecionado   nil
           :auto-rotate   true
           :painel-aberto false}))

;; Seleção imutável — troca o estado, Three.js reage no loop
(defn selecionar! [dialeto]
  (swap! state assoc
         :selecionado   dialeto
         :painel-aberto true))
Loop de animação com defn pura para os pulsos:

clojure
(defn calcular-pulso
  "Retorna escala de pulso para um sprite dado o tempo e se está selecionado."
  [t idx selecionado?]
  (if selecionado?
    (+ 1 (* 0.45 (js/Math.sin (* t 5))))
    (+ 1 (* 0.07 (js/Math.sin (+ (* t 1.6) (* idx 0.28)))))))

(defn animate! [{:keys [renderer scene camera globe-g markers] :as ctx}]
  (js/requestAnimationFrame #(animate! ctx))
  (let [t   (/ (.now js/Date) 1000)
        sel (:selecionado @state)]
    (when (:auto-rotate @state)
      (set! (.. globe-g -rotation -y)
            (+ (.. globe-g -rotation -y) 0.0018)))
    (doseq [[i sp] (map-indexed vector markers)]
      (let [base (.. sp -userData -base)
            p    (calcular-pulso t i (= (.. sp -userData -dialeto) sel))]
        (.set (.-scale sp) (* base p) (* base p) 1)))
    (.render renderer scene camera)))
Setup da cena usando -> e ->:

clojure
(defn init-scene! [canvas]
  (let [renderer (doto (THREE/WebGLRenderer. #js{:canvas canvas :antialias true})
                   (.setSize js/window.innerWidth js/window.innerHeight)
                   (.setPixelRatio (min (.-devicePixelRatio js/window) 2))
                   (.setClearColor 0x020817 1))
        scene    (THREE/Scene.)
        camera   (doto (THREE/PerspectiveCamera. 42
                         (/ js/window.innerWidth js/window.innerHeight) 0.1 10000)
                   (-> .-position (.set 0 0 280)))
        globe-g  (THREE/Group.)]
    (.add scene globe-g)
    (run! #(.add globe-g %)
          [(make-globe-mesh)
           (make-atmosphere)
           (make-stars)])
    (.add scene (THREE/AmbientLight. 0x0d1f44 1.2))
    {:renderer renderer :scene scene :camera camera :globe-g globe-g}))
2. Quil (JVM puro, sem JS)
Se o objetivo for zero JavaScript, Quil é o caminho — wrapper Clojure do Processing, com renderer P3D para 3D.

clojure
(ns globe.quil
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn setup []
  {:rot-y 0.0 :rot-x 0.15 :selecionado nil})

(defn update-state [{:keys [rot-y] :as state}]
  (assoc state :rot-y (+ rot-y 0.002)))

(defn draw-marker! [[x y z] [r g b] tamanho]
  (q/push-matrix)
  (q/translate x y z)
  (q/fill r g b 200)
  (q/no-stroke)
  (q/sphere tamanho)
  (q/pop-matrix))

(defn draw [{:keys [rot-x rot-y]}]
  (q/background 2 8 23)
  (q/ambient-light 13 31 68)
  (q/directional-light 85 136 204 1.5 0.4 1.0)
  (q/push-matrix)
  (q/rotate-x rot-x)
  (q/rotate-y rot-y)
  ;; Globo base
  (q/fill 4 16 32) (q/no-stroke)
  (q/sphere 200)
  ;; Ecossistemas
  (doseq [{:keys [lat lng cor]} ecossistemas]
    (let [{:keys [x y z]} (ll->xyz lat lng 205)]
      (draw-marker! [x y z] cor 8)))
  (q/pop-matrix))

(q/defsketch globe
  :title    "Clojure Dialects"
  :size     [1200 800]
  :setup    setup
  :update   update-state
  :draw     draw
  :renderer :p3d
  :middleware [m/fun-mode])
O tradeoff é claro: Quil roda na JVM (janela nativa, não browser), não tem shaders GLSL reais, e a qualidade visual é menor. Mas é 100% Clojure sem uma linha de JS.

Comparativo rápido
ClojureScript + Three.js	Quil (JVM)
Runtime	Browser	JVM (nativo)
Shaders GLSL	✅ total	❌
Deploy web	✅	❌ (precisa Electron)
Idiomaticidade	Alta	Alta
Visual = original	✅	Próximo
Zero JS	❌ (npm deps)	✅
Para o seu contexto — shadow-cljs, Reagent, CLJS no dia a dia — a rota ClojureScript + Three.js é a mais natural. A estrutura de dados fica em EDN puro, os helpers viram funções puras testáveis, e o estado muda via swap! em vez de mutação imperativa.

Já existe algum wrapper de threejs para cljs?

Jun 7

Investigou wrappers Three.js para ClojureScript e avaliou viabilidade
Investigou wrappers Three.js para ClojureScript e avaliou viabilidade
Existe um, e é exatamente o que você esperaria dado o ecossistema: threeagent (DougHamil/threeagent) — uma biblioteca ClojureScript que constrói apps Three.js no mesmo estilo do Reagent. É o único wrapper com tração real. 
GitHub

Como funciona: em vez de chamar THREE.Mesh., scene.add, etc., você descreve a cena com hiccup, e a lib faz o diff/patch no scene graph igual o React faz no DOM:

clojure
(ns globe.core
  (:require [threeagent.core :as th]))

(defonce state (th/atom {:rot-y 0.0 :ticks 0}))

;; Componente form-1 — puro
(defn eco-marker [color position]
  [:sphere {:radius 3
            :position position
            :material {:color color
                       :emissive color
                       :emissiveIntensity 0.4}}])

;; Componente form-2 — com estado local
(defn pulsing-marker [dialect]
  (let [t (atom 0)]
    (.setInterval js/window #(swap! t + 0.05) 16)
    (fn []
      [:sphere {:radius (+ 2 (* 0.5 (js/Math.sin @t)))
                :position (:position dialect)
                :material {:color (:color dialect)}}])))

;; Cena completa como hiccup
(defn root []
  (let [{:keys [rot-y]} @state]
    [:object {:rotation [0.15 rot-y 0]}
     ;; Globo
     [:sphere {:radius 100
               :segments 72
               :material {:color 0x04101e :shininess 8}}]
     ;; Luzes
     [:ambient-light {:intensity 1.2 :color 0x0d1f44}]
     [:directional-light {:intensity 1.4 :color 0x5588cc
                          :position [1.5 0.4 1]}]
     ;; Marcadores dos ecossistemas
     (for [eco ecossistemas]
       ^{:key (:id eco)}
       [eco-marker (:cor eco) (ll->three (:lat eco) (:lng eco) 102)])]))

;; Renderiza e inicia
(defonce scene (th/render root (.-body js/document)))

;; Loop de rotação
(.setInterval js/window
  #(swap! state update :rot-y + 0.002) 16)
O setup no shadow-cljs é direto:

clojure
;; deps.edn
{:deps {io.github.DougHamil/threeagent {:git/sha "..."}}}

;; package.json — Three.js via npm como peer dep
{"dependencies": {"three": "^0.175.0"}}
O problema real: a lib cobre os primitivos básicos (sphere, box, cylinder, ambient-light, directional-light, point-light, object) mas não expõe ShaderMaterial, Sprite, BufferGeometry custom nem AdditiveBlending. Para o nosso globo especificamente, a atmosfera GLSL e os sprites de glow precisariam de interop direto mesmo.

Na prática, a estratégia que funciona melhor é híbrida: threeagent para a estrutura declarativa da cena + interop THREE/* cirúrgico para as partes avançadas:

clojure
(defn atmosphere []
  ;; Aqui saímos do threeagent e criamos o objeto Three.js diretamente
  (let [mesh (THREE/Mesh.
               (THREE/SphereGeometry. 106.5 48 48)
               (THREE/ShaderMaterial. #js{:vertexShader   atmo-vert
                                         :fragmentShader atmo-frag
                                         :side           THREE/BackSide
                                         :blending       THREE/AdditiveBlending
                                         :transparent    true}))]
    ;; Retorna o objeto Three.js bruto — threeagent aceita via :object
    [:object {:three-obj mesh}]))
Veredicto honesto: o threeagent é elegante para cenas estruturadas (jogos, visualizações com geometrias padrão), mas para data viz pesada com shaders, partículas e blending customizado, interop direto + atom + loop de animação próprio dá mais controle sem a camada extra. A ausência de um equivalente ao @react-three/fiber do ecossistema JS é sentida aqui — a comunidade Clojure nunca teve um investimento proporcional nessa direção.

Como eu poderia contribuir com a biblioteca afim de atingir meu objetivo?

Jun 7

Mapeou lacunas técnicas e estratégia de contribuição para biblioteca
Mapeou lacunas técnicas e estratégia de contribuição para biblioteca
Boa estratégia. Antes de abrir qualquer PR, é preciso entender como o threeagent registra tipos de entidade — é o ponto de extensão correto.

Como o threeagent funciona internamente
A arquitetura é um reconciler de scene graph baseado em multimethods. Cada keyword como :sphere, :box, :ambient-light é um dispatch value que implementa três operações:

clojure
;; Padrão interno do threeagent
(defmulti  create-object  (fn [entity-type params] entity-type))
(defmulti  update-object! (fn [entity-type obj old-params new-params] entity-type))
(defmulti  destroy-object (fn [entity-type obj] entity-type))
Isso significa que adicionar um novo tipo de primitivo Three.js não requer tocar no core — é puro defmethod em namespace externo.

Gap analysis para o globo
Mapeando o que o nosso globo usa contra o que threeagent expõe:

Primitivo Three.js	threeagent tem?	Impacto no globo
SphereGeometry + MeshPhongMaterial	✅ :sphere	Globo base — OK
AmbientLight, DirectionalLight	✅	Iluminação — OK
ShaderMaterial	❌	Atmosfera GLSL — bloqueante
Sprite + SpriteMaterial	❌	Marcadores glow — bloqueante
Points + PointsMaterial	❌	Campo de estrelas — bloqueante
AdditiveBlending em materials	❌	Todos os glow effects
CanvasTexture como :map	❌	Textura de grade do globo
Objeto Three.js arbitrário (escape hatch)	parcial via defcomponent	Fallback
O que contribuir — e como
1. :points — estrelas (mais simples, bom primeiro PR)
clojure
;; Em um novo namespace: threeagent.contrib.particles
(ns threeagent.contrib.particles
  (:require ["three" :as THREE]
            [threeagent.impl.component :refer [defmethod-entity]]))

;; create — chamado uma vez quando o nó entra na cena
(defmethod-entity :points :create
  [_ {:keys [count spread size opacity color]
      :or   {count 10000 spread 3200 size 0.6 opacity 0.7 color 0xffffff}}]
  (let [geo (THREE/BufferGeometry.)
        pos (js/Float32Array. (* count 3))]
    (dotimes [i (* count 3)]
      (aset pos i (* (- (js/Math.random) 0.5) spread)))
    (.setAttribute geo "position" (THREE/BufferAttribute. pos 3))
    (THREE/Points. geo
      (THREE/PointsMaterial.
       #js{:color       color
           :size        size
           :transparent true
           :opacity     opacity}))))

;; update — chamado quando props mudam (diffs do reconciler)
(defmethod-entity :points :update
  [_ obj old-params {:keys [size opacity] :as new-params}]
  (when (not= (:size old-params) size)
    (set! (.. obj -material -size) size))
  (when (not= (:opacity old-params) opacity)
    (set! (.. obj -material -opacity) opacity)))
Uso depois de registrar:

clojure
[:points {:count 12000 :spread 3200 :size 0.55 :opacity 0.7}]
2. :sprite — marcadores de ecossistema (médio)
clojure
(defmethod-entity :sprite :create
  [_ {:keys [texture-fn color size blending]
      :or   {size 20 blending :additive}}]
  (let [tex (texture-fn)   ; função que retorna CanvasTexture
        mat (THREE/SpriteMaterial.
             #js{:map         tex
                 :blending    (case blending
                                :additive THREE/AdditiveBlending
                                :normal   THREE/NormalBlending
                                THREE/AdditiveBlending)
                 :transparent true
                 :depthWrite  false})
        sp  (THREE/Sprite. mat)]
    (.set (.-scale sp) size size 1)
    sp))

(defmethod-entity :sprite :update
  [_ obj old {:keys [size]}]
  (when (not= (:size old) size)
    (.set (.-scale obj) size size 1)))
Uso:

clojure
(defn eco-sprite [eco]
  [:sprite {:texture-fn #(create-glow-tex (:cor eco))
            :size       (+ 18 (* (count-dialetos (:id eco)) 5.5))
            :blending   :additive
            :position   (ll->vec3 (:lat eco) (:lng eco) 100.3)}])
3. :shader-mesh — atmosfera GLSL (mais complexo, maior valor)
clojure
(defmethod-entity :shader-mesh :create
  [_ {:keys [geometry vertex-shader fragment-shader uniforms
             side blending transparent depth-write]
      :or   {side :front blending :normal transparent false depth-write true}}]
  (let [geo (case (:type geometry)
              :sphere (THREE/SphereGeometry.
                       (:radius geometry 100)
                       (:segments geometry 48)
                       (:segments geometry 48))
              geometry)   ; aceita objeto Three.js direto
        mat (THREE/ShaderMaterial.
             #js{:vertexShader   vertex-shader
                 :fragmentShader fragment-shader
                 :uniforms       (clj->js (or uniforms {}))
                 :side           (case side
                                   :back  THREE/BackSide
                                   :both  THREE/DoubleSide
                                   THREE/FrontSide)
                 :blending       (case blending
                                   :additive THREE/AdditiveBlending
                                   THREE/NormalBlending)
                 :transparent    transparent
                 :depthWrite     depth-write})]
    (THREE/Mesh. geo mat)))

;; Update de uniforms é importante para animações
(defmethod-entity :shader-mesh :update
  [_ obj _ {:keys [uniforms]}]
  (when uniforms
    (doseq [[k v] uniforms]
      (when-let [u (aget (.. obj -material -uniforms) (name k))]
        (set! (.-value u) v)))))
Uso — atmosfera do globo:

clojure
[:shader-mesh
 {:geometry       {:type :sphere :radius 106.5 :segments 48}
  :vertex-shader  atmo-vert-glsl
  :fragment-shader atmo-frag-glsl
  :side           :back
  :blending       :additive
  :transparent    true
  :depth-write    false}]
Estrutura do PR
A contribuição mais cirúrgica e aceitável seria em três PRs separados:

PR 1 — :points + :PointsMaterial com propriedades básicas (pequeno, fácil review)

PR 2 — :sprite + :SpriteMaterial + suporte a :blending em materials existentes

PR 3 — :shader-mesh + sistema de uniforms reativos

Para cada PR, o padrão do projeto pede:

Namespace dedicado (ex: threeagent.components.particles)
Teste no threeagent-examples com demo visual
Docstring nos defmethod
Se o maintainer não estiver ativo
Dado que o threeagent tem commits esparsos, uma alternativa pragmática é criar um thin contrib namespace local no seu próprio projeto, usando o mesmo padrão de multimethods. Você não precisa que o PR seja mergeado para usar — o sistema de dispatch é aberto:

clojure
;; No seu projeto, em um ns próprio
(ns meu-projeto.threejs-ext
  (:require [threeagent.impl.component] ; ativa o multimethod
            ["three" :as THREE]))

;; Registra `:sprite` no sistema do threeagent sem tocar na lib
(defmethod threeagent.impl.component/create-entity :sprite
  [_ params] ...)
Isso é possível porque Clojure/ClojureScript permite extender multimethods de fora — o mesmo motivo pelo qual o protocolo funciona para Pathom 3 resolvers. Você contribui upstream quando tiver confiança na API; enquanto isso, itera localmente sem esperar review.

Vale a pena criar algo open source do zero?

Jun 7

Ponderou trade-offs entre criar novo projeto versus aproveitar alternativas existentes
Ponderou trade-offs entre criar novo projeto versus aproveitar alternativas existentes
Depende muito do que você quer com isso — e as respostas honestas puxam em direções diferentes.

Se o objetivo é usar o globo em produção agora, não vale. Você vai gastar semanas construindo um reconciler de scene graph, testando edge cases de diff/patch, mantendo compatibilidade com versões do Three.js, escrevendo docs e exemplos — tudo isso antes de escrever uma linha da visualização em si. O interop direto resolve em dias.

Se o objetivo é portfólio e sinal de seniência no ecossistema Clojure, aí o cálculo muda — mas tem um risco real. Um projeto open source abandonado faz mais mal do que bem. O GitHub está cheio de "cljs-threejs-wrapper" com dois commits e README incompleto. O sinal negativo de um projeto sem tração é pior do que não ter o projeto. Para ser um ativo de carreira real, precisaria de: exemplos funcionais, documentação decente, pelo menos uma pessoa além de você usando, e commits regulares por 6+ meses.

Se o objetivo é preencher um gap genuíno do ecossistema, o gap existe mas o mercado é pequeno. A interseção de "usa ClojureScript" + "precisa de Three.js" + "quer uma abstração declarativa" é uma comunidade de dezenas de pessoas no mundo. O @react-three/fiber tem 27k stars porque o ecossistema React/JS tem dezenas de milhões de devs. Um equivalente CLJS nunca vai ter essa tração proporcional — e isso afeta adoção, contribuições externas e motivação de longo prazo.

O padrão que funciona no ecossistema Clojure é diferente do JS: as bibliotecas boas surgem de extração, não de greenfield. O Pathom nasceu de um problema real que o Wilker tinha no sistema de resolução de dados da Nubank. O Malli surgiu de validação que já existia em prod. O babashka veio de scripts que o Michiel já usava. Você constrói a coisa para resolver seu problema, extrai o que ficou genérico, e aí publica.

Para o seu contexto específico — TechSentry rodando, contrato ativo, processo na Brasil Paralelo — o custo de oportunidade é alto demais para um projeto novo de infra de visualização. O que faria mais sentido seria: usa interop direto para o globo, anota os padrões que se repetem, e se depois de dois ou três projetos os mesmos padrões aparecerem, aí você extrai com confiança que a API está certa.

A única exceção seria se você quiser usar isso explicitamente como peça de portfólio para a vaga na Brasil Paralelo ou similares — aí um projeto pequeno, bem feito, com foco estreito (só :sprite + :shader-mesh + :points, por exemplo) e um exemplo visual impressionante pode ter retorno. Mas nesse caso o foco seria qualidade e finalização, não ambição de escopo.

Tenho um amigo criando coisas com clojure dart afim de gerar clientes para consultoria em metodologia de criação de apps. Poderia pensar em como uma lib clojure threejs poderia agregar

Jun 7

Conectou Three.js com ClojureDart para diferenciar consultoria
Conectou Three.js com ClojureDart para diferenciar consultoria
O achado mais relevante aqui é que já existe um port do Three.js para Dart — three_js, um pacote Flutter convertido de threejs e three_dart que permite visualizar, editar e controlar modelos 3D, com suporte completo aos recursos WebGL2 core. E tem um ecossistema modular ao redor: física (Cannon, Omio), loaders avançados (GLTF, FBX), controles de transformação, e até um SDK de XR para VR/AR. 
Pub.dev
Pub.dev

O pacote teve versão publicada em janeiro de 2026, então não é projeto abandonado. 
Pub.dev

Isso muda completamente o raciocínio.

O insight central: a discussão sobre "lib clojure threejs" não é só para web. Com ClojureDart + three_js, você tem o mesmo paradigma no Flutter — mobile, desktop e web. A biblioteca que você e seu amigo construiriam seria a camada Clojure sobre Three.js em ambas as plataformas:

Web    →  ClojureScript + Three.js (npm)
Mobile →  ClojureDart   + three_js (pub.dev)
Mesma API declarativa, mesmo modelo mental, dois alvos.

Como isso agrega para a consultoria do seu amigo:

O posicionamento muda de "consultoria de desenvolvimento Flutter com ClojureDart" para algo mais específico e defensável: "aplicações Flutter com capacidades 3D/imersivas usando metodologia Clojure". Isso é raro — a maioria das consultorias Flutter não toca em 3D.

Os casos de uso que geram cliente de consultoria são exatamente onde 3D se diferencia:

Configuradores de produto — móveis, automóveis, imóveis na planta. O cliente vê o produto em 3D antes de comprar. Mercado enorme no Brasil e com ticket alto.
Dashboards imersivos — IoT industrial, supply chain. Em vez de tabela, o cliente vê o galpão em 3D com os dados sobrepostos.
Onboarding de apps — telas de introdução com animações 3D retêm usuário de forma mensurável. É o tipo de coisa que vira case study.
Visualização de dados financeiros — o globo que construímos é exatamente esse padrão. Para um banco ou fintech, mostrar distribuição de portfólio em 3D é diferente de mostrar uma tabela.
Esses são projetos de ticket alto porque exigem especialização que poucos times têm — menos ainda combinado com ClojureDart.

O que construir como isca de lead:

O projeto open source não seria uma lib genérica de Three.js. Seria algo mais estreito e visualmente impressionante — um globo de dados interativo em Flutter, publicado com o código aberto em ClojureDart. Exatamente o que fizemos aqui, mas rodando nativo no celular.

O ciclo é:

Constroem o globo como demo de portfólio
Publicam no GitHub com README mostrando o vídeo rodando em Flutter
Escrevem um post técnico explicando como ClojureDart + three_js funciona
Esse post aparece nas buscas de devs Flutter que querem 3D
Esses devs ou contratam a consultoria ou indicam para alguém que precisa
A diferença de fazer isso com ClojureDart vs fazer com Flutter puro é que o público que chega via conteúdo técnico de Clojure tem perfil de empresa com engenharia própria — é o cliente que paga bem e respeita metodologia.

O que você entra nisso:

Você já fez a parte mais difícil — estruturou o pensamento sobre a lib, entende o padrão de reconciler, conhece Three.js por dentro agora. A camada ClojureDart seria uma tradução da mesma API, com interop Dart em vez de interop JS. Poderiam dividir o trabalho: você cuida da camada web (CLJS), ele cuida da camada Flutter (ClojureDart), e os dois publicam como projeto conjunto.

O retorno para você seria ter um projeto open source visível no ecossistema Clojure com case prático — exatamente o tipo de coisa que aparece bem em processos como o da Brasil Paralelo.

Acho que o ponto aqui é mostrar a riqueza de diversidade de dialetos clojure e compilação diversa nativamente multi plataforma

Jun 7

Reconheceu a proposta central: demonstrar diversidade Clojure multiplataforma
Reconheceu a proposta central: demonstrar diversidade Clojure multiplataforma
Exato — e isso muda tudo sobre o que vale construir.

O ponto mais poderoso não é a lib. É o meta-demo: uma visualização sobre a diversidade de plataformas do Clojure, que é ela mesma construída usando essa diversidade. O globo que os dialetos habitam é construído com esses dialetos.

O globo roda em web?     → ClojureScript + Three.js
O mesmo globo no Flutter? → ClojureDart  + three_js
Os dados vêm de onde?    → Babashka scripts (GitHub API)
A API serve o quê?       → Clojure JVM
Nenhuma linha de JavaScript, nenhuma linha de Dart, nenhuma linha de Python. Só Clojure — quatro compilações, quatro plataformas, um paradigma. Isso é a tese demonstrada, não explicada.

Por que isso funciona como marketing:

O demo tem uma qualidade rara — ele é recursivo. A visualização sobre Clojure compilar para todo lugar é Clojure compilando para todo lugar. Esse tipo de coisa para o scroll, gera tweet, vira talk em Clojure/conj. É o tipo de história que jornalistas de tech conseguem contar: "devs construíram uma visualização 3D que demonstra a si mesma."

A consultoria do seu amigo vira o time que criou isso. Não tem currículo mais direto que "construímos uma coisa que ninguém nunca havia feito com ClojureDart."

O que cobrir por plataforma:

Dialeto	Plataforma alvo	Papel no demo
Clojure JVM	Servidor	API de dados, atualiza ★ via GitHub
Babashka	CLI / CI	Scraping, geração do EDN de dados
ClojureScript	Web browser	Globe 3D interativo (Three.js)
ClojureDart	iOS, Android, Desktop	Globe nativo (three_js pub.dev)
E o detalhe que amarra a narrativa: ClojureDart aparece no globo como ponto #13 — então quem usa o app mobile está usando exatamente a tecnologia que o ponto ClojureDart no globo representa.

O que construir, em ordem:

Primeiro o web (já temos o HTML), segundo o pipeline de dados em Babashka que mantém os dados frescos via GitHub API, terceiro o ClojureDart com three_js. A lib emerge como subproduto natural — não é o objetivo, é o que você extrai depois que os três funcionam.

O projeto open source se chama algo como clojure-dialects-globe, tem três entrypoints (web/, flutter/, scripts/), e o README começa com: "Built with four Clojure dialects. One for each platform it runs on."

Isso é o portfólio e a metodologia ao mesmo tempo.

Então afinal estruture o projeto como um documento detalhando a implementação

Jun 7

Arquitetou projeto multi-dialeto Clojure com componentes integrados
Arquitetou projeto multi-dialeto Clojure com componentes integrados
O documento cobre o projeto completo em cinco camadas:

Estrutura do que foi especificado:

Dados e pipeline — base.edn como fonte de verdade versionada no git, Babashka enriquecendo com stars do GitHub via API, GitHub Action de cron diário gerando dialects.edn automaticamente.

API JVM — Reitit + Ring servindo Transit-JSON com cache por mtime do arquivo, zero cold start na leitura, deploy via systemd no nexus.

Web (ClojureScript) — shadow-cljs + Three.js via npm, todos os helpers geométricos (ll->xyz, grid-texture, glow-texture, shaders GLSL de atmosfera), watch reativo no atom de estado para adicionar marcadores quando os dados chegam da API.

Flutter (ClojureDart) — mesmo algoritmo geométrico, mesmo shader de atmosfera, three_js via pub.dev, fetch HTTP da mesma API JVM, widget nativo.

Infraestrutura — comandos de dev local por terminal, nginx como proxy reverso, systemd unit file para a API, builds de cada plataforma Flutter.

O projeto tem uma propriedade que poucas demos têm: a mensagem final — "No JavaScript. No Dart. No Python. Just Clojure." — é verificável olhando o código-fonte. Isso é o que transforma um portfólio em argumento.


Clojure dialects globe
Document · MD 
Destes tópicos, o que teremos pra apresentar?

Jun 7

Inventariou artefatos prontos versus planejados do projeto
Inventariou artefatos prontos versus planejados do projeto
Honestamente, só duas coisas existem de fato:

Pronto agora:

O globo rodando — o HTML completo com Three.js, todos os 39 dialetos, interação, shaders de atmosfera, painel de detalhes. Abre no browser e funciona.
O documento de especificação — arquitetura, modelo de dados EDN, código de cada dialeto, ordem de implementação, infraestrutura.
Existe apenas no papel:

O projeto ClojureScript (nenhuma linha de .cljs foi escrita)
A API Clojure JVM (nenhum namespace, nenhum deps.edn real)
Os scripts Babashka (nenhum .bb executável)
O projeto ClojureDart (nenhum .cljd, nenhum pubspec funcional)
O base.edn com os dados dos dialetos
O repositório GitHub
O globo HTML que temos é uma prova de conceito visual — demonstra o que a versão ClojureScript vai parecer, mas foi escrito em JavaScript puro. Serve bem para mostrar a ideia, não serve para mostrar a tese do projeto.

A próxima etapa real é uma só: criar o data/base.edn com os 39 dialetos e rodar o script Babashka. Com isso você tem a fundação que alimenta tudo — API, web e Flutter dependem desse dado existir.

Quer começar por aí?







Claude is AI and can make mistakes. Please double-check responses.
