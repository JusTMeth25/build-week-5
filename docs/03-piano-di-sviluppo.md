# Piano di sviluppo e divisione del lavoro

Team: Lorenzo, Javier, Marco, Simone, Gianluca.
Durata: 5 giornate di lavoro più la presentazione.

## 1. Flusso Git

| Branch | Ruolo |
|---|---|
| `main` | solo versioni consegnabili. Nessuno sviluppa qui, nessun commit diretto |
| `develop` | integrazione: ogni funzionalità completata viene unita qui |
| `feature/be-*` | una funzionalità di backend, un autore |
| `feature/fe-*` | una funzionalità di frontend, un autore |
| `docs/*` | documentazione |

Regole condivise:

1. Si parte sempre da `develop` aggiornato: `git switch develop && git pull && git switch -c feature/be-xxx`.
2. Commit piccoli e frequenti, uno per passo compiuto, non uno per branch. Push a ogni commit, così il lavoro è visibile al resto del team.
3. Messaggio di commit in italiano, imperativo, una riga: cosa cambia e perché, non l'elenco dei file.
4. Ognuno committa e pusha con la propria identità Git (`git config user.name`, `user.email`): la firma dei commit è la tracciabilità del lavoro individuale.
5. Merge su `develop` con `--no-ff`, così il ramo della funzionalità resta leggibile nella storia.
6. Il branch si unisce solo se il backend compila e parte: `./mvnw -q verify`.
7. Chi unisce risolve i propri conflitti. Non si riscrive la storia di un branch già pushato.

## 2. Assegnazione del backend

### Javier — Fondamenta e sicurezza (Parte 6, Parte 7 lato dati)

Branch: `feature/be-fondamenta`, `feature/be-sicurezza-privacy`

- Struttura del progetto, dipendenze, configurazione `application.yml`, database locale.
- Spring Security: catena dei filtri, sessione autenticata, logout, gerarchia dei ruoli.
- Protezione CSRF con token in cookie, CORS sulle origini dichiarate, intestazioni di sicurezza, Content-Security-Policy.
- Pulizia in ingresso di tutte le stringhe contro XSS, validazione dei DTO.
- Gestione centralizzata degli errori con risposte uniformi.
- Anonimizzazione dei dati e disattivazione dell'account.
- Custode del flusso Git: apre `develop`, controlla i merge, aggiorna il README.

### Lorenzo — Autenticazione e utenti (Parte 1)

Branch: `feature/be-autenticazione`

- Entità `Utente`, ruoli, anagrafica completa (nome, cognome, indirizzo, data di nascita, telefono).
- Registrazione con email, password cifrata con BCrypt, dati anagrafici.
- Generazione e verifica del codice di conferma, scadenza, reinvio.
- Login e logout su sessione, endpoint del profilo, blocco degli account non verificati.
- Test manuali documentati della sequenza registrazione → verifica → login.

### Marco — Gestione eventi e AI (Parte 2, prima metà)

Branch: `feature/be-eventi`

- Entità `Evento`, `ImmagineEvento`, `Artista`, `MarkerEvento` e relazioni.
- Creazione, modifica, cancellazione ed elenco eventi con controllo di proprietà.
- Gestione delle immagini e dell'elenco artisti.
- Marker di ingressi, uscite e uscite di sicurezza.
- Geocoding dell'indirizzo con Google quando l'evento è creato senza coordinate.
- Miglioramento della descrizione tramite AI passando immagine e testo originale.

### Simone — Ticket, notifiche e mailing (Parte 2 seconda metà, Parte 4)

Branch: `feature/be-ticket-notifiche`, `feature/be-mailing`

- Entità `Ticket` con i dati copiati al momento dell'emissione, codice univoco.
- Iscrizione a un evento, emissione del ticket, elenco dei propri ticket.
- Notifica automatica a tutti i possessori di ticket a ogni modifica dell'evento.
- Notifica scritta a mano dal proprietario verso i partecipanti.
- Invio in tempo reale delle notifiche via WebSocket.
- Servizio di mailing su SMTP Gmail e le tre email obbligatorie: codice di conferma, ticket al partecipante, avviso al proprietario per ogni nuova iscrizione.

### Gianluca — Partecipanti, amicizie, chat e mappa (Parte 3, Parte 5)

Branch: `feature/be-social-chat`, `feature/be-mappa`

- Elenco partecipanti visibile solo a chi possiede un ticket per quell'evento.
- Richiesta di amicizia fra partecipanti, accettazione e rifiuto.
- Chat uno a uno abilitata solo con amicizia accettata, controllo lato server a ogni messaggio.
- Consegna dei messaggi in tempo reale via WebSocket sulla coda personale.
- Endpoint pubblico della mappa, senza autenticazione, su tutto il territorio nazionale.
- Ordinamento per vicinanza quando il client fornisce la posizione, con anteprima dell'evento.

## 3. Assegnazione del frontend

Si apre quando il backend della parte corrispondente è unito su `develop`.

| Persona | Branch | Contenuto |
|---|---|---|
| Lorenzo | `feature/fe-autenticazione` | registrazione, inserimento del codice, login, logout, profilo |
| Marco | `feature/fe-eventi` | creazione e modifica evento, caricamento immagini, artisti, pulsante di miglioramento AI |
| Simone | `feature/fe-ticket-notifiche` | pagina dei propri ticket, campanello delle notifiche live |
| Gianluca | `feature/fe-mappa-chat` | mappa pubblica con marker e anteprima, elenco partecipanti, amicizie, chat live |
| Javier | `feature/fe-impalcatura` | routing, client HTTP con CSRF, layout, pagine cookie policy e privacy policy |

## 4. Documentazione

| Documento | Responsabile | Contributi |
|---|---|---|
| `01-debriefing.md` | Javier | tutti: ogni scelta discussa finisce qui nello stesso commit che la applica |
| `02-schema-relazionale.md` | Lorenzo | Marco e Gianluca per le tabelle delle proprie aree |
| `03-piano-di-sviluppo.md` | Marco | aggiornato quando cambia un'assegnazione |
| `04-api.md` | Simone | ognuno documenta gli endpoint che scrive, nello stesso commit |
| `05-privacy.md` | Gianluca | Javier per la parte dei dati trattati |
| `README.md` | Javier | tutti |

Regola unica sulla documentazione: il documento si aggiorna nel commit che cambia
la scelta, non il giorno della consegna.

## 5. Calendario

| Giorno | Backend | Frontend | Documenti |
|---|---|---|---|
| 1 | riunione, fondamenta, sicurezza di base | impalcatura | debriefing, schema |
| 2 | autenticazione, eventi | autenticazione | api |
| 3 | ticket, notifiche, mailing | eventi | api, schema |
| 4 | amicizie, chat, mappa | ticket, notifiche | privacy |
| 5 | indurimento sicurezza, anonimizzazione | mappa, chat, policy | revisione completa |

## 6. Definizione di "finito"

Una funzionalità è finita quando:

1. il backend compila e parte con `./mvnw -q verify`;
2. gli input sono validati e le autorizzazioni sono verificate lato server;
3. gli endpoint sono in `04-api.md`;
4. il branch è unito su `develop` senza conflitti aperti;
5. la scelta progettuale, se nuova, è nel debriefing.
