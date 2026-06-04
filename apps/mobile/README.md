# Sentinela — App Android

App nativo (Kotlin + Compose) que consome o backend Sentinela e avalia proximidade de focos de calor em pontos monitorados.

## Pré-requisitos

- Android Studio (Ladybug ou mais recente)
- JDK 17+
- Backend local rodando: `npm run dev:backend` na raiz do monorepo
- SDK Android 35 instalado no Studio

## Configuração

1. Copie `local.properties.example` para `local.properties` na pasta `apps/mobile/`.
2. Ajuste `sdk.dir` para o caminho do seu Android SDK.
3. Use `SENTINELA_API_URL=http://127.0.0.1:8787` e, com o emulador aberto, rode:
   ```bash
   adb reverse tcp:8787 tcp:8787
   ```
   _(No Windows, isso é mais confiável que `10.0.2.2`.)_

> Dispositivo físico na mesma rede: use o IP da máquina, ex. `http://192.168.1.10:8787`.

## Abrir no Android Studio

**File → Open** → selecione a pasta `apps/mobile` (não a raiz do monorepo).

Aguarde o **Gradle Sync** terminar.

## Executar

1. Terminal na raiz do monorepo: `npm run dev:backend` (o script já usa `--ip 0.0.0.0` para o emulador Android alcançar a porta 8787)
2. Com emulador ligado: `adb reverse tcp:8787 tcp:8787`
3. No Studio: Run no emulador API 26+ (rebuild após mudar `local.properties`)
3. Na primeira execução (Android 13+), aceite a permissão de notificações quando solicitada.

## Build via linha de comando

```bash
cd apps/mobile
./gradlew assembleDebug
```

## Estrutura

```
app/src/main/java/com/sentinela/
├── data/       # Retrofit, Room, repositórios
├── domain/     # modelos e use cases (Haversine, proximidade)
├── ui/         # Compose + ViewModels por tela
├── notification/
└── di/         # AppContainer (DI manual)
```

## Critérios de aceite (Fase 3)

| Item | Como validar |
|------|----------------|
| Focos no mapa | Mapa inicial com marcadores no Brasil |
| Room | Cadastre um ponto, feche o app, reabra — ponto permanece |
| Alerta | Ponto com coordenadas/raio cobrindo um foco real → badge vermelho + notificação |
| Sem MAP_KEY | Apenas `BuildConfig.SENTINELA_API_URL` |

## Produção (Fase 4)

Altere `SENTINELA_API_URL` em `local.properties` (ou CI) para a URL do Worker publicado na Cloudflare.
