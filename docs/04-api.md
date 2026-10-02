# API del backend

Base: `http://localhost:8080` in locale.
Tutte le risposte sono JSON. Gli errori seguono lo stesso formato:

```json
{
  "istante": "2026-09-28T10:00:00Z",
  "stato": 400,
  "errore": "Richiesta non valida",
  "messaggio": "descrizione leggibile",
  "campi": { "titolo": "non può essere vuoto" }
}
```

L'autenticazione è con token JWT: il login restituisce il token, che va inviato
sulle richieste protette nell'header `Authorization: Bearer <token>`. L'API è
stateless (nessuna sessione lato server) e senza CSRF, perché il token viaggia
nell'header e non in un cookie inviato in automatico dal browser.

Ogni sezione viene compilata dall'autore della funzionalità corrispondente,
nello stesso commit che introduce gli endpoint.

## Diagnostica

| Metodo | Percorso | Accesso | Descrizione |
|---|---|---|---|
| GET | `/api/stato` | pubblico | nome del database collegato e ora del server |
| GET | `/actuator/health` | pubblico | health check |

## Autenticazione e utenti

| Metodo | Percorso | Accesso | Descrizione |
|---|---|---|---|
| POST | `/api/auth/registrazione` | pubblico | crea l'account e invia il codice di conferma. `201` |
| POST | `/api/auth/verifica` | pubblico | conferma l'account con il codice a sei cifre. `204` |
| POST | `/api/auth/codice` | pubblico | reinvia il codice di conferma. `204` |
| POST | `/api/auth/login` | pubblico | verifica le credenziali e restituisce il token JWT |
| GET | `/api/auth/io` | autenticato | dati dell'utente del token |
| GET | `/api/utenti/me` | autenticato | profilo |
| PUT | `/api/utenti/me` | autenticato | aggiorna anagrafica e coordinate |

Corpo della registrazione:

```json
{
  "email": "lorenzo@example.com",
  "password": "PasswordSicura1",
  "nome": "Lorenzo",
  "cognome": "Rossi",
  "indirizzo": "Via Roma 1, Milano",
  "dataNascita": "1998-04-12",
  "telefono": "3331234567"
}
```

La password richiede almeno dieci caratteri, una lettera e una cifra.
L'età non si invia: si calcola da `dataNascita`.

Un account non verificato che tenta il login riceve `403` con il messaggio che
invita a inserire il codice: la piattaforma resta inaccessibile fino alla conferma.

Risposta del login:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "utente": { "id": 1, "email": "lorenzo@example.com", "nome": "Lorenzo", "cognome": "Rossi", "ruolo": "UTENTE", "verificato": true }
}
```

Il token è un JWT firmato HS256, contiene id, email e ruolo e scade dopo
`app.sicurezza.jwt.scadenza-minuti` (120 minuti di default). Non esiste un endpoint
di logout: essendo l'API stateless, il logout è lato client (si scarta il token) e
il token perde validità alla scadenza.

## Eventi

| Metodo | Percorso | Accesso | Descrizione |
|---|---|---|---|
| GET | `/api/eventi` | pubblico | eventi futuri, paginati. Parametri: `testo`, `pagina`, `dimensione` |
| GET | `/api/eventi/{id}` | pubblico | dettaglio con artisti, immagini e marker |
| GET | `/api/eventi/miei` | autenticato | eventi di cui si è proprietario |
| POST | `/api/eventi` | autenticato | crea l'evento. `201` |
| PUT | `/api/eventi/{id}` | proprietario | modifica l'evento e avvisa i possessori di ticket |
| DELETE | `/api/eventi/{id}` | proprietario | elimina l'evento. `204` |
| POST | `/api/eventi/{id}/descrizione/migliora` | proprietario | propone la descrizione riscritta dall'AI |
| GET | `/api/artisti` | pubblico | elenco degli artisti censiti |

Corpo dell'evento:

```json
{
  "titolo": "Notte della musica elettronica",
  "descrizione": "serata con dj set dalle 22 alle 4",
  "dataEvento": "2026-12-20T21:00:00Z",
  "luogo": "Fabrique",
  "indirizzo": "Via Fantoli 9, Milano",
  "latitudine": 45.4408,
  "longitudine": 9.2612,
  "capienza": 1200,
  "artisti": ["Anyma", "Mind Against"],
  "immagini": [{ "url": "https://esempio.it/locandina.jpg", "principale": true }],
  "marker": [
    { "tipo": "INGRESSO", "etichetta": "Ingresso principale", "latitudine": 45.4409, "longitudine": 9.2613 },
    { "tipo": "USCITA_SICUREZZA", "etichetta": "Uscita nord", "latitudine": 45.4411, "longitudine": 9.261 }
  ]
}
```

`tipo` del marker: `INGRESSO`, `USCITA`, `USCITA_SICUREZZA`.
Gli artisti si passano per nome: quelli nuovi vengono creati, gli esistenti riusati.
Se nessuna immagine è marcata `principale`, lo diventa la prima: è quella che viene
passata all'AI insieme alla descrizione.

`latitudine` e `longitudine` sono facoltative. Se non arrivano, il backend le
ricava dall'`indirizzo` (o, in mancanza, dal `luogo`) chiamando Google Geocoding:
è il caso dell'utente che scrive solo l'indirizzo invece di cliccare sulla mappa.
Se le coordinate ci sono già, vengono usate così come sono e Google non viene
interrogato. Se non è possibile ricavarle (indirizzo non riconosciuto, oppure
geocoding non configurato) la risposta è `400`. Il geocoding richiede la variabile
d'ambiente `GOOGLE_MAPS_API_KEY`; con `GEOCODING_ABILITATO=false` o senza chiave è
spento e le coordinate diventano obbligatorie nel corpo della richiesta.

Il miglioramento AI restituisce la proposta senza salvarla, così l'organizzatore
decide se tenerla:

```json
{ "descrizioneOriginale": "...", "descrizioneMigliorata": "..." }
```

Chi non è il proprietario riceve `403` su modifica, cancellazione e miglioramento.

## Servizio di mailing

Non ha endpoint propri: le tre email partono dalle operazioni che le generano.

| Email | Quando | A chi |
|---|---|---|
| Codice di conferma | registrazione e richiesta di un nuovo codice | all'utente che si registra |
| Ticket | iscrizione a un evento | al partecipante |
| Avviso di nuova iscrizione | iscrizione a un evento | al proprietario dell'evento |

Provider Mailjet, tramite la sua API HTTP (Send API v3.1): il piano gratuito di Render
blocca le porte SMTP. Configurazione tramite variabili d'ambiente:

```
MAIL_ABILITATO=true
MAILJET_API_KEY=chiave-api
MAILJET_SECRET_KEY=chiave-segreta
MAIL_FROM=account@gmail.com
MAIL_FROM_NAME=EventVerse
```

`MAIL_FROM` deve essere un indirizzo verificato su Mailjet (Account > Sender
addresses), altrimenti Mailjet rifiuta l'invio.

Con `MAIL_ABILITATO=false`, cioè per impostazione predefinita in locale, le tre email
vengono scritte nel log del backend invece di essere spedite. È la modalità con cui si
sviluppa: il codice di verifica si legge dal log e il flusso resta completo.

L'invio è asincrono e i suoi errori non fanno fallire l'operazione che lo ha
richiesto: un'iscrizione resta valida anche se Gmail rifiuta la consegna.

## Ticket

| Metodo | Percorso | Accesso | Descrizione |
|---|---|---|---|
| POST | `/api/eventi/{id}/iscrizione` | autenticato | emette il ticket e manda le due email. `201` |
| GET | `/api/ticket` | autenticato | i propri ticket validi |
| DELETE | `/api/ticket/{id}` | possessore | annulla il ticket. `204` |

Risposta del ticket:

```json
{
  "id": 1,
  "codice": "459e550e-be31-45d2-a8ae-583af39e8cb5",
  "eventoId": 1,
  "nomeEvento": "Notte della musica elettronica",
  "dataEvento": "2026-12-20T21:00:00Z",
  "luogoEvento": "Fabrique",
  "nomePartecipante": "Marco Bianchi",
  "emessoIl": "2026-09-28T11:17:59Z"
}
```

L'iscrizione viene rifiutata con `409` quando: il ticket esiste già, l'evento è
passato, la capienza è esaurita, oppure chi chiede è il proprietario dell'evento.

## Notifiche

| Metodo | Percorso | Accesso | Descrizione |
|---|---|---|---|
| GET | `/api/notifiche` | autenticato | le proprie notifiche, dalla più recente |
| GET | `/api/notifiche/non-lette` | autenticato | `{ "nonLette": 2 }` |
| POST | `/api/notifiche/{id}/letta` | destinatario | segna letta. `204` |
| POST | `/api/notifiche/lette` | autenticato | segna lette tutte. `204` |
| POST | `/api/eventi/{id}/notifiche` | proprietario | avvisa a mano i partecipanti, `{ "avvisati": 1 }` |

Tipi: `EVENTO_MODIFICATO`, `MESSAGGIO_PROPRIETARIO`, `NUOVA_ISCRIZIONE`,
`RICHIESTA_AMICIZIA`, `AMICIZIA_ACCETTATA`.

Ogni `PUT /api/eventi/{id}` genera una notifica per tutti i possessori di un ticket
valido di quell'evento.

## Canale WebSocket

| Voce | Valore |
|---|---|
| Endpoint | `ws://localhost:8080/ws` (STOMP, senza SockJS) |
| Autenticazione | token JWT nell'header nativo `Authorization: Bearer <token>` del frame STOMP CONNECT |
| Coda delle notifiche | `/utente/queue/notifiche` |
| Prefisso dei messaggi in ingresso | `/app` |

La sottoscrizione è personale: il broker consegna a ciascuno solo la propria coda,
la destinazione non contiene l'identificativo dell'utente e non è indovinabile.
Senza token valido la connessione viene rifiutata.

## Partecipanti e amicizie

| Metodo | Percorso | Accesso | Descrizione |
|---|---|---|---|
| GET | `/api/eventi/{id}/partecipanti` | possessore di ticket o proprietario | elenco dei partecipanti con lo stato dell'amicizia |
| POST | `/api/amicizie/{utenteId}` | autenticato | chiede l'amicizia. `201` |
| POST | `/api/amicizie/{id}/accetta` | destinatario | accetta la richiesta |
| POST | `/api/amicizie/{id}/rifiuta` | destinatario | rifiuta la richiesta |
| GET | `/api/amicizie` | autenticato | i propri amici |
| GET | `/api/amicizie/richieste` | autenticato | richieste ricevute in attesa |

L'elenco dei partecipanti risponde `403` a chi non ha un ticket per quell'evento.
La richiesta di amicizia è ammessa solo verso chi partecipa a un evento in comune:
altrimenti `403`. Una richiesta già in attesa o già accettata dà `409`; solo il
destinatario può accettare o rifiutare, a chiunque altro risponde `403`.

## Chat

| Metodo | Percorso | Accesso | Descrizione |
|---|---|---|---|
| POST | `/api/chat/messaggi` | amico accettato | invia un messaggio. `201` |
| GET | `/api/chat/{utenteId}/messaggi` | amico accettato | conversazione paginata, segna letti i ricevuti |
| GET | `/api/chat/non-letti` | autenticato | `{ "nonLetti": 3 }` |

Sul WebSocket:

| Destinazione | Verso | Contenuto |
|---|---|---|
| `/app/chat` | client → server | `{ "destinatarioId": 3, "contenuto": "..." }` |
| `/utente/queue/messaggi` | server → client | il messaggio, recapitato a mittente e destinatario |
| `/utente/queue/errori` | server → client | `{ "errore": "..." }` quando l'invio viene rifiutato |

Il controllo dell'amicizia è nel servizio, non nel controller: vale allo stesso modo
per la chiamata REST e per il frame STOMP. Un invio verso un utente con cui non esiste
un'amicizia accettata produce `403` su REST e un messaggio sulla coda degli errori sul
WebSocket, e in nessun caso il messaggio viene salvato.

## Mappa e geolocalizzazione

| Metodo | Percorso | Accesso | Descrizione |
|---|---|---|---|
| GET | `/api/mappa/eventi` | pubblico | eventi futuri per la mappa. Parametri: `latitudine`, `longitudine`, `massimo` |
| GET | `/api/mappa/eventi/{id}` | pubblico | anteprima del marker |

Senza `latitudine` e `longitudine` l'ordine è per data e `distanzaKm` è assente.
Con la posizione l'ordine è per vicinanza e ogni elemento porta `distanzaKm`.
I contenuti sono gli stessi in entrambi i casi: la posizione cambia l'ordine, non
l'insieme degli eventi, che copre tutto il territorio nazionale.

Misurato in locale con cinque eventi, da Roma:

```
   3.5 km  Auditorium Parco della Musica
 184.9 km  Arena Flegrea
 426.9 km  Teatro Massimo
 471.6 km  Fabrique
 524.0 km  Piazza Castello
```

Anteprima del marker:

```json
{
  "id": 1,
  "titolo": "Notte della musica elettronica",
  "dataEvento": "2026-12-20T20:00:00Z",
  "luogo": "Fabrique",
  "indirizzo": "Via Fantoli 9, Milano",
  "immaginePrincipale": "https://esempio.it/locandina.jpg",
  "artisti": ["Anyma", "Mind Against"],
  "marker": { "INGRESSO": 1 },
  "collegamento": "/api/eventi/1"
}
```

`collegamento` è la risorsa completa dell'evento: è il passaggio dall'anteprima alla
pagina dell'evento.

La distanza è calcolata con la formula di Haversine dentro la query, con i parametri
associati e mai concatenati; serve solo a ordinare.

## Privacy

| Metodo | Percorso | Accesso | Descrizione |
|---|---|---|---|
| POST | `/api/privacy/anonimizzazione` | autenticato | anonimizza i propri dati e disattiva l'account. `204` |

```json
{ "password": "la-propria-password", "confermo": true }
```

Servono entrambi i campi: la password perché l'operazione è irreversibile, `confermo`
perché comporta la disattivazione dell'account. Senza conferma o con password errata la
risposta è `400` e nulla viene toccato. Al termine l'account è disattivato e non
consente più il login; un token già emesso resta valido fino alla sua scadenza.

Effetto misurato in locale su un utente con un ticket:

```
utenti:  email -> rimosso-6@anonimo.invalid, nome -> "Utente rimosso",
         indirizzo/telefono/data_nascita/coordinate svuotati,
         attivo = false, anonimizzato = true
ticket:  conservato, nome_partecipante -> "Utente rimosso"
eventi:  conservati
messaggi: contenuto rimosso
```

## Sicurezza applicata

| Protezione | Come |
|---|---|
| Autenticazione | token JWT firmato HS256 (chiave da `JWT_SECRET`, almeno 256 bit), inviato in `Authorization: Bearer`; API stateless, nessuna sessione lato server |
| CSRF | non applicabile: il token viaggia nell'header, non in un cookie inviato in automatico dal browser, quindi la protezione CSRF è disattivata |
| CORS | solo le origini di `ALLOWED_ORIGIN`, con credenziali, header e metodi dichiarati uno per uno |
| XSS | ogni stringa in ingresso viene ripulita in deserializzazione, più `Content-Security-Policy: default-src 'none'` |
| SQL injection | solo JPQL e query native con parametri associati: nessuna concatenazione di stringhe in SQL |
| Validazione | vincoli sui DTO e sui parametri, con errori campo per campo |
| Autorizzazioni | verificate nei servizi, non nei controller e mai solo nel frontend |
| Intestazioni | `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy`, `Permissions-Policy`, HSTS |

Le password sono salvate solo come hash BCrypt con costo 12 e i campi password sono
esclusi dalla sanificazione, così restano esattamente quelli digitati.

HSTS viene emesso solo sulle richieste HTTPS: in locale, su HTTP, l'intestazione non
compare ed è corretto che sia così.
