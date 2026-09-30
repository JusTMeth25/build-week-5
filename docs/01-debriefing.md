# Verbale di debriefing

Progetto: piattaforma di gestione eventi
Team: Lorenzo, Javier, Marco, Simone, Gianluca
Stato del documento: si aggiorna ogni volta che una scelta cambia.

## 1. Riunione di apertura

Ordine del giorno: lettura della consegna, scelta dell'architettura, definizione
dello schema relazionale, divisione del lavoro, definizione del flusso Git.

Esito: lo stack è imposto dalla consegna (Spring Boot 4 su Java 25, React con
TypeScript, PostgreSQL via pgAdmin, WebSocket per il realtime, Git per
backend e frontend). Non è stata valutata nessuna alternativa allo stack.

## 2. Decisioni prese

### 2.1 Sessione autenticata, non JWT

**Scelta:** autenticazione con sessione server (`HttpSession`) e cookie di
sessione `HttpOnly`, protezione CSRF con token in cookie leggibile dal
frontend e header `X-XSRF-TOKEN`.

**Perché:** la consegna chiede esplicitamente "login e logout con sessione
autenticata" e, nella parte sulla sicurezza, la protezione CSRF. Le due cose
vanno insieme: il CSRF è un problema solo quando il browser allega da sé la
credenziale, cioè con i cookie.

**Alternativa scartata:** JWT in `localStorage`. Scartata perché non è una
sessione, rende il logout lato server un problema (il token resta valido fino
alla scadenza) ed è esposta a XSS, dato che qualunque script della pagina può
leggere `localStorage`.

### 2.2 Ticket con dati denormalizzati

**Scelta:** il ticket salva una copia di nome evento, data evento e nome
partecipante, oltre alle chiavi esterne.

**Perché:** il ticket è un documento emesso in un certo momento e inviato per
email. Se l'evento cambia titolo o data, il ticket già emesso deve continuare a
riportare quello che è stato consegnato al partecipante; la notifica di modifica
è il meccanismo previsto dalla consegna per comunicare il cambiamento.

**Alternativa scartata:** ricostruire il ticket con una join a ogni lettura.
Scartata perché il contenuto del ticket cambierebbe a posteriori.

### 2.3 Amicizia come riga direzionale con vincolo di unicità

**Scelta:** una sola riga per coppia, con richiedente e destinatario, stato
`IN_ATTESA` / `ACCETTATA` / `RIFIUTATA`, vincolo di unicità sulla coppia e
controllo di non autoreferenzialità.

**Perché:** serve sapere chi ha chiesto a chi per poter accettare o rifiutare.
La lettura "siamo amici?" si risolve con una query che considera entrambi i versi.

**Alternativa scartata:** due righe simmetriche inserite all'accettazione.
Scartata perché raddoppia i dati e richiede di tenerle coerenti a mano.

### 2.4 Regola della chat applicata nel backend

**Scelta:** ogni invio di messaggio verifica sul database l'esistenza di
un'amicizia accettata fra mittente e destinatario. Il controllo è nel servizio,
non nel controller e non nel frontend.

**Perché:** lo chiede la consegna ("il controllo sta nel backend, dettato dal
db") e perché il canale WebSocket è raggiungibile anche senza passare
dall'interfaccia.

### 2.5 Protezione XSS in ingresso più intestazioni di sicurezza

**Scelta:** ogni stringa che arriva dal client viene ripulita in fase di
deserializzazione JSON (escape dei caratteri HTML), più `Content-Security-Policy`,
`X-Content-Type-Options` e `Referrer-Policy` su tutte le risposte.

**Perché:** la protezione vale per qualunque client, non solo per il nostro
frontend, e non dipende dal fatto che React faccia escape in fase di render.

**Alternativa scartata:** affidarsi solo all'escape automatico di React.
Scartata perché il dato sporco resterebbe in database e il backend serve anche
altri client.

### 2.6 Distanza calcolata in SQL con query parametrizzata

**Scelta:** formula di Haversine in una query nativa con parametri, usata solo
per ordinare i risultati.

**Perché:** la consegna dice che la posizione dell'utente cambia l'ordine, non i
contenuti: l'ordinamento in database evita di caricare tutti gli eventi in
memoria. I parametri sono sempre associati, mai concatenati nella stringa SQL.

**Alternativa scartata:** PostGIS. Scartata perché aggiunge un'estensione al
database per un solo ordinamento.

### 2.7 Immagini salvate come URL

**Scelta:** la tabella delle immagini contiene l'URL del file, non il file.

**Perché:** i binari in database appesantiscono backup e query. Il piano gratuito
di Render non offre disco persistente, quindi i file stanno su un servizio esterno.

**Alternativa scartata:** colonna `bytea`.

### 2.8 Schema generato da Hibernate

**Scelta:** `ddl-auto: update` per la durata della build week.

**Perché:** lo schema cambia più volte al giorno mentre il team lavora in
parallelo; il modello Java resta l'unica fonte di verità.

**Alternativa scartata:** Flyway. Scartata solo per la durata del progetto: è la
scelta corretta appena lo schema si stabilizza, e va introdotta prima di mettere
dati reali in produzione.

### 2.9 Notifiche agganciate a un evento applicativo

**Scelta:** il servizio degli eventi pubblica `EventoAggiornato` con
`ApplicationEventPublisher`; chi manda le notifiche lo ascolta.

**Perché:** le notifiche e la gestione eventi sono di due persone diverse e di due
branch diversi. Senza questo passaggio il servizio degli eventi dovrebbe conoscere
quello delle notifiche, e ogni nuovo destinatario di un avviso sarebbe una modifica
al codice di chi gestisce gli eventi.

**Alternativa scartata:** iniettare il servizio delle notifiche dentro quello degli
eventi. Scartata per non legare le due aree e per non creare una dipendenza circolare
quando le notifiche dovranno leggere l'evento.

### 2.10 Letture tramite proiezioni

**Scelta:** gli elenchi non caricano l'entità `Evento` ma una proiezione con i soli
campi mostrati, immagine principale inclusa, in una sola query.

**Perché:** l'elenco non serve le collezioni dell'evento; caricarle produrrebbe una
query per riga. Con `open-in-view` disattivato le entità non si possono nemmeno
leggere fuori dalla transazione, quindi la conversione in DTO avviene nel servizio.

### 2.11 Invio email asincrono e non bloccante

**Scelta:** le email partono su un pool di thread separato e gli errori di consegna
vengono registrati senza propagarsi al chiamante.

**Perché:** SMTP è lento e può fallire per motivi esterni. Se l'invio fosse dentro la
transazione, un rifiuto di Gmail annullerebbe una registrazione o un'iscrizione già
valide, e l'utente pagherebbe un problema che non è suo.

**Conseguenza accettata:** l'utente può vedere l'operazione riuscita prima che la mail
arrivi. Per il codice di verifica esiste il reinvio, per il ticket resta la copia in
piattaforma.

### 2.12 Booleani assenti valgono falso

**Scelta:** `fail-on-null-for-primitives` disattivato in Jackson.

**Perché:** con l'impostazione predefinita di Spring Boot 4, un corpo JSON che
omette un campo booleano fa fallire la deserializzazione con un errore interno
invece della risposta di validazione attesa. Un campo assente ora vale `false`, e la
validazione dei DTO resta l'unico punto che decide se un campo è obbligatorio.

### 2.13 Geocoding degli indirizzi con Google

**Scelta:** alla creazione di un evento le coordinate sono facoltative; se mancano
il backend le ricava dall'indirizzo con Google Geocoding. La chiave sta nella
variabile d'ambiente `GOOGLE_MAPS_API_KEY` e la funzione si disattiva con
`GEOCODING_ABILITATO=false`.

**Perché:** l'utente può cliccare sulla mappa (arrivano le coordinate) oppure
scrivere solo l'indirizzo (non arrivano). Il secondo caso va coperto lato server,
così l'evento finisce sempre sulla mappa. Se le coordinate ci sono già, Google non
viene chiamato.

**Alternativa scartata:** geocodificare solo lato frontend. Scartata perché il
backend serve anche altri client e la garanzia "un evento ha sempre coordinate"
deve valere sul server, non dipendere dall'interfaccia.

## 3. Rischi individuati

| Rischio | Contromisura |
|---|---|
| SMTP Gmail bloccato o quota esaurita | l'invio non blocca la transazione; l'errore viene registrato e il codice resta valido |
| Chiave AI assente | la funzione di miglioramento descrizione è disattivabile; l'endpoint risponde con un errore chiaro |
| Chiave Google Geocoding assente | il geocoding si disattiva (`GEOCODING_ABILITATO=false` o chiave vuota); in quel caso le coordinate vanno inviate nel corpo della richiesta, altrimenti la creazione risponde `400` |
| Conflitti Git su branch paralleli | branch per funzionalità, merge frequenti su `develop`, nessuno sviluppo diretto su `main` |
| Deriva fra documenti e codice | i documenti si aggiornano nello stesso commit che cambia la scelta |
| Tentativi di password a ripetizione sul login | **non ancora coperto**: non c'è un limite ai tentativi. Va aggiunto un blocco temporaneo per email e per indirizzo IP prima di qualunque uso reale |
| Sessioni in memoria | un riavvio del backend fa decadere tutte le sessioni. Accettabile in sviluppo; in produzione servirebbe Spring Session su database o Redis |
| Nessun test automatico | il backend è verificato con `collaudo/collaudo-backend.sh`, che confronta gli stati HTTP attesi su tutto il percorso funzionale. I test di unità e di integrazione restano da scrivere |
