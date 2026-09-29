#!/usr/bin/env bash
# Collaudo degli endpoint del backend: confronta lo stato HTTP atteso con quello
# ottenuto su tutto il percorso funzionale, dal pubblico all'anonimizzazione.
#
#   ./collaudo/collaudo-backend.sh [file-di-log-del-backend]
#
# Il file di log serve a leggere il codice di verifica quando il mailing e'
# disattivato (MAIL_ABILITATO=false), cioe' nella configurazione predefinita.
set -u
LOG="${1:-}"; ok=0; ko=0
B="${BASE_URL:-http://localhost:8080}"
S=$(mktemp -d)
trap 'rm -rf "$S"' EXIT
verifica() { local nome="$1" attesa="$2" reale="$3"
  if [ "$attesa" = "$reale" ]; then ok=$((ok+1)); printf "  ok      %-50s %s\n" "$nome" "$reale"
  else ko=$((ko+1)); printf "  FALLITO %-50s atteso %s, ottenuto %s\n" "$nome" "$attesa" "$reale"; fi; }
tok() { awk '$6=="XSRF-TOKEN"{print $7}' "$1"; }
chiama() { local metodo="$1" percorso="$2" jar="${3:-}" corpo="${4:-}"
  if [ -z "$jar" ]; then curl -s -o /dev/null -w '%{http_code}' -X "$metodo" "$B$percorso"; return; fi
  if [ -z "$corpo" ]; then
    curl -s -o /dev/null -w '%{http_code}' -X "$metodo" "$B$percorso" -b "$jar" -c "$jar" -H "X-XSRF-TOKEN: $(tok "$jar")"
  else
    curl -s -o /dev/null -w '%{http_code}' -X "$metodo" "$B$percorso" -b "$jar" -c "$jar" \
      -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $(tok "$jar")" --data-binary "$corpo"
  fi; }
apri() { local jar="$1"; rm -f "$jar"; curl -s -o /dev/null -c "$jar" "$B/api/auth/csrf"; }
entra() { local jar="$1" corpo="$2"; apri "$jar"; chiama POST /api/auth/login "$jar" "$corpo"; }
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
E="smoke$RANDOM@example.com"; J="$S/smoke.txt"; apri "$J"
REG=$(printf '{"email":"%s","password":"PasswordSicura7","nome":"Smoke","cognome":"Test","indirizzo":"Via Prova 1","dataNascita":"1994-05-05","telefono":"3401112223"}' "$E")
verifica "registrazione" 201 "$(chiama POST /api/auth/registrazione "$J" "$REG")"
verifica "password troppo debole" 400 "$(chiama POST /api/auth/registrazione "$J" '{"email":"debole@example.com","password":"corta","nome":"A","cognome":"B"}')"
verifica "email non valida" 400 "$(chiama POST /api/auth/registrazione "$J" '{"email":"non-una-email","password":"PasswordSicura7","nome":"A","cognome":"B"}')"
verifica "email duplicata" 409 "$(chiama POST /api/auth/registrazione "$J" "$REG")"
ACC=$(printf '{"email":"%s","password":"PasswordSicura7"}' "$E")
verifica "login non verificato" 403 "$(chiama POST /api/auth/login "$J" "$ACC")"
VER_KO=$(printf '{"email":"%s","codice":"000000"}' "$E")
verifica "codice errato" 400 "$(chiama POST /api/auth/verifica "$J" "$VER_KO")"
VER_OK=$(printf '{"email":"%s","codice":"%s"}' "$E" "$(codice "$E")")
verifica "verifica con codice dal log" 204 "$(chiama POST /api/auth/verifica "$J" "$VER_OK")"
verifica "login verificato" 200 "$(entra "$J" "$ACC")"
verifica "dati della sessione" 200 "$(chiama GET /api/auth/io "$J")"
verifica "senza token CSRF" 403 "$(curl -s -o /dev/null -w '%{http_code}' -X PUT "$B/api/utenti/me" -b "$J" -H 'Content-Type: application/json' --data-binary '{"nome":"X","cognome":"Y"}')"
verifica "aggiornamento profilo" 200 "$(chiama PUT /api/utenti/me "$J" '{"nome":"Smoke","cognome":"Test","latitudine":45.46,"longitudine":9.19}')"
verifica "coordinate fuori scala" 400 "$(chiama PUT /api/utenti/me "$J" '{"nome":"Smoke","cognome":"Test","latitudine":999,"longitudine":9.19}')"

echo "EVENTI"
JL="$S/lorenzo.txt"; verifica "login proprietario" 200 "$(entra "$JL" '{"email":"lorenzo@example.com","password":"PasswordSicura1"}')"
EV='{"titolo":"Evento di collaudo","descrizione":"prova","dataEvento":"2027-06-01T20:00:00Z","luogo":"Arena","latitudine":44.5,"longitudine":11.35,"capienza":2,"artisti":["Prova"],"immagini":[{"url":"https://esempio.it/x.jpg","principale":true}],"marker":[{"tipo":"INGRESSO","latitudine":44.5,"longitudine":11.35}]}'
ID=$(curl -s -X POST "$B/api/eventi" -b "$JL" -c "$JL" -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $(tok "$JL")" --data-binary "$EV" | python3 -c 'import sys,json; print(json.load(sys.stdin)["id"])')
verifica "evento creato (id $ID)" 200 "$(chiama GET /api/eventi/$ID)"
verifica "data nel passato rifiutata" 400 "$(chiama POST /api/eventi "$JL" '{"titolo":"Vecchio","dataEvento":"2020-01-01T20:00:00Z","luogo":"X","latitudine":1,"longitudine":1}')"
verifica "titolo mancante" 400 "$(chiama POST /api/eventi "$JL" '{"dataEvento":"2027-01-01T20:00:00Z","luogo":"X","latitudine":1,"longitudine":1}')"
verifica "immagine non https" 400 "$(chiama POST /api/eventi "$JL" '{"titolo":"T","dataEvento":"2027-01-01T20:00:00Z","luogo":"X","latitudine":1,"longitudine":1,"immagini":[{"url":"http://insicuro.it/x.jpg"}]}')"
verifica "cancellazione da non proprietario" 403 "$(chiama DELETE /api/eventi/$ID "$J")"
verifica "modifica dal proprietario" 200 "$(chiama PUT /api/eventi/$ID "$JL" "$EV")"
verifica "AI non attiva" 403 "$(chiama POST /api/eventi/$ID/descrizione/migliora "$JL" '{"descrizione":"prova"}')"

echo "TICKET"
verifica "il proprietario non prende ticket" 409 "$(chiama POST /api/eventi/$ID/iscrizione "$JL")"
verifica "iscrizione" 201 "$(chiama POST /api/eventi/$ID/iscrizione "$J")"
verifica "doppia iscrizione" 409 "$(chiama POST /api/eventi/$ID/iscrizione "$J")"
verifica "partecipanti con ticket" 200 "$(chiama GET /api/eventi/$ID/partecipanti "$J")"
verifica "elenco propri ticket" 200 "$(chiama GET /api/ticket "$J")"

echo "AMICIZIE E CHAT"
JM="$S/marcos.txt"; verifica "login altro utente" 200 "$(entra "$JM" '{"email":"marco@example.com","password":"Pass<word>99x"}')"
verifica "amicizia senza evento in comune" 403 "$(chiama POST /api/amicizie/1 "$J")"
verifica "amicizia verso se stessi" 400 "$(chiama POST /api/amicizie/2 "$JM")"
verifica "chat fra amici" 201 "$(chiama POST /api/chat/messaggi "$JM" '{"destinatarioId":3,"contenuto":"messaggio di collaudo"}')"
verifica "chat verso non amico" 403 "$(chiama POST /api/chat/messaggi "$JM" '{"destinatarioId":1,"contenuto":"ciao"}')"
verifica "messaggio vuoto" 400 "$(chiama POST /api/chat/messaggi "$JM" '{"destinatarioId":3,"contenuto":"   "}')"
verifica "conversazione" 200 "$(chiama GET /api/chat/3/messaggi "$JM")"
verifica "messaggi non letti" 200 "$(chiama GET /api/chat/non-letti "$JM")"

echo "NOTIFICHE"
verifica "avviso ai partecipanti" 200 "$(chiama POST /api/eventi/$ID/notifiche "$JL" '{"messaggio":"avviso di collaudo"}')"
verifica "avviso da non proprietario" 403 "$(chiama POST /api/eventi/$ID/notifiche "$J" '{"messaggio":"non autorizzato"}')"
verifica "notifiche ricevute" 200 "$(chiama GET /api/notifiche "$J")"
verifica "contatore non lette" 200 "$(chiama GET /api/notifiche/non-lette "$J")"
verifica "segna tutte lette" 204 "$(chiama POST /api/notifiche/lette "$J")"

echo "PRIVACY"
verifica "anonimizzazione senza conferma" 400 "$(chiama POST /api/privacy/anonimizzazione "$J" '{"password":"PasswordSicura7","confermo":false}')"
verifica "anonimizzazione password errata" 400 "$(chiama POST /api/privacy/anonimizzazione "$J" '{"password":"sbagliata0000","confermo":true}')"
verifica "anonimizzazione confermata" 204 "$(chiama POST /api/privacy/anonimizzazione "$J" '{"password":"PasswordSicura7","confermo":true}')"
verifica "sessione invalidata" 401 "$(chiama GET /api/utenti/me "$J")"
verifica "login dopo anonimizzazione" 400 "$(chiama POST /api/auth/login "$J" "$ACC")"
verifica "logout" 204 "$(chiama POST /api/auth/logout "$JL")"
verifica "dopo il logout" 401 "$(chiama GET /api/auth/io "$JL")"

echo
echo "RISULTATO: $ok superati, $ko falliti"
[ $ko -eq 0 ]
