# Privacy e trattamento dei dati

Documento di lavoro: descrive quello che il sistema tratta davvero. Le pagine
pubbliche di cookie policy e privacy policy si scrivono su questo contenuto.

## 1. Dati raccolti

| Dato | Dove | Perché |
|---|---|---|
| Email | registrazione | identificazione, invio del codice di conferma e del ticket |
| Password | registrazione | accesso. Salvata solo come hash BCrypt, mai in chiaro, mai nei log |
| Nome e cognome | registrazione | intestazione del ticket, elenco partecipanti |
| Indirizzo, data di nascita, telefono | registrazione | anagrafica richiesta dalla consegna |
| Posizione geografica | mappa, solo con consenso del browser | ordinare gli eventi per vicinanza |
| Eventi creati, ticket, amicizie, messaggi | uso della piattaforma | funzionamento del servizio |
| Token di accesso (JWT) | login | autenticare le richieste successive; contiene id, email e ruolo, scade dopo 120 minuti |

Il token JWT non è un cookie: lo conserva il client e lo invia nell'header
`Authorization`. Nessun cookie di sessione, nessun cookie di profilazione, nessun
servizio di analisi di terze parti.

## 2. Posizione geografica

La mappa è pubblica e funziona senza login e senza posizione. La posizione, se
concessa, viene usata solo per ordinare i risultati e non viene salvata in
database: cambia l'ordine, non i contenuti. Le coordinate salvate nel profilo
sono un dato facoltativo che l'utente inserisce lui.

## 3. Destinatari

I dati non vengono ceduti. Escono dal sistema in due soli casi:

- **SMTP Gmail**, per recapitare il codice di conferma, il ticket e l'avviso al proprietario dell'evento;
- **API di OpenRouter**, quando l'utente chiede il miglioramento della descrizione di un evento: partono l'immagine e il testo di quell'evento, nient'altro. La chiamata avviene solo su richiesta esplicita;
- **Google Geocoding API**, quando si crea un evento indicando solo l'indirizzo (senza cliccare sulla mappa): viene inviato quel testo di indirizzo per ottenere le coordinate, nient'altro. Se le coordinate sono già presenti Google non viene interrogato.

## 4. Anonimizzazione e disattivazione

L'utente può chiedere l'anonimizzazione dei propri dati. L'effetto:

| Dato | Dopo l'anonimizzazione |
|---|---|
| Email | sostituita con un valore interno non recapitabile |
| Nome e cognome | "Utente rimosso" |
| Indirizzo, telefono, data di nascita, coordinate | svuotati |
| Password | sostituita con un valore casuale non utilizzabile |
| Account | `attivo = false`, `anonimizzato = true`: nessun accesso possibile |
| Messaggi inviati | contenuto rimosso |
| Ticket | conservati, con il nome del partecipante sostituito |
| Eventi creati | conservati, con il proprietario mostrato come utente rimosso |

L'anonimizzazione comporta la disattivazione dell'account e non è reversibile.

Non viene fatta una cancellazione fisica della riga: gli eventi e i ticket
riguardano anche altre persone e la loro cancellazione a catena distruggerebbe i
dati di terzi. La scelta è la minimizzazione: resta il minimo necessario perché i
dati altrui restino coerenti, senza più alcun riferimento alla persona.

## 5. Conservazione

| Dato | Conservazione |
|---|---|
| Codici di verifica | 15 minuti, poi inutilizzabili |
| Token di accesso (JWT) | valido fino alla scadenza (120 minuti); non conservato lato server |
| Account e contenuti | fino alla richiesta di anonimizzazione |

## 6. Sicurezza applicata

Password con hash BCrypt, autenticazione con token JWT (HS256) e API stateless,
CORS limitato alle origini dichiarate, escape delle stringhe in ingresso contro
XSS, query sempre parametrizzate contro SQL injection, controlli di
autorizzazione nel backend su ogni risorsa.
