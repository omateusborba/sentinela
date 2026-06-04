# Sentinela

Solução híbrida (app Android + painel web + backend de borda) para alertas de proximidade de queimadas com base em focos de calor da [NASA FIRMS](https://firms.modaps.eosdis.nasa.gov/).

## Estrutura do monorepo

```
sentinela/
├── apps/
│   ├── backend/      # Cloudflare Worker + Hono.js (Fase 1)
│   ├── web/          # React + Vite (Fase 2)
│   └── mobile/       # Android Kotlin + Compose (Fase 3)
├── packages/
│   └── shared/       # Tipos TypeScript compartilhados
└── README.md
```

## Pré-requisitos

- [Node.js](https://nodejs.org/) 20+
- Conta Cloudflare (para deploy)
- Chave NASA FIRMS (grátis): https://firms.modaps.eosdis.nasa.gov/api/map_key/

## Instalação

```bash
npm install
```

## Fase 1 — Backend (local)

### 1. Chave FIRMS

Copie o exemplo e preencha sua chave (nunca commite `.dev.vars`):

```bash
cp apps/backend/.dev.vars.example apps/backend/.dev.vars
# Edite apps/backend/.dev.vars e defina FIRMS_MAP_KEY=sua_chave
```

### 2. KV namespace (opcional para dev local)

O Wrangler cria um namespace de preview automaticamente em `wrangler dev`. Para produção:

```bash
cd apps/backend
npx wrangler kv namespace create FIRES_CACHE
```

Atualize `id` e `preview_id` em `apps/backend/wrangler.toml` com os IDs retornados.

### 3. Subir o Worker

```bash
npm run dev:backend
# ou: cd apps/backend && npm run dev
```

Servidor local padrão: **http://localhost:8787** (escuta em `0.0.0.0` para o emulador Android via `10.0.2.2`)

### 4. Testar

```bash
# Health
curl http://localhost:8787/health

# Focos no Brasil (1 dia)
curl "http://localhost:8787/api/fires?bbox=-74,-34,-34,6&days=1"

# Risco regional (3 dias)
curl "http://localhost:8787/api/risk?bbox=-74,-34,-34,6&days=3"
```

A segunda chamada idêntica a `/api/fires` deve retornar o header `X-Cache: HIT`. A `MAP_KEY` nunca aparece nas respostas nem no repositório.

### Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/health` | `{ "status": "ok" }` |
| GET | `/api/fires?bbox=&days=&source=` | Lista de focos normalizados (`FireHotspot[]`) |
| GET | `/api/risk?bbox=&days=` | Índice de risco (`RegionRisk`) |

**Parâmetros**

- `bbox` (obrigatório): `oeste,sul,leste,norte` — ex. Brasil: `-74,-34,-34,6`
- `days`: 1–10 (padrão `1` em fires, `3` em risk)
- `source`: `VIIRS_NOAA20_NRT` (padrão), `VIIRS_SNPP_NRT`, `MODIS_NRT`

### Deploy (Fase 4 — resumo)

```bash
cd apps/backend
npx wrangler secret put FIRMS_MAP_KEY
npx wrangler deploy
```

Depois de publicar o painel web, adicione a URL de produção em `CORS_ORIGINS` em `apps/backend/src/index.ts`.

## Fase 2 — Painel web (local)

Requer o backend rodando (`npm run dev:backend`).

```bash
cp apps/web/.env.example apps/web/.env
npm run dev:web
```

Abra **http://localhost:5173**. O painel consome `VITE_API_URL` (padrão `http://localhost:8787`).

### Funcionalidades

- Mapa Leaflet com focos coloridos por confiança/FRP
- Filtro de período: 24h, 3 dias, 7 dias
- Cards de risco por macro-região (Norte, Nordeste, Centro-Oeste, Sudeste, Sul)
- Tabela dos focos mais recentes

### Build estático

```bash
npm run build:web
# saída em apps/web/dist
```

## Typecheck

```bash
npm run typecheck
```

## Fase 3 — App Android

Documentação completa: [apps/mobile/README.md](apps/mobile/README.md)

Resumo:

1. Backend rodando: `npm run dev:backend`
2. `apps/mobile/local.properties` com `SENTINELA_API_URL=http://10.0.2.2:8787`
3. Abra `apps/mobile` no Android Studio → Run no emulador

## Próximas fases

- **Fase 4:** deploy Cloudflare + CORS de produção
