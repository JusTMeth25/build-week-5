#!/usr/bin/env bash
# Avvio locale per macOS/Linux (equivalente di avvia.cmd).
# BE in background, FE in primo piano: Ctrl+C chiude entrambi.
cd "$(dirname "$0")" || exit 1

# ---------- PostgreSQL: serve il database BUILD-WEEK-5 sulla 5432 ----------
# DB_URL del progetto, imposta qui perche' una variabile d'ambiente rimasta da
# un altro progetto avrebbe la precedenza su application.yml e il backend si
# collegherebbe al database sbagliato. Utente e password restano quelli
# dell'ambiente (DB_USERNAME / DB_PASSWORD), o i valori di application.yml.
export DB_URL="jdbc:postgresql://localhost:5432/BUILD-WEEK-5"

if nc -z localhost 5432 >/dev/null 2>&1; then
  echo "[postgres] in ascolto sulla 5432."
else
  echo "[postgres] porta 5432 chiusa: il backend non partira'."
  echo "           createdb -U postgres BUILD-WEEK-5"
fi

# Dipendenze FE solo al primo avvio
if [ ! -d fe/node_modules ]; then
  echo "[FE] npm install..."
  (cd fe && npm install) || exit 1
fi

# Backend in background; all'uscita si chiude tutto il gruppo di processi
# (anche la JVM che spring-boot:run avvia come processo figlio)
trap 'kill 0 2>/dev/null' EXIT
(cd be && ./mvnw spring-boot:run) &

echo
echo " Applicazione : http://localhost:5173"
echo " Stato        : http://localhost:8080/api/stato"
echo " Salute       : http://localhost:8080/actuator/health"
echo

# Frontend in primo piano
cd fe && npm run dev
