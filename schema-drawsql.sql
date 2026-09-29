-- Schema DB piattaforma-eventi (BUILD-WEEK-5)
-- Estratto dallo schema reale generato da Hibernate dalle entita' del backend.
-- Importabile in DrawSQL: Import database > PostgreSQL.

CREATE TABLE utenti (
    id BIGINT PRIMARY KEY,
    nome VARCHAR NOT NULL,
    cognome VARCHAR NOT NULL,
    email VARCHAR NOT NULL UNIQUE,
    password_hash VARCHAR NOT NULL,
    ruolo VARCHAR NOT NULL,
    telefono VARCHAR,
    data_nascita DATE,
    indirizzo VARCHAR,
    latitudine DOUBLE PRECISION,
    longitudine DOUBLE PRECISION,
    attivo BOOLEAN NOT NULL,
    verificato BOOLEAN NOT NULL,
    anonimizzato BOOLEAN NOT NULL,
    tentativi_falliti INTEGER NOT NULL,
    bloccato_fino_il TIMESTAMPTZ,
    creato_il TIMESTAMPTZ NOT NULL
);

CREATE TABLE artisti (
    id BIGINT PRIMARY KEY,
    nome VARCHAR NOT NULL UNIQUE,
    genere VARCHAR
);

CREATE TABLE eventi (
    id BIGINT PRIMARY KEY,
    titolo VARCHAR NOT NULL,
    descrizione TEXT,
    data_evento TIMESTAMPTZ NOT NULL,
    luogo VARCHAR NOT NULL,
    indirizzo VARCHAR,
    latitudine DOUBLE PRECISION NOT NULL,
    longitudine DOUBLE PRECISION NOT NULL,
    capienza INTEGER,
    proprietario_id BIGINT NOT NULL REFERENCES utenti(id),
    creato_il TIMESTAMPTZ NOT NULL,
    aggiornato_il TIMESTAMPTZ
);

CREATE TABLE codici_verifica (
    id BIGINT PRIMARY KEY,
    codice VARCHAR NOT NULL,
    scade_il TIMESTAMPTZ NOT NULL,
    usato BOOLEAN NOT NULL,
    utente_id BIGINT NOT NULL REFERENCES utenti(id)
);

CREATE TABLE eventi_artisti (
    evento_id BIGINT NOT NULL REFERENCES eventi(id),
    artista_id BIGINT NOT NULL REFERENCES artisti(id),
    PRIMARY KEY (evento_id, artista_id)
);

CREATE TABLE immagini_evento (
    id BIGINT PRIMARY KEY,
    url VARCHAR NOT NULL,
    principale BOOLEAN NOT NULL,
    evento_id BIGINT NOT NULL REFERENCES eventi(id)
);

CREATE TABLE marker_evento (
    id BIGINT PRIMARY KEY,
    tipo VARCHAR NOT NULL,
    etichetta VARCHAR,
    latitudine DOUBLE PRECISION NOT NULL,
    longitudine DOUBLE PRECISION NOT NULL,
    evento_id BIGINT NOT NULL REFERENCES eventi(id)
);

CREATE TABLE notifiche (
    id BIGINT PRIMARY KEY,
    tipo VARCHAR NOT NULL,
    messaggio VARCHAR NOT NULL,
    letta BOOLEAN NOT NULL,
    destinatario_id BIGINT NOT NULL REFERENCES utenti(id),
    evento_id BIGINT REFERENCES eventi(id),
    creata_il TIMESTAMPTZ NOT NULL
);

CREATE TABLE ticket (
    id BIGINT PRIMARY KEY,
    codice VARCHAR NOT NULL UNIQUE,
    nome_evento VARCHAR NOT NULL,
    luogo_evento VARCHAR NOT NULL,
    data_evento TIMESTAMPTZ NOT NULL,
    nome_partecipante VARCHAR NOT NULL,
    annullato BOOLEAN NOT NULL,
    emesso_il TIMESTAMPTZ NOT NULL,
    evento_id BIGINT NOT NULL REFERENCES eventi(id),
    partecipante_id BIGINT NOT NULL REFERENCES utenti(id),
    CONSTRAINT uq_ticket_evento_partecipante UNIQUE (evento_id, partecipante_id)
);

CREATE TABLE amicizie (
    id BIGINT PRIMARY KEY,
    stato VARCHAR NOT NULL,
    richiedente_id BIGINT NOT NULL REFERENCES utenti(id),
    destinatario_id BIGINT NOT NULL REFERENCES utenti(id),
    creata_il TIMESTAMPTZ NOT NULL,
    aggiornata_il TIMESTAMPTZ,
    CONSTRAINT uq_amicizia_coppia UNIQUE (richiedente_id, destinatario_id)
);

CREATE TABLE messaggi (
    id BIGINT PRIMARY KEY,
    contenuto VARCHAR NOT NULL,
    mittente_id BIGINT NOT NULL REFERENCES utenti(id),
    destinatario_id BIGINT NOT NULL REFERENCES utenti(id),
    inviato_il TIMESTAMPTZ NOT NULL,
    letto_il TIMESTAMPTZ
);
