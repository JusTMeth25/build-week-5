# Piattaforma di gestione eventi - BE + FE + PostgreSQL

Applicazione full-stack: creazione e scoperta di eventi su mappa, ticket,
notifiche in tempo reale, amicizie e chat, con backend Spring Boot e frontend
React. Pronta per il deploy su Render.

| Parte | Tecnologia | In locale | Su Render |
|---|---|---|---|
| Backend | Spring Boot 4.1.1, Java 25, Maven wrapper | `be` sulla 8080 | Web Service (Docker) |
| Frontend | React 19, Vite, TypeScript, Tailwind 4 | `fe` sulla 5173 | Static Site |
| Database | PostgreSQL | locale sulla 5432 | Render PostgreSQL |

## Documentazione

La cartella [`docs/`](docs/) contiene la progettazione completa:

| File | Contenuto |
|---|---|
| [`docs/01-debriefing.md`](docs/01-debriefing.md) | verbale e decisioni progettuali con le alternative scartate |
| [`docs/02-schema-relazionale.md`](docs/02-schema-relazionale.md) | schema del database, tabelle e indici |
| [`docs/03-piano-di-sviluppo.md`](docs/03-piano-di-sviluppo.md) | flusso Git, divisione del lavoro, calendario |
| [`docs/04-api.md`](docs/04-api.md) | tutti gli endpoint del backend |
| [`docs/05-privacy.md`](docs/05-privacy.md) | dati trattati, destinatari, anonimizzazione |
| [`docs/diagramma-er.webp`](docs/diagramma-er.webp) | diagramma entità-relazioni |

## Funzionalità

Autenticazione con verifica via email, gestione eventi (immagini, artisti,
marker, miglioramento della descrizione tramite AI), ticket, notifiche in tempo
reale via WebSocket, amicizie, chat uno-a-uno, mappa pubblica con ordinamento per
vicinanza, geocoding degli indirizzi con Google, anonimizzazione dei dati.

Elenco completo degli endpoint in [`docs/04-api.md`](docs/04-api.md). Endpoint di
diagnostica:

| Metodo | Percorso | Cosa fa |
|---|---|---|
| GET | `/api/stato` | nome del database collegato e ora del server |
| GET | `/actuator/health` | health check per Render |

## Variabili d'ambiente

I segreti non stanno nel codice: si passano da variabili d'ambiente (i valori
dopo i due punti in `application.yml` sono i default locali).

| Variabile | Obbligatoria | Scopo |
|---|---|---|
| `JWT_SECRET` | sì | segreto per firmare i token di accesso |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | no in locale | connessione al database |
| `GOOGLE_MAPS_API_KEY` | solo per il geocoding | ricava le coordinate dall'indirizzo dell'evento |
| `GEOCODING_ABILITATO` | no | `false` disattiva il geocoding |
| `OPENROUTER_API_KEY` | solo per l'AI | miglioramento AI delle descrizioni |
| `AI_ABILITATA` | no | `false` disattiva l'AI |
| `MAIL_ABILITATO`, `MAIL_USERNAME`, `MAIL_PASSWORD` | solo per l'invio email | SMTP Gmail; con `false` le email finiscono nel log |
| `ALLOWED_ORIGIN` | no in locale | origini CORS ammesse |

## Avvio in locale

1. PostgreSQL sulla 5432 e database creato:
   ```
   createdb -U postgres BUILD-WEEK-5
   ```
   Credenziali diverse da `postgres` / `admin`: variabili `DB_URL`, `DB_USERNAME`,
   `DB_PASSWORD`, oppure `be/src/main/resources/application.yml`.
2. Imposta almeno `JWT_SECRET` (e le chiavi che ti servono, vedi tabella sopra).
   Le email e l'AI in locale possono restare spente (`MAIL_ABILITATO=false`,
   `AI_ABILITATA=false`): il codice di verifica si legge dal log del backend.
3. Doppio clic su `avvia.cmd` (Windows) o `./avvia.sh` (macOS/Linux), oppure:
   ```
   cd be && .\mvnw.cmd spring-boot:run
   cd fe && npm install && npm run dev
   ```
4. http://localhost:5173

## Deploy su Render

1. Repository Git con `be/`, `fe/`, `render.yaml` nella radice.
2. **New > Blueprint**, si sceglie la repo: nascono `app-db`, `app-be`, `app-fe`
   (rinominarli in `render.yaml` prima del primo deploy).
3. Dopo la prima build si impostano le variabili `sync: false`, senza `/` finale:

   | Servizio | Variabile | Valore |
   |---|---|---|
   | `app-be` | `ALLOWED_ORIGIN` | `https://app-fe.onrender.com` |
   | `app-be` | `JWT_SECRET` | segreto lungo e casuale |
   | `app-be` | `GOOGLE_MAPS_API_KEY` | chiave Google (se serve il geocoding) |
   | `app-be` | `OPENROUTER_API_KEY` | chiave OpenRouter (se serve l'AI) |
   | `app-fe` | `VITE_API_URL` | `https://app-be.onrender.com` |

4. **Manual Deploy** di entrambi (`VITE_API_URL` è letta in fase di build).

## Struttura

```
render.yaml                 blueprint: database + backend + frontend
avvia.cmd / avvia.sh        avvio locale (Windows / macOS-Linux)
schema-drawsql.sql          schema importabile in DrawSQL
docs/                       progettazione (vedi tabella sopra) + diagramma-er.webp
be/
  Dockerfile                usato solo da Render
  src/main/java/it/epicode/eventi/
    PiattaformaEventiApplication.java   avvio dell'applicazione
    utente/                 registrazione, verifica, login (JWT), profilo
    evento/                 eventi, immagini, artisti, marker
    geocoding/              indirizzo -> coordinate (Google Geocoding)
    ticket/                 iscrizioni ed emissione ticket
    notifica/               notifiche + invio realtime
    amicizia/               richieste di amicizia e partecipanti
    chat/                   messaggi uno-a-uno (REST + WebSocket)
    mappa/                  eventi per la mappa e ordinamento per vicinanza
    mail/                   invio email (SMTP Gmail)
    ai/                     miglioramento descrizioni (OpenRouter)
    privacy/                anonimizzazione dei dati
    config/                 sicurezza, JWT, CORS, WebSocket, asincronia
    comune/                 eccezioni, sanificazione testo, errori uniformi
  src/main/resources/application.yml
fe/
  src/lib/api.ts            base delle fetch, da VITE_API_URL
  src/App.tsx               pagina iniziale
  .env.example
```
