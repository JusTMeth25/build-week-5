const BASE = (import.meta.env.VITE_API_URL ?? '').replace(/\/$/, '')
const TOKEN_KEY = 'epicode.eventi.token'

export type Page<T> = { content: T[]; totalElements: number; totalPages: number; number: number; size: number }
export type Stato = { servizio: string; database: string; ora: string }
export type Utente = { id: number; email: string; nome: string; cognome: string; indirizzo?: string; dataNascita?: string; eta?: number; telefono?: string; latitudine?: number; longitudine?: number; ruolo: 'UTENTE' | 'AMMINISTRATORE'; verificato: boolean }
export type UtentePubblico = { id: number; nome: string; cognome: string }
export type LoginResponse = { token: string; utente: Utente }
export type EventoSintesi = { id: number; titolo: string; dataEvento: string; luogo: string; latitudine: number; longitudine: number; immaginePrincipale?: string }
export type Evento = EventoSintesi & { descrizione?: string; indirizzo?: string; capienza?: number; genere?: string; proprietario: UtentePubblico; artisti: { id: number; nome: string; genere?: string }[]; immagini: { id: number; url: string; principale: boolean }[]; marker: { id: number; tipo: string; etichetta?: string; latitudine: number; longitudine: number }[]; creatoIl: string; aggiornatoIl?: string }
export type EventoInput = { titolo: string; descrizione?: string; dataEvento: string; luogo: string; indirizzo?: string; latitudine?: number; longitudine?: number; capienza?: number; genere?: string; artisti?: string[]; immagini?: { url: string; principale: boolean }[]; marker?: { tipo: string; etichetta?: string; latitudine: number; longitudine: number }[] }
export type EventoMappa = EventoSintesi & { distanzaKm?: number }
export type Ticket = { id: number; codice: string; eventoId: number; nomeEvento: string; dataEvento: string; luogoEvento: string; nomePartecipante: string; emessoIl: string }
export type Notifica = { id: number; tipo: string; messaggio: string; eventoId?: number; letta: boolean; creataIl: string }
export type Partecipante = { id: number; nome: string; cognome: string; statoAmicizia?: string; richiestaInviataDaMe: boolean }
export type Amicizia = { id: number; richiedente: UtentePubblico; destinatario: UtentePubblico; stato: string; creataIl: string }
export type Messaggio = { id: number; mittenteId: number; nomeMittente: string; destinatarioId: number; contenuto: string; inviatoIl: string; lettoIl?: string }
export type SuggerimentoIndirizzo = { idLuogo: string; descrizione: string; principale?: string; secondario?: string }
export type IndirizzoGeocodificato = { indirizzo: string; nomeLuogo?: string; latitudine: number; longitudine: number }
export type ApiError = Error & { status?: number }

export const sessione = {
  token: () => localStorage.getItem(TOKEN_KEY),
  salva: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  cancella: () => localStorage.removeItem(TOKEN_KEY),
}

async function chiama<T>(percorso: string, opzioni: RequestInit = {}): Promise<T> {
  const headers = new Headers(opzioni.headers)
  if (opzioni.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json')
  const token = sessione.token()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const risposta = await fetch(`${BASE}${percorso}`, { ...opzioni, headers })
  if (!risposta.ok) {
    const testo = await risposta.text()
    let messaggio = testo || `${risposta.status} ${risposta.statusText}`
    try {
      const json = JSON.parse(testo) as { messaggio?: string; errore?: string }
      messaggio = json.messaggio || json.errore || messaggio
    } catch {
      // testo non JSON
    }
    const errore = new Error(messaggio) as ApiError
    errore.status = risposta.status
    throw errore
  }
  return risposta.status === 204 ? (undefined as T) : ((await risposta.json()) as T)
}

const post = (body?: unknown): RequestInit => ({ method: 'POST', body: body ? JSON.stringify(body) : undefined })

export const api = {
  indirizzo: BASE || '(stessa origine, proxy Vite)',
  stato: () => chiama<Stato>('/api/stato'),
  login: (email: string, password: string) => chiama<LoginResponse>('/api/auth/login', post({ email, password })),
  registrazione: (body: { email: string; password: string; nome: string; cognome: string; indirizzo?: string; dataNascita?: string; telefono?: string }) => chiama<Utente>('/api/auth/registrazione', post(body)),
  verifica: (email: string, codice: string) => chiama<void>('/api/auth/verifica', post({ email, codice })),
  passwordDimenticata: (email: string) => chiama<void>('/api/auth/password-dimenticata', post({ email })),
  reimpostaPassword: (token: string, password: string) => chiama<void>('/api/auth/reimposta-password', post({ token, password })),
  io: () => chiama<Utente>('/api/auth/io'),
  profilo: () => chiama<Utente>('/api/utenti/me'),
  aggiornaProfilo: (body: Partial<Utente>) => chiama<Utente>('/api/utenti/me', { method: 'PUT', body: JSON.stringify(body) }),
  eventi: (testo = '', pagina = 0) => chiama<Page<EventoSintesi>>(`/api/eventi?pagina=${pagina}&dimensione=12&testo=${encodeURIComponent(testo)}`),
  evento: (id: number) => chiama<Evento>(`/api/eventi/${id}`),
  mieiEventi: () => chiama<Page<EventoSintesi>>('/api/eventi/miei?dimensione=50'),
  creaEvento: (body: EventoInput) => chiama<Evento>('/api/eventi', post(body)),
  aggiornaEvento: (id: number, body: EventoInput) => chiama<Evento>(`/api/eventi/${id}`, { method: 'PUT', body: JSON.stringify(body) }),
  eliminaEvento: (id: number) => chiama<void>(`/api/eventi/${id}`, { method: 'DELETE' }),
  miglioraDescrizione: (id: number, descrizione: string) => chiama<{ descrizioneOriginale: string; descrizioneMigliorata: string }>(`/api/eventi/${id}/descrizione/migliora`, post({ descrizione })),
  mappa: (latitudine?: number, longitudine?: number) => {
    const qs = latitudine != null && longitudine != null ? `?latitudine=${latitudine}&longitudine=${longitudine}` : ''
    return chiama<EventoMappa[]>(`/api/mappa/eventi${qs}`)
  },
  suggerimentiIndirizzo: (testo: string, sessione: string) => chiama<SuggerimentoIndirizzo[]>(`/api/geocoding/suggerimenti?testo=${encodeURIComponent(testo)}&sessione=${encodeURIComponent(sessione)}`),
  dettaglioLuogo: (idLuogo: string, sessione: string) => chiama<IndirizzoGeocodificato>(`/api/geocoding/luoghi/${encodeURIComponent(idLuogo)}?sessione=${encodeURIComponent(sessione)}`),
  indirizzoDaCoordinate: (latitudine: number, longitudine: number) => chiama<IndirizzoGeocodificato>(`/api/geocoding/inverso?latitudine=${latitudine}&longitudine=${longitudine}`),
  iscrivi: (eventoId: number) => chiama<Ticket>(`/api/eventi/${eventoId}/iscrizione`, { method: 'POST' }),
  ticket: () => chiama<Ticket[]>('/api/ticket'),
  annullaTicket: (id: number) => chiama<void>(`/api/ticket/${id}`, { method: 'DELETE' }),
  notifiche: async () => (await chiama<Page<Notifica>>('/api/notifiche?dimensione=50')).content,
  leggiNotifica: (id: number) => chiama<void>(`/api/notifiche/${id}/letta`, { method: 'POST' }),
  leggiTutte: () => chiama<void>('/api/notifiche/lette', { method: 'POST' }),
  partecipanti: (eventoId: number) => chiama<Partecipante[]>(`/api/eventi/${eventoId}/partecipanti`),
  amici: () => chiama<Amicizia[]>('/api/amicizie'),
  richiesteAmicizia: () => chiama<Amicizia[]>('/api/amicizie/richieste'),
  richiediAmicizia: (utenteId: number) => chiama<Amicizia>(`/api/amicizie/${utenteId}`, { method: 'POST' }),
  accettaAmicizia: (id: number) => chiama<Amicizia>(`/api/amicizie/${id}/accetta`, { method: 'POST' }),
  chat: (utenteId: number) => chiama<Page<Messaggio>>(`/api/chat/${utenteId}/messaggi?dimensione=50`),
  messaggio: (destinatarioId: number, contenuto: string) => chiama<Messaggio>('/api/chat/messaggi', post({ destinatarioId, contenuto })),
}