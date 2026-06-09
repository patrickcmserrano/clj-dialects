# Clojure Dialects Globe — Devlog de Sessão

> **Data:** 2026-06-08  
> **Status:** Em desenvolvimento ativo  
> **URL local:** http://localhost:8080

---

## O Projeto

Visualização interativa de **39 dialetos da linguagem Clojure** em um globo 3D. A tese é recursiva: uma visualização *sobre* Clojure compilar para múltiplas plataformas, *construída* com Clojure em múltiplas plataformas.

Cada marcador no globo representa um dialeto — posicionado geograficamente no país de origem do seu criador. O tamanho do marcador é proporcional ao número de GitHub Stars.

---

## Arquitetura — 4 Camadas Clojure

```
Babashka (scripts)          ← coleta stars do GitHub
    ↓  data/dialects.edn
Clojure JVM (API)           ← serve via Transit-JSON na porta 3000
    ↓  HTTP fetch
ClojureScript / Three.js    ← globo 3D interativo (porta 8080)
    ↓  (paridade)
ClojureDart / Flutter       ← app mobile (implementado, não testado)
```

### Stack por camada

| Camada | Tecnologia |
|--------|-----------|
| Scripts | Babashka 1.12.218 |
| API | Clojure 1.12, Reitit 0.7.2, Ring/Jetty 1.12.2, Transit-CLJ 1.0.333 |
| Web | ClojureScript, shadow-cljs 2.28, Reagent 2.0.0, React 18, Three.js r175 |
| Mobile | ClojureDart, Flutter |

---

## Dados — 39 Dialetos, 14 Ecossistemas

### Ecossistemas (coordenadas geográficas reais)

| Ecossistema | Localização | Motivo |
|-------------|-------------|--------|
| JVM | Chicago IL, EUA (41.8°N, 87.6°W) | Rich Hickey / Cognitect |
| JavaScript | Amsterdam, Holanda (52.4°N, 4.9°E) | Michiel Borkent (5 de 8 dialetos JS) |
| C / C++ | Murray Hill NJ, EUA (40.7°N, 74.3°W) | Bell Labs — origem do C e C++ |
| Go | Mountain View CA, EUA (37.4°N, 122.1°W) | Google HQ |
| Python | Utrecht, Holanda (52.1°N, 5.3°E) | Guido van Rossum |
| Rust | Vancouver BC, Canadá (49.3°N, 123.1°W) | Graydon Hoare |
| BEAM | Estocolmo, Suécia (59.3°N, 18.1°E) | Ericsson — criadora do Erlang |
| .NET | Redmond WA, EUA (47.6°N, 122.2°W) | Microsoft HQ |
| Dart | Paris, França (48.8°N, 2.3°E) | Christophe Grand & Baptiste Dupuch |
| Lua | Rio de Janeiro, Brasil (22.9°S, 43.2°W) | PUC-Rio ✓ |
| PHP | British Columbia, Canadá (49.1°N, 122.9°W) | Rasmus Lerdorf |
| Wasm | San Francisco CA, EUA (37.8°N, 122.4°W) | Mozilla / W3C |
| Lisp | Cambridge MA, EUA (42.4°N, 71.1°W) | MIT — John McCarthy |
| Outros | Amsterdam, Holanda (52.3°N, 4.8°E) | obb / Borkent |

### Dialetos completos

| Bandeira | Nome | Ecossistema | Stars | País do Criador |
|----------|------|-------------|-------|----------------|
| 🇺🇸 | Clojure | JVM | 10.927 | Estados Unidos |
| 🇺🇸 | ClojureScript | JavaScript | 9.385 | Estados Unidos |
| 🇸🇪 | Carp | C/C++ | 5.936 | Suécia |
| 🇺🇸 | Hy | Python | 5.418 | Estados Unidos |
| 🇳🇱 | Babashka | JVM | 4.530 | Países Baixos |
| 🇺🇸 | Janet | C/C++ | 4.297 | Estados Unidos |
| 🇺🇸 | Jank | C/C++ | 3.265 | Estados Unidos |
| 🇺🇸 | Fennel | Lua | 2.400 | Estados Unidos |
| 🇷🇺 | Joker | Go | 1.750 | Rússia |
| 🇵🇹 | Lux | JVM | 1.739 | Portugal |
| 🇦🇷 | Clojerl | BEAM | 1.713 | Argentina |
| 🇺🇸 | ClojureCLR | .NET | 1.643 | Estados Unidos |
| 🇫🇷 | ClojureDart | Dart | 1.621 | França |
| 🇹🇷 | Ferret | C/C++ | 1.121 | Turquia |
| 🇺🇸 | Planck | JavaScript | 1.040 | Estados Unidos |
| — | ClojureRS | Rust | 980 | — |
| 🇳🇱 | nbb | JavaScript | 955 | Países Baixos |
| 🇳🇱 | Squint | JavaScript | 865 | Países Baixos |
| 🇳🇱 | Cherry | JavaScript | 641 | Países Baixos |
| 🇺🇸 | YAMLScript | JVM | 622 | Estados Unidos |
| 🇸🇪 | Joyride | JavaScript | 583 | Suécia |
| — | Glojure | Go | 535 | — |
| 🇩🇪 | Phel | PHP | 512 | Alemanha |
| 🇵🇱 | let-go | Go | 487 | Polônia |
| 🇺🇸 | Basilisp | Python | 465 | Estados Unidos |
| 🇳🇱 | Scittle | JavaScript | 432 | Países Baixos |
| 🇺🇸 | Cloture | Lisp | 399 | Estados Unidos |
| 🇳🇱 | obb | Outros | 247 | Países Baixos |
| — | Lokke | Lisp | 220 | — |
| — | JO Clojure | C/C++ | 153 | — |
| — | Cream | JVM | 144 | — |
| 🇺🇸 | clojure-clr-next | .NET | 142 | Estados Unidos |
| 🇨🇳 | Calcit | JavaScript | 138 | China |
| — | ClojureWasm | Wasm | 105 | — |
| 🇺🇸 | Lingy | Outros | 44 | Estados Unidos |
| — | Gloat | Go | 38 | — |
| — | go-joker | Go | 19 | — |
| — | cljrs | Rust | 16 | — |
| 🇦🇺 | ClojureFnl | Lua | 4 | Austrália |

> **Nota:** Michiel Borkent (🇳🇱) criou sozinho 6 projetos: Babashka, nbb, Squint, Cherry, Scittle, obb.

---

## Funcionalidades Implementadas

### Globo 3D
- Textura foto-realista da Terra (NASA land/ocean/ice/cloud, 2048px)
- Overlay de grade lat/lng semi-transparente (10° e equador destacado)
- Atmosfera com shader GLSL (rim-glow, blending aditivo)
- 12.000 estrelas de fundo (BufferGeometry)
- Auto-rotação (0.0018 rad/frame) com pausa ao interagir
- Drag para rotacionar (mouse)
- Iluminação direcional + ambiente

### Marcadores
- **Eco-sprites**: glow colorido por ecossistema, escala `8 + 2√n` (proporcional ao nº de dialetos)
- **Dialeto-sprites**: glow branco com cor do ecossistema, escala ∝ `√stars`
- Distribuição em anel ao redor do centro do ecossistema: raio fixo `2.5 + 0.9√total`
- Pulsação: ecossistema a 1.6 Hz, dialeto selecionado a 5 Hz (45% maior)

### Interação
- **Hover**: tooltip com bandeira + nome + stars
- **Click no globo**: abre painel lateral com detalhes completos
- **Click na lista (painel esquerdo)**: globe anima suavemente para o país do criador do dialeto
  - Cada dialeto tem coordenadas individuais por país (não por ecossistema)
  - Fallback para centro do ecossistema se `:pais` for nil

### Painel Lateral (dialeto selecionado)
- Rank, nome, ecossistema (badge colorido)
- Badge de origem: bandeira + país + criador
- Descrição
- Barra de stars relativa ao Clojure (máximo: 11.000)
- Host, release date, link GitHub

### Lista de Dialetos (painel esquerdo)
- Título: "**N dialetos detectados**" + contagem de ecossistemas
- Agrupado por ecossistema (cabeçalho clicável → foca no ecossistema)
- Por dialeto: bandeira + nome + stars abreviadas (ex: `1.1k`)
- Ordenado por stars (maior primeiro) dentro de cada ecossistema
- Hover highlight
- Click: foca o globo no país do criador + abre painel de detalhes

---

## Problemas Encontrados e Soluções

### 1. Globo azul sem textura
**Causa:** Three.js r155+ requer `texture.colorSpace = THREE.SRGBColorSpace` para canvas textures.  
**Fix:** `(aset tex "colorSpace" (.-SRGBColorSpace THREE))`

### 2. ReactDOM.render depreciado (React 18)
**Causa:** Reagent usava API antiga de React 17.  
**Fix:** Importar `"react-dom/client"` e usar `(.createRoot rdom-client el)` + `.render`.

### 3. Erro `(.. hits 0 -object)` — dot form inválido
**Causa:** O macro `..` do ClojureScript não suporta índice numérico.  
**Fix:** `(aget hits 0)` + acesso de propriedade separado.

### 4. Stars zeradas (GitHub API sem token)
**Causa:** Sem `GITHUB_TOKEN`, todas as chamadas à API retornavam nil.  
**Fix:** Seed de stars em `base.edn` + fallback no script: `cond-> dialeto live (assoc :stars live)`.

### 5. API não encontrava `dialects.edn`
**Causa:** Path relativo resolvia da raiz do projeto, não de `api/`.  
**Fix:** `(def data-path (or (System/getenv "DATA_PATH") "../data/dialects.edn"))`

### 6. Bracket mismatch em `ui.cljs`
**Causa:** Erro manual de contagem — 8 `]` onde havia 7 necessários.  
**Fix:** Corrigido manualmente.

### 7. Canvas `width`/`height` não aplicavam
**Causa:** `set!` em ClojureScript para propriedades DOM às vezes não funciona em shadow-cljs.  
**Fix:** Trocado para `aset`.

### 8. Eco-sprites enormes (JVM ocupava 45% do globo)
**Causa:** Fórmula `18 + n × 5.5` — com 5 dialetos = 45.5 vs raio do globo = 100.  
**Fix:** `8 + 2√n` — JVM: 12.5, JS: 17.2, máximo ~20.

### 9. Dialetos em espiral em vez de anel
**Causa:** `ring = 4 + index × 0.85` — crescia com o índice.  
**Fix:** `ring = 2.5 + 0.9√total` — raio fixo para todo o grupo.

### 10. Coordenadas geográficas erradas
**Causa:** Planejamento inicial usou posições temáticas arbitrárias (BEAM no Oriente Médio, Dart no Vietnã, etc.).  
**Fix:** Corrigidas para origens reais de cada tecnologia.

### 11. Click na lista focava no ecossistema em vez do país do criador
**Causa:** Handler usava `(:lat eco)` para todos os dialetos.  
**Fix:** Mapa `pais->coords` no `ui.cljs` com coordenadas por país; fallback para ecossistema quando `:pais` é nil.

---

## Como Rodar

```bash
# 1. Gerar dados (usa stars do base.edn como seed; com token busca GitHub live)
cd /home/pace/dev/clj-dialects
bb scripts/fetch_stars.bb                      # sem token usa seed
GITHUB_TOKEN=ghp_... bb scripts/fetch_stars.bb # com token busca live

# 2. API (porta 3000)
cd api && clojure -M:run

# 3. Web (porta 8080, hot-reload)
cd web && npx shadow-cljs watch globe

# Abrir: http://localhost:8080
```

---

## Arquivos Principais

```
clj-dialects/
├── data/
│   ├── base.edn          ← fonte de verdade: 39 dialetos + 14 ecossistemas
│   └── dialects.edn      ← gerado pelo Babashka (base + stars atualizadas)
├── scripts/
│   └── fetch_stars.bb    ← enriquece base.edn com stars do GitHub
├── .github/workflows/
│   └── update-data.yml   ← cron diário 6h UTC, auto-commit
├── api/
│   ├── deps.edn
│   └── src/globe/api/
│       ├── server.clj    ← Jetty porta 3000, wrap-cors
│       ├── routes.clj    ← /api/dialects (Transit-JSON), /api/health
│       └── data.clj      ← cache com invalidação por mtime
└── web/
    ├── shadow-cljs.edn
    ├── public/
    │   ├── index.html
    │   └── textures/
    │       └── earth.jpg ← textura NASA 2048px (580KB)
    └── src/globe/web/
        ├── core.cljs     ← build-scene!, animation-loop!, raycaster, foco
        ├── globe.cljs    ← ll->xyz, earth-texture, glow, sprites, atmosfera
        ├── state.cljs    ← átomo Reagent: dialetos, selecionado, foco
        ├── data.cljs     ← fetch Transit-JSON da API
        └── ui.cljs       ← lista, painel lateral, tooltip, pais->coords
```

---

## Próximos Passos (backlog)

- [ ] Filtro por ecossistema na lista
- [ ] Campo de busca por nome de dialeto
- [ ] Animação de câmera (zoom in ao selecionar)
- [ ] Deploy estático (Cloudflare Pages / GitHub Pages)
- [ ] Preencher `:pais` / `:bandeira` dos 9 dialetos com `nil`
- [ ] Testar a camada Flutter/ClojureDart
- [ ] Mobile: touch events para drag no globo
