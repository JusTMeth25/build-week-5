#!/usr/bin/env bash
# Collaudo degli endpoint del backend: confronta lo stato HTTP atteso con quello
# ottenuto su tutto il percorso funzionale, dal pubblico all'anonimizzazione.
#
#   ./collaudo/collaudo-backend.sh [file-di-log-del-backend]
#
# Il file di log serve a leggere il codice di verifica quando il mailing e'
# disattivato (MAIL_ABILITATO=false), cioe' nella configurazione predefinita.
# L'autenticazione e' con token JWT: il login restituisce il token, che viene
# inviato nell'header Authorization: Bearer sulle richieste protette.
set -u
LOG="${1:-}"; ok=0; ko=0
B="${BASE_URL:-http://localhost:8080}"
RESP_CODE=""; RESP_TOKEN=""
verifica() { local nome="$1" attesa="$2" reale="$3"
  if [ "$attesa" = "$reale" ]; then ok=$((ok+1)); printf "  ok      %-50s %s\n" "$nome" "$reale"
  else ko=$((ko+1)); printf "  FALLITO %-50s atteso %s, ottenuto %s\n" "$nome" "$attesa" "$reale"; fi; }
# chiama METODO PERCORSO [TOKEN] [CORPO]: con TOKEN aggiunge Authorization: Bearer
chiama() { local metodo="$1" percorso="$2" token="${3:-}" corpo="${4:-}"
  local auth=(); [ -n "$token" ] && auth=(-H "Authorization: Bearer $token")
  if [ -z "$corpo" ]; then
    curl -s -o /dev/null -w '%{http_code}' -X "$metodo" "$B$percorso" "${auth[@]}"
  else
    curl -s -o /dev/null -w '%{http_code}' -X "$metodo" "$B$percorso" "${auth[@]}" \
      -H 'Content-Type: application/json' --data-binary "$corpo"
  fi; }
# login CORPO: imposta RESP_CODE (stato HTTP) e RESP_TOKEN (token estratto)
login() { local corpo="$1" tmp body
  tmp=$(curl -s -w $'\n%{http_code}' -X POST "$B/api/auth/login" \
    -H 'Content-Type: application/json' --data-binary "$corpo")
  RESP_CODE="${tmp##*$'\n'}"; body="${tmp%$'\n'*}"
  RESP_TOKEN=$(printf '%s' "$body" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p'); }
codice() { grep -o "codice di verifica per $1: [0-9]*" "$LOG" | tail -1 | grep -o '[0-9]\{6\}$'; }

echo "PUBBLICO"
verifica "stato del servizio" 200 "$(chiama GET /api/stato)"
verifica "salute" 200 "$(chiama GET /actuator/health)"
verifica "elenco eventi senza login" 200 "$(chiama GET /api/eventi)"
verifica "ricerca per testo" 200 "$(chiama GET '/api/eventi?testo=arena')"
verifica "mappa senza login" 200 "$(chiama GET /api/mappa/eventi)"
verifica "mappa con posizione" 200 "$(chiama GET '/api/mappa/eventi?latitudine=41.9&longitudine=12.5')"
verifica "anteprima marker senza login" 200 "$(chiama GET /api/mappa/eventi/1)"
verifica "notifiche senza login" 401 "$(chiama GET /api/notifiche)"
verifica "partecipanti senza login" 401 "$(chiama GET /api/eventi/1/partecipanti)"
verifica "evento inesistente" 404 "$(chiama GET /api/eventi/99999)"

echo "AUTENTICAZIONE"
E="smoke$RANDOM@example.com"
REG=$(printf '{"email":"%s","password":"PasswordSicura7","nome":"Smoke","cognome":"Test","indirizzo":"Via Prova 1","dataNascita":"1994-05-05","telefono":"3401112223"}' "$E")
verifica "registrazione" 201 "$(chiama POST /api/auth/registrazione "" "$REG")"
verifica "password troppo debole" 400 "$(chiama POST /api/auth/registrazione "" '{"email":"debole@example.com","password":"corta","nome":"A","cognome":"B"}')"
verifica "email non valida" 400 "$(chiama POST /api/auth/registrazione "" '{"email":"non-una-email","password":"PasswordSicura7","nome":"A","cognome":"B"}')"
verifica "email duplicata" 409 "$(chiama POST /api/auth/registrazione "" "$REG")"
ACC=$(printf '{"email":"%s","password":"PasswordSicura7"}' "$E")
login "$ACC"; verifica "login non verificato" 403 "$RESP_CODE"
VER_KO=$(printf '{"email":"%s","codice":"000000"}' "$E")
verifica "codice errato" 400 "$(chiama POST /api/auth/verifica "" "$VER_KO")"
VER_OK=$(printf '{"email":"%s","codice":"%s"}' "$E" "$(codice "$E")")
verifica "verifica con codice dal log" 204 "$(chiama POST /api/auth/verifica "" "$VER_OK")"
login "$ACC"; verifica "login verificato" 200 "$RESP_CODE"; TOK_S="$RESP_TOKEN"
verifica "dati dell'utente dal token" 200 "$(chiama GET /api/auth/io "$TOK_S")"
verifica "senza token" 401 "$(chiama PUT /api/utenti/me "" '{"nome":"X","cognome":"Y"}')"
verifica "aggiornamento profilo" 200 "$(chiama PUT /api/utenti/me "$TOK_S" '{"nome":"Smoke","cognome":"Test","latitudine":45.46,"longitudine":9.19}')"
verifica "coordinate fuori scala" 400 "$(chiama PUT /api/utenti/me "$TOK_S" '{"nome":"Smoke","cognome":"Test","latitudine":999,"longitudine":9.19}')"

echo "EVENTI"
login '{"email":"lorenzo@example.com","password":"PasswordSicura1"}'
verifica "login proprietario" 200 "$RESP_CODE"; TOK_L="$RESP_TOKEN"
EV='{"titolo":"Evento di collaudo","descrizione":"prova","dataEvento":"2027-06-01T20:00:00Z","luogo":"Arena","latitudine":44.5,"longitudine":11.35,"capienza":2,"artisti":["Prova"],"immagini":[{"url":"https://esempio.it/x.jpg","principale":true}],"marker":[{"tipo":"INGRESSO","latitudine":44.5,"longitudine":11.35}]}'
ID=$(curl -s -X POST "$B/api/eventi" -H "Authorization: Bearer $TOK_L" -H 'Content-Type: application/json' --data-binary "$EV" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
verifica "evento creato (id $ID)" 200 "$(chiama GET /api/eventi/$ID)"
verifica "data nel passato rifiutata" 400 "$(chiama POST /api/eventi "$TOK_L" '{"titolo":"Vecchio","dataEvento":"2020-01-01T20:00:00Z","luogo":"X","latitudine":1,"longitudine":1}')"
verifica "titolo mancante" 400 "$(chiama POST /api/eventi "$TOK_L" '{"dataEvento":"2027-01-01T20:00:00Z","luogo":"X","latitudine":1,"longitudine":1}')"
verifica "immagine non https" 400 "$(chiama POST /api/eventi "$TOK_L" '{"titolo":"T","dataEvento":"2027-01-01T20:00:00Z","luogo":"X","latitudine":1,"longitudine":1,"immagini":[{"url":"http://insicuro.it/x.jpg"}]}')"
verifica "cancellazione da non proprietario" 403 "$(chiama DELETE /api/eventi/$ID "$TOK_S")"
verifica "modifica dal proprietario" 200 "$(chiama PUT /api/eventi/$ID "$TOK_L" "$EV")"
verifica "AI non attiva" 403 "$(chiama POST /api/eventi/$ID/descrizione/migliora "$TOK_L" '{"descrizione":"prova"}')"

echo "TICKET"
verifica "il proprietario non prende ticket" 409 "$(chiama POST /api/eventi/$ID/iscrizione "$TOK_L")"
verifica "iscrizione" 201 "$(chiama POST /api/eventi/$ID/iscrizione "$TOK_S")"
verifica "doppia iscrizione" 409 "$(chiama POST /api/eventi/$ID/iscrizione "$TOK_S")"
verifica "partecipanti con ticket" 200 "$(chiama GET /api/eventi/$ID/partecipanti "$TOK_S")"
verifica "elenco propri ticket" 200 "$(chiama GET /api/ticket "$TOK_S")"

echo "AMICIZIE E CHAT"
login '{"email":"marco@example.com","password":"Pass<word>99x"}'
verifica "login altro utente" 200 "$RESP_CODE"; TOK_M="$RESP_TOKEN"
verifica "amicizia senza evento in comune" 403 "$(chiama POST /api/amicizie/1 "$TOK_S")"
verifica "amicizia verso se stessi" 400 "$(chiama POST /api/amicizie/2 "$TOK_M")"
verifica "chat fra amici" 201 "$(chiama POST /api/chat/messaggi "$TOK_M" '{"destinatarioId":3,"contenuto":"messaggio di collaudo"}')"
verifica "chat verso non amico" 403 "$(chiama POST /api/chat/messaggi "$TOK_M" '{"destinatarioId":1,"contenuto":"ciao"}')"
verifica "messaggio vuoto" 400 "$(chiama POST /api/chat/messaggi "$TOK_M" '{"destinatarioId":3,"contenuto":"   "}')"
verifica "conversazione" 200 "$(chiama GET /api/chat/3/messaggi "$TOK_M")"
verifica "messaggi non letti" 200 "$(chiama GET /api/chat/non-letti "$TOK_M")"

echo "NOTIFICHE"
verifica "avviso ai partecipanti" 200 "$(chiama POST /api/eventi/$ID/notifiche "$TOK_L" '{"messaggio":"avviso di collaudo"}')"
verifica "avviso da non proprietario" 403 "$(chiama POST /api/eventi/$ID/notifiche "$TOK_S" '{"messaggio":"non autorizzato"}')"
verifica "notifiche ricevute" 200 "$(chiama GET /api/notifiche "$TOK_S")"
verifica "contatore non lette" 200 "$(chiama GET /api/notifiche/non-lette "$TOK_S")"
verifica "segna tutte lette" 204 "$(chiama POST /api/notifiche/lette "$TOK_S")"

echo "PRIVACY"
verifica "anonimizzazione senza conferma" 400 "$(chiama POST /api/privacy/anonimizzazione "$TOK_S" '{"password":"PasswordSicura7","confermo":false}')"
verifica "anonimizzazione password errata" 400 "$(chiama POST /api/privacy/anonimizzazione "$TOK_S" '{"password":"sbagliata0000","confermo":true}')"
verifica "anonimizzazione confermata" 204 "$(chiama POST /api/privacy/anonimizzazione "$TOK_S" '{"password":"PasswordSicura7","confermo":true}')"
verifica "token non piu' valido dopo anonimizzazione" 401 "$(chiama GET /api/utenti/me "$TOK_S")"
verifica "login dopo anonimizzazione" 400 "$(chiama POST /api/auth/login "" "$ACC")"

echo
echo "RISULTATO: $ok superati, $ko falliti"
[ $ko -eq 0 ]
