# PROGETTAZIONE - GESTIONE EVENTI

Documento di architettura e progettazione della piattaforma di gestione eventi.
Backend Java con Spring Boot, frontend TypeScript con React, PostgreSQL, integrazione AI.

| Legenda | Significato |
|---|---|
| **implementato** | presente nel codice e verificato in locale |
| **da implementare** | progettato in questo documento, non ancora scritto |

## SCOSTAMENTI DAL MODELLO DI RIFERIMENTO

Quattro scelte divergono dal modello usato come base. Sono deliberate e motivate qui.
(Autenticazione JWT e provider AI OpenRouter coincidono con il modello di
riferimento, quindi non sono scostamenti.)

| Voce | Modello di riferimento | Questo progetto | Perché |
|---|---|---|---|
| Versione Java | Java 21 | **Java 25** con Spring Boot 4.1.1 | la consegna impone Spring Boot 4 su Java 25 |
| Lombok | previsto | **non adottato** | i DTO sono `record`, le entità hanno accessori espliciti. Aggiungere generazione di codice a progetto avviato non porta vantaggi e nasconde il contratto delle entità |
| Chiavi primarie | GUID | **BIGSERIAL** | chiavi corte e ordinate: indici più compatti e join più rapidi. Il codice pubblico del ticket è un UUID, perché quello sì circola fuori dal sistema |
| Ruolo organizzatore | ruolo globale `ORGANIZER` | **proprietà per evento** (`eventi.proprietario_id`) più ruoli `UTENTE` / `AMMINISTRATORE` | chiunque può creare un evento: essere organizzatore è una proprietà del singolo evento, non dell'account. Un ruolo globale non direbbe *di quale* evento si è organizzatori, e i permessi vanno verificati sull'evento |

## STACK TECNOLOGICO

### Piattaforma

| Voce | Valore |
|---|---|
| Linguaggio | Java 25 (LTS) |
| Framework | Spring Boot 4.1.1 |
| Build | Maven con wrapper (`./mvnw`) |
| Database | PostgreSQL 18, gestito con pgAdmin, schema `BUILD-WEEK-5` |
| Frontend | React 19, TypeScript, Vite 8, Tailwind 4 |
| Deploy | Render: Web Service Docker per il backend, Static Site per il frontend |

### Dipendenze Spring

| Dipendenza | A cosa serve |
|---|---|
| `spring-boot-starter-webmvc` | controller REST, negoziazione JSON con Jackson 3 |
| `spring-boot-starter-data-jpa` | entità, repository, transazioni. Hibernate 7 |
| `spring-boot-starter-validation` | vincoli sui DTO e sui parametri di richiesta |
| `spring-boot-starter-security` | catena dei filtri, BCrypt, autorizzazioni |
| `jjwt` (api/impl/jackson) | firma e verifica dei token JWT (HS256) |
| `spring-boot-starter-websocket` | STOMP per chat e notifiche in tempo reale |
| `spring-boot-starter-mail` | invio email su SMTP Gmail |
| `spring-boot-starter-actuator` | `/actuator/health` per il controllo di salute su Render |
| `postgresql` | driver JDBC |
| `spring-boot-devtools` | riavvio automatico in sviluppo |

Hash delle password: `BCryptPasswordEncoder` con costo 12, incluso in Spring Security.
Autenticazione con token JWT (libreria jjwt): API stateless, nessuna sessione lato
server e CSRF disattivato (il token viaggia nell'header, non in un cookie).

### Integrazione AI

| Voce | Valore |
|---|---|
| Provider | OpenRouter |
| Client | chiamata REST con `RestClient` (nessun SDK dedicato) |
| Modello | `nvidia/nemotron-nano-9b-v2:free`, configurabile con `AI_MODELLO` |
| Modalità | input multimodale (immagine per URL + testo) in una sola chiamata |
| Attivazione | `AI_ABILITATA=true` più `OPENROUTER_API_KEY`. Spenta, l'endpoint risponde `403` con messaggio esplicito |
| Confine dei dati | partono solo il titolo, la descrizione da migliorare e l'immagine principale di quell'evento, e solo su richiesta esplicita dell'organizzatore |

## ENTITÀ

Dieci entità più una tabella ponte. Chiavi primarie `BIGSERIAL`, istanti in `TIMESTAMPTZ`.

### Utente (`utenti`) — implementato

| Campo | Tipo | Note |
|---|---|---|
| id | BIGSERIAL | PK |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| passwordHash | VARCHAR(255) | solo hash BCrypt, mai in chiaro, mai nei log |
| nome, cognome | VARCHAR(60) | NOT NULL |
| indirizzo | VARCHAR(255) | |
| dataNascita | DATE | l'età è calcolata, non memorizzata |
| telefono | VARCHAR(30) | |
| latitudine, longitudine | DOUBLE | posizione facoltativa del profilo |
| ruolo | ENUM `Ruolo` | `UTENTE`, `AMMINISTRATORE` |
| verificato | BOOLEAN | `false` blocca l'uso della piattaforma |
| attivo | BOOLEAN | `false` dopo l'anonimizzazione |
| anonimizzato | BOOLEAN | traccia della richiesta di anonimizzazione |
| creatoIl | TIMESTAMPTZ | |

### CodiceVerifica (`codici_verifica`) — implementato

| Campo | Tipo | Note |
|---|---|---|
| id | BIGSERIAL | PK |
| utente | FK → utenti | N:1, cascata in cancellazione |
| codice | VARCHAR(6) | sei cifre da `SecureRandom` |
| scadeIl | TIMESTAMPTZ | 15 minuti |
| usato | BOOLEAN | una nuova richiesta invalida i precedenti |

### Evento (`eventi`) — implementato

| Campo | Tipo | Note |
|---|---|---|
| id | BIGSERIAL | PK |
| titolo | VARCHAR(140) | obbligatorio |
| descrizione | TEXT | migliorabile via AI |
| dataEvento | TIMESTAMPTZ | deve essere futura alla creazione |
| luogo | VARCHAR(140) | obbligatorio |
| indirizzo | VARCHAR(255) | |
| latitudine, longitudine | DOUBLE | obbligatorie: senza coordinate l'evento non è sulla mappa |
| capienza | INTEGER | facoltativa, positiva. Limita i ticket emessi |
| proprietario | FK → utenti | N:1. È l'organizzatore di questo evento |
| creatoIl, aggiornatoIl | TIMESTAMPTZ | |

### ImmagineEvento (`immagini_evento`) — implementato

| Campo | Tipo | Note |
|---|---|---|
| id | BIGSERIAL | PK |
| evento | FK → eventi | N:1, cascata |
| url | VARCHAR(500) | solo `https://`. File su storage esterno, non in database |
| principale | BOOLEAN | una sola per evento: è l'immagine passata all'AI |

### Artista (`artisti`) + ponte (`eventi_artisti`) — implementato

| Campo | Tipo | Note |
|---|---|---|
| id | BIGSERIAL | PK |
| nome | VARCHAR(120) | UNIQUE |
| genere | VARCHAR(60) | |

Ponte `eventi_artisti (evento_id, artista_id)` con chiave primaria composta.

### MarkerEvento (`marker_evento`) — implementato

| Campo | Tipo | Note |
|---|---|---|
| id | BIGSERIAL | PK |
| evento | FK → eventi | N:1, cascata |
| tipo | ENUM `TipoMarker` | `INGRESSO`, `USCITA`, `USCITA_SICUREZZA` |
| etichetta | VARCHAR(80) | |
| latitudine, longitudine | DOUBLE | NOT NULL |

### Ticket (`ticket`) — implementato

| Campo | Tipo | Note |
|---|---|---|
| id | BIGSERIAL | PK |
| codice | VARCHAR(36) | UNIQUE, UUID. È il dato che circola fuori dal sistema |
| evento | FK → eventi | N:1 |
| partecipante | FK → utenti | N:1 |
| nomeEvento, dataEvento, luogoEvento, nomePartecipante | copia all'emissione | il ticket è un documento: non cambia se l'evento cambia |
| emessoIl | TIMESTAMPTZ | |
| annullato | BOOLEAN | annullamento logico, nessuna cancellazione |

UNIQUE `(evento_id, partecipante_id)`: un partecipante, un ticket per evento.

### Notifica (`notifiche`) — implementato

| Campo | Tipo | Note |
|---|---|---|
| id | BIGSERIAL | PK |
| destinatario | FK → utenti | N:1 |
| evento | FK → eventi | facoltativa: le notifiche di amicizia non hanno evento |
| tipo | ENUM `TipoNotifica` | `EVENTO_MODIFICATO`, `MESSAGGIO_PROPRIETARIO`, `NUOVA_ISCRIZIONE`, `RICHIESTA_AMICIZIA`, `AMICIZIA_ACCETTATA` |
| messaggio | VARCHAR(500) | |
| letta | BOOLEAN | |
| creataIl | TIMESTAMPTZ | |

### Amicizia (`amicizie`) — implementato

| Campo | Tipo | Note |
|---|---|---|
| id | BIGSERIAL | PK |
| richiedente, destinatario | FK → utenti | N:M autoreferenziale, una riga per coppia |
| stato | ENUM `StatoAmicizia` | `IN_ATTESA`, `ACCETTATA`, `RIFIUTATA` |
| creataIl, aggiornataIl | TIMESTAMPTZ | |

UNIQUE `(richiedente_id, destinatario_id)` e CHECK di non autoreferenzialità.
La riga è direzionale per sapere chi deve rispondere; la lettura considera entrambi i versi.

### Messaggio (`messaggi`) — implementato

| Campo | Tipo | Note |
|---|---|---|
| id | BIGSERIAL | PK |
| mittente, destinatario | FK → utenti | N:M autoreferenziale |
| contenuto | VARCHAR(2000) | svuotato dall'anonimizzazione |
| inviatoIl | TIMESTAMPTZ | |
| lettoIl | TIMESTAMPTZ | NULL finché non letto |

### Enum

| Enum | Valori |
|---|---|
| `Ruolo` | UTENTE, AMMINISTRATORE |
| `TipoMarker` | INGRESSO, USCITA, USCITA_SICUREZZA |
| `TipoNotifica` | EVENTO_MODIFICATO, MESSAGGIO_PROPRIETARIO, NUOVA_ISCRIZIONE, RICHIESTA_AMICIZIA, AMICIZIA_ACCETTATA |
| `StatoAmicizia` | IN_ATTESA, ACCETTATA, RIFIUTATA |

## CONTROLLERS ENDPOINTS

Le richieste protette richiedono l'header `Authorization: Bearer <token>`, dove il
token è il JWT restituito dal login. L'API è stateless e senza CSRF.

### 1. Login e registrazione — implementato

`ControllerAutenticazione` → `ServizioUtenti`, `ServizioAutenticazione`

| Metodo | Percorso | Scopo | Logica di servizio |
|---|---|---|---|
| POST | `/api/auth/registrazione` | crea l'account | email normalizzata e controllata come unica, password cifrata BCrypt, codice a sei cifre salvato con scadenza 15 minuti e spedito per email |
| POST | `/api/auth/verifica` | conferma con il codice | cerca il codice non usato più recente, rifiuta se scaduto, lo segna usato e imposta `verificato = true` |
| POST | `/api/auth/codice` | reinvia il codice | invalida i precedenti e ne genera uno nuovo |
| POST | `/api/auth/login` | restituisce il token JWT | `AuthenticationManager` con `DaoAuthenticationProvider`; se le credenziali sono valide emette un JWT firmato HS256 (id, email, ruolo, scadenza). Account non verificato o disattivato → `403` con messaggio leggibile, credenziali errate → `400` generico |
| GET | `/api/auth/io` | dati dell'utente del token | |

Non c'è un endpoint di logout: essendo l'API stateless, il logout è lato client
(si scarta il token) e il token perde validità alla scadenza.

Validazioni: email conforme, password con almeno dieci caratteri, una lettera e una cifra,
`dataNascita` nel passato. I campi password sono esclusi dalla sanificazione HTML, così
restano esattamente quelli digitati.

### 2. Gestione utente — implementato

`ControllerProfilo` → `ServizioUtenti`

| Metodo | Percorso | Scopo | Logica di servizio |
|---|---|---|---|
| GET | `/api/utenti/me` | profilo | età calcolata da `dataNascita` |
| PUT | `/api/utenti/me` | aggiorna anagrafica e coordinate | validazione dei limiti geografici, stringhe sanificate in ingresso |

Ruoli: `UTENTE` è il predefinito, `AMMINISTRATORE` è previsto nel modello e nella
gerarchia di sicurezza. Non esiste un ruolo di organizzatore: chi crea un evento ne
diventa proprietario, e ogni operazione di modifica verifica la proprietà di *quel*
evento. Promozione a `AMMINISTRATORE`: **da implementare**, endpoint previsto
`PATCH /api/utenti/{id}/ruolo`, riservato agli amministratori.

### 3. Gestione eventi — implementato

`ControllerEventi` → `ServizioEventi`

| Metodo | Percorso | Scopo | Logica di servizio |
|---|---|---|---|
| GET | `/api/eventi` | elenco pubblico paginato | solo eventi futuri; filtro `testo` su titolo e luogo; proiezione con immagine principale in una sola query |
| GET | `/api/eventi/{id}` | dettaglio pubblico | una query con fetch di proprietario, immagini, marker e artisti |
| GET | `/api/eventi/miei` | eventi di cui si è proprietari | |
| POST | `/api/eventi` | crea | data futura obbligatoria, coordinate obbligatorie, artisti creati o riusati per nome, prima immagine promossa a principale se nessuna lo è |
| PUT | `/api/eventi/{id}` | modifica | verifica della proprietà; al termine pubblica `EventoAggiornato`, che fa partire le notifiche ai possessori di ticket |
| DELETE | `/api/eventi/{id}` | elimina | verifica della proprietà; cascata su immagini, marker e ponte artisti |
| GET | `/api/artisti` | elenco artisti | pubblico |

Filtri di ricerca: testo, paginazione e ordine per data sono attivi. Filtri per
categoria e per intervallo di date: **da implementare** — richiedono un campo
`categoria` su `eventi` e due parametri `da` / `a`.
Posti disponibili: calcolati come `capienza - ticket non annullati` in fase di
iscrizione; esposizione nel dettaglio dell'evento **da implementare**.

### 4. Prenotazioni / biglietti — implementato

`ControllerTicket` → `ServizioTicket`

| Metodo | Percorso | Scopo | Logica di servizio |
|---|---|---|---|
| POST | `/api/eventi/{id}/iscrizione` | emette il ticket | quattro controlli in sequenza: il proprietario non prende ticket del proprio evento, l'evento non deve essere passato, non deve esistere già un ticket valido, la capienza non deve essere esaurita. Poi genera il codice UUID, copia i dati dell'evento, invia il ticket per email al partecipante e avvisa il proprietario per email e con una notifica |
| GET | `/api/ticket` | storico dei propri ticket | ordinati per data evento |
| DELETE | `/api/ticket/{id}` | annulla | solo il possessore; annullamento logico, il posto torna disponibile |
| GET | `/api/eventi/{id}/partecipanti` | elenco partecipanti | visibile solo a chi possiede un ticket valido per quell'evento o al proprietario; riporta lo stato dell'amicizia con ciascuno |

### 5. Statistiche — **da implementare**

`ControllerStatistiche` → `ServizioStatistiche`

| Metodo | Percorso | Scopo | Logica di servizio prevista |
|---|---|---|---|
| GET | `/api/statistiche/eventi/{id}` | dashboard dell'organizzatore | verifica della proprietà, poi in una query di aggregazione: ticket emessi, annullati, posti residui, percentuale di riempimento, andamento delle iscrizioni per giorno |
| GET | `/api/statistiche/miei-eventi` | quadro di tutti i propri eventi | aggregato per evento, ordinato per data |
| POST | `/api/statistiche/eventi/{id}/report` | invia il report per email | riusa `ServizioMail`, invio asincrono, corpo HTML con la stessa aggregazione |

Le letture sono aggregazioni SQL parametrizzate, senza caricare i ticket in memoria.
Il canale WebSocket già presente permette di spingere l'aggiornamento del contatore
all'organizzatore a ogni nuova iscrizione, sulla destinazione `/utente/queue/statistiche`.

### 6. AI Controller — in parte implementato

`ControllerEventi` (miglioramento descrizione) → `MiglioratoreDescrizione`

| Metodo | Percorso | Scopo | Stato e logica |
|---|---|---|---|
| POST | `/api/eventi/{id}/descrizione/migliora` | riscrive la descrizione | **implementato**. Verifica la proprietà, prende l'immagine principale e la descrizione da migliorare, chiama il modello con istruzioni che vietano di inventare fatti, restituisce la proposta **senza salvarla**: decide l'organizzatore |
| GET | `/api/ai/raccomandazioni` | eventi consigliati | **da implementare**. Nessuna chiamata al modello nel percorso di lettura: si parte dai dati già presenti — categorie e artisti degli eventi con ticket, amicizie, posizione — e si ordina per affinità in SQL. Il modello serve solo se serve una spiegazione in linguaggio naturale del perché di un suggerimento |
| POST | `/api/eventi/{id}/analisi-capienza` | analisi della capienza | **da implementare**. Prima l'aggregazione: andamento delle iscrizioni, giorni residui, riempimento. Poi il modello riceve quei numeri, non le righe, e produce il commento e i suggerimenti |

Regole valide per tutto l'ambito AI:

1. Il modello non decide né scrive in database: propone, l'utente conferma.
2. Ogni chiamata parte da un'azione esplicita dell'utente, mai da un percorso di lettura.
3. Senza chiave configurata l'implementazione attiva è quella che rifiuta con `403` e messaggio chiaro: la funzione si spegne, l'applicazione no.
4. Gli errori del provider diventano un messaggio comprensibile, non un errore interno.

## I MIEI STEP

1. **Database e configurazione.** PostgreSQL locale, database `BUILD-WEEK-5` in pgAdmin, `application.yml` con le credenziali da variabili d'ambiente, `DATABASE_URL` di Render tradotta in formato JDBC all'avvio. Schema generato dalle entità con `ddl-auto: update`.
2. **Fondamenta del backend.** Riorganizzazione per domini, dipendenze, eccezioni di dominio, gestione centralizzata degli errori con risposta uniforme, sanificazione delle stringhe in ingresso.
3. **Sicurezza, CORS e WebSocket.** Catena dei filtri con autenticazione JWT stateless (CSRF disattivato), origini consentite da `ALLOWED_ORIGIN` per HTTP e per l'handshake WebSocket, intestazioni di sicurezza, endpoint STOMP `/ws` con code personali (token JWT nel frame CONNECT).
4. **Scaffolding dei domini.** Entità, repository, servizi e controller nell'ordine delle dipendenze: utenti, eventi, mailing, ticket e notifiche, amicizie e chat, mappa. Ogni dominio su un branch, con commit progressivi e merge su `develop`.
5. **Frontend.** Impalcatura con routing e client HTTP che invia il token JWT nell'header `Authorization`, poi le schermate nell'ordine in cui il backend si libera: autenticazione, eventi, ticket e notifiche live, mappa e chat, pagine di cookie e privacy policy.
6. **Revisione finale.** Collaudo di tutti gli endpoint con gli stati HTTP attesi, controllo delle autorizzazioni lato server, allineamento dei documenti al codice, deploy su Render con `JWT_SECRET`, le chiavi dei servizi (Google Geocoding, OpenRouter) e le variabili del mailing.

## STEP AI

1. **Controllo delle dipendenze.** Verificare che le librerie dichiarate esistano nelle versioni indicate e che l'SDK usato sia quello ufficiale del provider, leggendo la documentazione della versione in uso invece di ricordarne le firme. Risolvere le dipendenze prima di scrivere codice.
2. **Scrittura del codice.** Un dominio per volta, compilando a ogni passo. Le classi delle librerie esterne si verificano contro l'artefatto vero quando la documentazione non le copre: un errore del compilatore costa meno di un'ipotesi sbagliata.
3. **Controlli di sicurezza.** A ogni endpoint nuovo: chi può chiamarlo, quali dati arrivano dal client e come sono validati, dove sta il controllo di autorizzazione. Le protezioni si verificano provandole — richiesta senza token, richiesta da utente non proprietario, invio verso un non amico — non dichiarandole.
4. **Supporto al frontend.** Contratti degli endpoint documentati man mano, esempi di corpo e di risposta, formato degli errori uniforme, destinazioni WebSocket dichiarate. Il frontend non deve dedurre nulla dal codice del backend.
5. **Debug e revisione finale.** Ogni errore si legge nel log e si corregge alla radice. Collaudo completo ripetuto alla fine, elenco esplicito di quello che resta aperto: nessun test automatico, nessun limite ai tentativi di login, sessioni in memoria, funzione AI mai eseguita contro il servizio reale.

## RIFERIMENTI

| Documento | Contenuto |
|---|---|
| `docs/01-debriefing.md` | scelte progettuali con alternative scartate e rischi |
| `docs/02-schema-relazionale.md` | tabelle, vincoli, indici |
| `docs/03-piano-di-sviluppo.md` | divisione del lavoro e flusso Git |
| `docs/04-api.md` | contratto completo degli endpoint |
| `docs/05-privacy.md` | dati trattati, anonimizzazione, conservazione |
| `collaudo/collaudo-backend.sh` | collaudo degli endpoint, 55 controlli |
