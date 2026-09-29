# Piattaforma di gestione eventi

Progetto di build week: creazione e gestione di eventi, ticket, notifiche e chat in
tempo reale, mappa pubblica con geolocalizzazione.

| Parte | Tecnologia | In locale | Su Render |
|---|---|---|---|
| Backend | Spring Boot 4.1.1, Java 25, Maven wrapper | `be` sulla 8080 | Web Service (Docker) |
| Frontend | React 19, Vite, TypeScript, Tailwind 4 | `fe` sulla 5173 | Static Site |
| Database | PostgreSQL 18, gestito con pgAdmin | `BUILD-WEEK-5` sulla 5432 | Render PostgreSQL |
| Realtime | WebSocket con STOMP | `/ws` | `/ws` |

Stato: **backend completo**, frontend ancora allo scheletro di partenza.

## Documenti

| Documento | Contenuto |
|---|---|
| [PROGETTAZIONE.md](PROGETTAZIONE.md) | documento di architettura: stack, entità, endpoint, passi di sviluppo |
| [docs/01-debriefing.md](docs/01-debriefing.md) | verbale della riunione, scelte prese e alternative scartate |
| [docs/02-schema-relazionale.md](docs/02-schema-relazionale.md) | tabelle, relazioni, vincoli, indici |
| [docs/03-piano-di-sviluppo.md](docs/03-piano-di-sviluppo.md) | divisione del lavoro, flusso Git, calendario |
| [docs/04-api.md](docs/04-api.md) | tutti gli endpoint del backend |
| [docs/05-privacy.md](docs/05-privacy.md) | dati trattati, anonimizzazione, conservazione |

## Avvio in locale

1. PostgreSQL sulla 5432 con il database creato:
   ```
   createdb -U postgres BUILD-WEEK-5
   ```
2. `./avvia.sh` (macOS/Linux) oppure doppio clic su `avvia.cmd` (Windows).
   Lo script fissa `DB_URL` sul database del progetto, controlla la 5432, installa le
   dipendenze del frontend al primo avvio e lancia backend e frontend insieme.
3. Indirizzi: applicazione su http://localhost:5173, stato su
   http://localhost:8080/api/stato, salute su http://localhost:8080/actuator/health.

Il primo avvio crea le tabelle da solo: lo schema è generato dalle entità JPA.

## Configurazione

| Variabile | Valore predefinito | A cosa serve |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/BUILD-WEEK-5` | database |
| `DB_USERNAME` / `DB_PASSWORD` | `postgres` / `admin` | credenziali del database |
| `ALLOWED_ORIGIN` | `http://localhost:5173,http://localhost:4173` | origini ammesse per CORS e WebSocket |
| `COOKIE_SECURE` | `false` | cookie di sessione solo su HTTPS: `true` in produzione |
| `MAIL_ABILITATO` | `false` | con `false` le email finiscono nel log invece di partire |
| `MAIL_HOST` / `MAIL_PORT` | `smtp.gmail.com` / `587` | server SMTP |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | vuoti | account Gmail e password per le app |
| `MAIL_FROM` | `no-reply@piattaforma-eventi.it` | mittente delle email |
| `AI_ABILITATA` | `false` | attiva il miglioramento della descrizione |
| `AI_MODELLO` | `claude-opus-5` | modello usato per la riscrittura |
| `ANTHROPIC_API_KEY` | vuota | chiave del servizio AI |

Se nel terminale è presente una `DB_URL` di un altro progetto, avviare il backend a
mano con `./mvnw spring-boot:run` lo farebbe collegare al database sbagliato: `avvia.sh`
e `avvia.cmd` impostano la variabile del progetto e risolvono il problema.

## Collaudo del backend

Con backend e database avviati:

```
./collaudo/collaudo-backend.sh /percorso/del/log-del-backend
```

Lo script percorre tutto il funzionale, dagli endpoint pubblici all'anonimizzazione,
e confronta lo stato HTTP atteso con quello ottenuto. Il file di log serve solo a
leggere il codice di verifica quando `MAIL_ABILITATO=false`. Ultimo esito: 55 controlli
superati su 55.

## Struttura del backend

```
be/src/main/java/it/epicode/eventi/
  PiattaformaEventiApplication.java
  config/         sicurezza, CORS, WebSocket, invio asincrono, URL del database
  comune/         eccezioni di dominio, gestione errori, sanificazione del testo
  utente/         anagrafica, registrazione, verifica, sessione, profilo
  evento/         eventi, immagini, artisti, marker
  ai/             miglioramento della descrizione
  ticket/         emissione e gestione dei ticket
  notifica/       notifiche persistenti e consegna in tempo reale
  amicizia/       partecipanti, richieste di amicizia
  chat/           messaggi uno a uno, REST e WebSocket
  mappa/          mappa pubblica e ordinamento per vicinanza
  privacy/        anonimizzazione dei dati
  diagnostica/    endpoint di stato
```

## Flusso Git

`main` contiene solo versioni consegnabili, `develop` integra il lavoro, ogni
funzionalità vive su un branch `feature/...`. Nessuno sviluppa direttamente su `main`.
Il dettaglio delle regole è in [docs/03-piano-di-sviluppo.md](docs/03-piano-di-sviluppo.md).

## Deploy su Render

1. Repository con `be/`, `fe/`, `render.yaml` nella radice.
2. **New > Blueprint**, si sceglie la repo: nascono database, backend e frontend.
3. Dopo la prima build si impostano le variabili `sync: false`, senza `/` finale:

   | Servizio | Variabile | Valore |
   |---|---|---|
   | `app-be` | `ALLOWED_ORIGIN` | `https://app-fe.onrender.com` |
   | `app-fe` | `VITE_API_URL` | `https://app-be.onrender.com` |

4. In produzione servono anche `COOKIE_SECURE=true` e le variabili del mailing.
5. **Manual Deploy** di entrambi: `VITE_API_URL` è letta in fase di build.
