import { AnimatePresence, motion } from 'framer-motion'
import {
  Bell,
  Bot,
  CalendarDays,
  Check,
  Compass,
  Globe2,
  HeartHandshake,
  LayoutDashboard,
  LogOut,
  MapPin,
  MessageCircle,
  Plus,
  QrCode,
  Search,
  Send,
  ShieldCheck,
  Sparkles,
  Ticket,
  User,
  Users,
  Wand2,
  Zap,
} from 'lucide-react'
import { useCallback, useEffect, useState, type FormEvent } from 'react'
import { Link, NavLink, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import { EventSphere } from '@/components/EventSphere'
import { IndirizzoAutocomplete } from '@/components/IndirizzoAutocomplete'
import MapDashboard from '@/components/mappa/MapDashboard'
import { Area, Badge, Button, Card, Field, Reveal } from '@/components/ui'
import { useAuth } from '@/context/AuthContext'
import { api, type Amicizia, type EventoInput, type EventoMappa, type EventoSintesi, type IndirizzoGeocodificato, type Messaggio, type Notifica, type Stato, type Ticket as TicketType } from '@/lib/api'
import { cn, dataBella, giorno, iniziali } from '@/lib/utils'

const fallbackImage = 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=1400&auto=format&fit=crop'

function useAsync<T>(loader: () => Promise<T>, deps: unknown[] = []) {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const reload = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setData(await loader())
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setLoading(false)
    }
  }, deps)
  useEffect(() => {
    void reload()
  }, [reload])
  return { data, loading, error, reload }
}

function Shell({ children }: { children: React.ReactNode }) {
  const { utente, logout } = useAuth()
  const nav = [
    ['/', 'Home', Sparkles],
    ['/eventi', 'Eventi', Compass],
    ['/mappa', 'Mappa', Globe2],
    ['/dashboard', 'Cockpit', LayoutDashboard],
    ['/ticket', 'Ticket', Ticket],
    ['/rete', 'Rete', Users],
    ['/notifiche', 'Notifiche', Bell],
  ] as const
  return (
    <div className="noise min-h-screen">
      <header className="sticky top-0 z-40 border-b border-white/10 bg-slate-950/60 backdrop-blur-2xl">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3">
          <Link to="/" className="flex items-center gap-3 font-black tracking-tight">
            <span className="grid size-11 place-items-center rounded-2xl aurora shadow-lg shadow-cyan-500/20"><Zap className="size-5" /></span>
            <span className="hidden sm:block">EventVerse</span>
          </Link>
          <nav className="hide-scrollbar flex max-w-[58vw] gap-1 overflow-x-auto rounded-2xl border border-white/10 bg-white/5 p-1">
            {nav.map(([to, label, Icon]) => (
              <NavLink key={to} to={to} className={({ isActive }) => cn('flex items-center gap-2 rounded-xl px-3 py-2 text-xs font-bold text-slate-300 transition hover:bg-white/10', isActive && 'bg-white text-slate-950')}>
                <Icon className="size-4" /> <span className="hidden md:inline">{label}</span>
              </NavLink>
            ))}
          </nav>
          {utente ? (
            <div className="flex items-center gap-2">
              <Link to="/profilo" className="hidden items-center gap-2 rounded-2xl border border-white/10 bg-white/5 px-3 py-2 text-sm font-bold md:flex">
                <span className="grid size-8 place-items-center rounded-xl bg-cyan-300 text-slate-950">{iniziali(utente.nome, utente.cognome)}</span>
                {utente.nome}
              </Link>
              <Button variant="ghost" onClick={logout} title="Esci"><LogOut className="size-4" /></Button>
            </div>
          ) : (
            <Link to="/accesso"><Button>Accedi</Button></Link>
          )}
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-8">{children}</main>
    </div>
  )
}

function Protected({ children }: { children: React.ReactNode }) {
  const { utente, caricamento } = useAuth()
  if (caricamento) return <Shell><Card>Caricamento sessione...</Card></Shell>
  if (!utente) return (
    <Shell>
      <Card className="mx-auto max-w-3xl text-center">
        <Badge className="mb-4 border-cyan-300/30 bg-cyan-300/10 text-cyan-100"><ShieldCheck className="size-3" /> Area riservata</Badge>
        <h1 className="text-4xl font-black md:text-6xl">Accedi per continuare</h1>
        <p className="mx-auto mt-4 max-w-xl text-slate-300">Puoi esplorare eventi, dettagli e mappa anche senza account. Per ticket, notifiche, chat e cockpit serve invece la registrazione.</p>
        <div className="mt-7 flex flex-wrap justify-center gap-3">
          <Link to="/accesso"><Button><User className="size-4" /> Accedi o registrati</Button></Link>
          <Link to="/eventi"><Button variant="glass"><Compass className="size-4" /> Continua a esplorare</Button></Link>
        </div>
      </Card>
    </Shell>
  )
  return children
}

function Home() {
  const stato = useAsync<Stato>(() => api.stato(), [])
  const eventi = useAsync(() => api.eventi('', 0), [])
  const highlights = eventi.data?.content.slice(0, 3) ?? []
  return (
    <Shell>
      <section className="relative min-h-[78vh] overflow-hidden rounded-[3rem] border border-white/10 bg-white/[0.04] p-6 shadow-2xl shadow-black/30 md:p-10">
        <EventSphere />
        <div className="relative z-10 grid items-center gap-10 lg:grid-cols-[1.05fr_.95fr]">
          <Reveal className="max-w-3xl">
            <Badge className="mb-6 border-cyan-300/30 bg-cyan-300/10 text-cyan-100"><Sparkles className="size-3" /> gestione eventi next-gen</Badge>
            <h1 className="text-5xl font-black leading-[.95] tracking-tight md:text-7xl">
              Organizza eventi che sembrano arrivare dal futuro.
            </h1>
            <p className="mt-6 max-w-2xl text-lg leading-8 text-slate-300">
              EventVerse unisce ticket, notifiche live, geocoding, social discovery, chat e cockpit organizzatore in un’unica esperienza cinematografica.
            </p>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link to="/eventi"><Button><Compass className="size-4" /> Esplora eventi</Button></Link>
              <Link to="/crea"><Button variant="glass"><Plus className="size-4" /> Crea il tuo evento</Button></Link>
            </div>
            <div className="mt-8 grid max-w-xl grid-cols-3 gap-3">
              {[['API', stato.data?.servizio ?? '...'], ['DB', stato.data?.database ?? '...'], ['FE', 'React 19']].map(([a, b]) => <Card key={a} className="p-4"><p className="text-xs text-slate-400">{a}</p><p className="truncate font-black">{b}</p></Card>)}
            </div>
          </Reveal>
          <Reveal delay={0.15} className="grid gap-4">
            {highlights.map((evento, i) => <EventCard key={evento.id} evento={evento} featured={i === 0} />)}
            {!highlights.length && <Card className="p-8 text-slate-300">Nessun evento ancora: diventa il primo organizzatore.</Card>}
          </Reveal>
        </div>
      </section>
      <section className="mt-10 grid gap-4 md:grid-cols-4">
        {[
          [Bot, 'AI copywriter', 'Migliora descrizioni evento con un click.'],
          [MapPin, 'Geocoding', 'Crea eventi partendo da luogo e indirizzo.'],
          [HeartHandshake, 'Community', 'Amicizie basate sugli eventi condivisi.'],
          [ShieldCheck, 'JWT sicuro', 'Flusso autenticato coerente col backend.'],
        ].map(([Icon, title, text]) => <Reveal key={String(title)}><Card className="h-full"><Icon className="mb-5 size-8 text-cyan-200" /><h3 className="font-black">{String(title)}</h3><p className="mt-2 text-sm text-slate-400">{String(text)}</p></Card></Reveal>)}
      </section>
    </Shell>
  )
}

function EventCard({ evento, featured = false }: { evento: EventoSintesi | EventoMappa; featured?: boolean }) {
  return (
    <Link to={`/eventi/${evento.id}`}>
      <motion.article whileHover={{ y: -6, scale: 1.01 }} className={cn('group overflow-hidden rounded-[2rem] border border-white/10 bg-white/[0.06] shadow-xl shadow-black/20', featured && 'md:grid md:grid-cols-[1fr_1.2fr]')}>
        <div className="relative min-h-48 overflow-hidden">
          <img src={evento.immaginePrincipale || fallbackImage} alt="" className="absolute inset-0 h-full w-full object-cover transition duration-700 group-hover:scale-110" />
          <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/20 to-transparent" />
          <span className="absolute left-4 top-4 rounded-2xl bg-white px-3 py-2 text-center text-sm font-black text-slate-950">{giorno(evento.dataEvento)}</span>
        </div>
        <div className="p-5">
          <div className="flex items-center gap-2 text-xs font-bold text-cyan-200"><MapPin className="size-4" /> {evento.luogo}</div>
          <h3 className="mt-3 text-2xl font-black leading-tight">{evento.titolo}</h3>
          <p className="mt-3 text-sm text-slate-400">{dataBella(evento.dataEvento)}</p>
          {'distanzaKm' in evento && evento.distanzaKm != null && <Badge className="mt-4">{evento.distanzaKm} km da te</Badge>}
        </div>
      </motion.article>
    </Link>
  )
}

function Accesso() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [mode, setMode] = useState<'login' | 'register' | 'verify'>('login')
  const [form, setForm] = useState({ email: '', password: '', nome: '', cognome: '', codice: '' })
  const [errore, setErrore] = useState<string | null>(null)
  const [ok, setOk] = useState<string | null>(null)
  async function submit(e: FormEvent) {
    e.preventDefault(); setErrore(null); setOk(null)
    try {
      if (mode === 'login') { await login(form.email, form.password); navigate('/dashboard') }
      if (mode === 'register') { await api.registrazione(form); setMode('verify'); setOk('Account creato: inserisci il codice ricevuto/loggato dal backend.') }
      if (mode === 'verify') { await api.verifica(form.email, form.codice); setMode('login'); setOk('Account verificato. Ora puoi accedere.') }
    } catch (e) { setErrore(e instanceof Error ? e.message : String(e)) }
  }
  return (
    <Shell>
      <div className="mx-auto grid max-w-5xl gap-6 lg:grid-cols-[1fr_.9fr]">
        <Card className="p-8"><h1 className="text-5xl font-black">Entra nel backstage.</h1><p className="mt-4 text-slate-300">Un solo accesso per organizzare, prenotare, chattare e ricevere notifiche.</p><div className="mt-8 grid gap-3">{['JWT Bearer nativo', 'Dashboard organizzatore', 'Ticket digitali', 'Chat tra partecipanti'].map(x => <Badge key={x}><Check className="size-3" /> {x}</Badge>)}</div></Card>
        <Card className="p-6">
          <div className="mb-6 flex gap-2">{(['login', 'register', 'verify'] as const).map(x => <Button key={x} variant={mode === x ? 'primary' : 'ghost'} onClick={() => setMode(x)}>{x === 'login' ? 'Login' : x === 'register' ? 'Registrati' : 'Verifica'}</Button>)}</div>
          <form onSubmit={submit} className="grid gap-3">
            <Field placeholder="email" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required />
            {mode !== 'verify' && <Field placeholder="password" type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} required />}
            {mode === 'register' && <><Field placeholder="nome" value={form.nome} onChange={e => setForm({ ...form, nome: e.target.value })} required /><Field placeholder="cognome" value={form.cognome} onChange={e => setForm({ ...form, cognome: e.target.value })} required /></>}
            {mode === 'verify' && <Field placeholder="codice a 6 cifre" value={form.codice} onChange={e => setForm({ ...form, codice: e.target.value })} required />}
            {errore && <p className="rounded-2xl bg-rose-500/15 p-3 text-sm text-rose-100">{errore}</p>}
            {ok && <p className="rounded-2xl bg-emerald-500/15 p-3 text-sm text-emerald-100">{ok}</p>}
            <Button>{mode === 'login' ? 'Accedi' : mode === 'register' ? 'Crea account' : 'Verifica account'}</Button>
          </form>
        </Card>
      </div>
    </Shell>
  )
}

function Eventi() {
  const [query, setQuery] = useState('')
  const eventi = useAsync(() => api.eventi(query, 0), [query])
  return <Shell><PageTitle icon={Compass} title="Esplora" subtitle="Scopri esperienze, concerti e community intorno a te." action={<Link to="/crea"><Button><Plus className="size-4" /> Nuovo evento</Button></Link>} /><div className="mb-6 flex gap-3"><Field placeholder="Cerca per titolo o luogo" value={query} onChange={e => setQuery(e.target.value)} /><Button variant="glass"><Search className="size-4" /></Button></div><StatusBox {...eventi} /> <div className="grid gap-5 md:grid-cols-2 lg:grid-cols-3">{eventi.data?.content.map(e => <EventCard key={e.id} evento={e} />)}</div></Shell>
}

function PageTitle({ icon: Icon, title, subtitle, action }: { icon: typeof Sparkles; title: string; subtitle: string; action?: React.ReactNode }) {
  return <div className="mb-8 flex flex-col justify-between gap-4 md:flex-row md:items-end"><div><Badge className="mb-3"><Icon className="size-3" /> EventVerse</Badge><h1 className="text-4xl font-black md:text-6xl">{title}</h1><p className="mt-3 max-w-2xl text-slate-400">{subtitle}</p></div>{action}</div>
}

function StatusBox<T>({ loading, error }: { loading: boolean; error: string | null; data: T | null; reload: () => Promise<void> }) {
  if (loading) return <Card className="mb-6 animate-pulse">Caricamento dati...</Card>
  if (error) return <Card className="mb-6 border-rose-300/30 bg-rose-500/10 text-rose-100">{error}</Card>
  return null
}

function DettaglioEvento() {
  const { id } = useParams(); const eventId = Number(id)
  const { utente } = useAuth(); const evento = useAsync(() => api.evento(eventId), [eventId])
  const partecipanti = useAsync(() => utente ? api.partecipanti(eventId) : Promise.resolve([]), [eventId, utente?.id])
  const [msg, setMsg] = useState<string | null>(null)
  async function iscrivi() { if (!utente) { setMsg('Per prendere un ticket devi accedere o registrarti.'); return } try { await api.iscrivi(eventId); setMsg('Ticket generato! Lo trovi nella tua area ticket.') } catch (e) { setMsg(e instanceof Error ? e.message : String(e)) } }
  async function migliora() { if (!utente) { setMsg('Il copywriter AI e riservato agli utenti autenticati.'); return } if (!evento.data) return; try { const r = await api.miglioraDescrizione(eventId, evento.data.descrizione || ''); setMsg(r.descrizioneMigliorata) } catch (e) { setMsg(e instanceof Error ? e.message : String(e)) } }
  const e = evento.data
  return <Shell><StatusBox {...evento} />{e && <div className="grid gap-6 lg:grid-cols-[1.1fr_.9fr]"><Card className="overflow-hidden p-0"><img className="h-80 w-full object-cover" src={e.immaginePrincipale || e.immagini[0]?.url || fallbackImage} /><div className="p-6"><Badge><CalendarDays className="size-3" /> {dataBella(e.dataEvento)}</Badge><h1 className="mt-4 text-5xl font-black">{e.titolo}</h1><p className="mt-4 text-slate-300">{e.descrizione || 'Descrizione in arrivo.'}</p><div className="mt-6 flex flex-wrap gap-2">{e.artisti.map(a => <Badge key={a.id}>{a.nome}</Badge>)}</div></div></Card><div className="grid gap-4"><Card><h3 className="text-2xl font-black">Azioni rapide</h3><p className="mt-2 text-slate-400"><MapPin className="inline size-4" /> {e.indirizzo || e.luogo}</p><div className="mt-5 grid gap-3"><Button onClick={iscrivi}><Ticket className="size-4" /> {utente ? 'Prenota ticket' : 'Accedi per prenotare'}</Button><Button variant="glass" onClick={migliora}><Wand2 className="size-4" /> Migliora descrizione AI</Button></div>{!utente && <p className="mt-4 rounded-2xl border border-cyan-300/20 bg-cyan-300/10 p-3 text-sm text-cyan-50">Puoi guardare tutti i dettagli senza login. Registrati solo quando vuoi prendere il ticket.</p>}{msg && <p className="mt-4 rounded-2xl bg-white/10 p-3 text-sm text-cyan-50">{msg}</p>}</Card><Card><h3 className="mb-4 font-black">Partecipanti</h3>{utente ? <div className="grid gap-2">{partecipanti.data?.slice(0, 8).map(p => <div key={p.id} className="flex items-center justify-between rounded-2xl bg-white/5 p-3"><span>{p.nome} {p.cognome}</span><Button variant="ghost" onClick={() => api.richiediAmicizia(p.id)}>Connetti</Button></div>)}</div> : <p className="text-sm text-slate-400">Accedi per vedere i partecipanti e connetterti con la community.</p>}</Card></div></div>}</Shell>
}

function CreaEvento() {
  const navigate = useNavigate(); const [errore, setErrore] = useState<string | null>(null)
  const [form, setForm] = useState({ titolo: '', descrizione: '', dataEvento: '', luogo: '', indirizzo: '', latitudine: '', longitudine: '', immagine: '', artisti: '', capienza: '' })
  // Coordinate scritte a mano -> indirizzo ricavato con il geocoding inverso (dopo una pausa di battitura).
  const [coordinateManuali, setCoordinateManuali] = useState(false); const [geoInfo, setGeoInfo] = useState<string | null>(null)
  useEffect(() => {
    if (!coordinateManuali) return
    const lat = Number(form.latitudine), lng = Number(form.longitudine)
    if (form.latitudine === '' || form.longitudine === '' || !Number.isFinite(lat) || !Number.isFinite(lng) || Math.abs(lat) > 90 || Math.abs(lng) > 180) return
    let annullato = false
    const timer = setTimeout(async () => { setGeoInfo("Cerco l'indirizzo per queste coordinate..."); try { const r = await api.indirizzoDaCoordinate(lat, lng); if (!annullato) { setForm(f => ({ ...f, indirizzo: r.indirizzo })); setGeoInfo(null) } } catch { if (!annullato) setGeoInfo('Nessun indirizzo trovato per queste coordinate.') } }, 700)
    return () => { annullato = true; clearTimeout(timer) }
  }, [form.latitudine, form.longitudine, coordinateManuali])
  const scegliLuogo = (r: IndirizzoGeocodificato) => { setCoordinateManuali(false); setGeoInfo(null); setForm(f => ({ ...f, indirizzo: r.indirizzo, latitudine: r.latitudine.toFixed(6), longitudine: r.longitudine.toFixed(6), luogo: f.luogo || r.nomeLuogo || '' })) }
  // Un indirizzo riscritto a mano invalida le coordinate: le ricalcola il backend al salvataggio.
  const scriviIndirizzo = (indirizzo: string) => { setCoordinateManuali(false); setForm(f => ({ ...f, indirizzo, latitudine: '', longitudine: '' })) }
  const scriviCoordinata = (campo: 'latitudine' | 'longitudine', valore: string) => { setCoordinateManuali(true); setForm(f => ({ ...f, [campo]: valore })) }
  async function submit(e: FormEvent) { e.preventDefault(); setErrore(null); const latitudine = form.latitudine ? Number(form.latitudine) : undefined; const longitudine = form.longitudine ? Number(form.longitudine) : undefined; const body: EventoInput = { titolo: form.titolo, descrizione: form.descrizione, dataEvento: new Date(form.dataEvento).toISOString(), luogo: form.luogo, indirizzo: form.indirizzo, latitudine, longitudine, capienza: form.capienza ? Number(form.capienza) : undefined, artisti: form.artisti.split(',').map(x => x.trim()).filter(Boolean), immagini: form.immagine ? [{ url: form.immagine, principale: true }] : [] }; try { const created = await api.creaEvento(body); navigate(`/eventi/${created.id}`) } catch (e) { setErrore(e instanceof Error ? e.message : String(e)) } }
  return <Protected><Shell><PageTitle icon={Plus} title="Event studio" subtitle="Crea un evento con geocoding backend: coordinate opzionali, esperienza obbligatoria." /><Card><form onSubmit={submit} className="grid gap-4 lg:grid-cols-2"><Field placeholder="Titolo" value={form.titolo} onChange={e => setForm({ ...form, titolo: e.target.value })} required /><Field type="datetime-local" value={form.dataEvento} onChange={e => setForm({ ...form, dataEvento: e.target.value })} required /><Field placeholder="Luogo" value={form.luogo} onChange={e => setForm({ ...form, luogo: e.target.value })} required /><IndirizzoAutocomplete value={form.indirizzo} onChange={scriviIndirizzo} onSeleziona={scegliLuogo} /><Field placeholder="Latitudine es. 45.4408" type="number" step="any" min={-90} max={90} value={form.latitudine} onChange={e => scriviCoordinata('latitudine', e.target.value)} /><Field placeholder="Longitudine es. 9.2612" type="number" step="any" min={-180} max={180} value={form.longitudine} onChange={e => scriviCoordinata('longitudine', e.target.value)} />{geoInfo && <p className="lg:col-span-2 text-sm text-cyan-100">{geoInfo}</p>}<Field placeholder="Immagine https" value={form.immagine} onChange={e => setForm({ ...form, immagine: e.target.value })} /><Field placeholder="Capienza" type="number" value={form.capienza} onChange={e => setForm({ ...form, capienza: e.target.value })} /><Field className="lg:col-span-2" placeholder="Artisti separati da virgola" value={form.artisti} onChange={e => setForm({ ...form, artisti: e.target.value })} /><Area className="lg:col-span-2" placeholder="Descrizione" value={form.descrizione} onChange={e => setForm({ ...form, descrizione: e.target.value })} />{errore && <p className="lg:col-span-2 rounded-2xl bg-rose-500/15 p-3 text-rose-100">{errore}</p>}<p className="lg:col-span-2 text-sm text-slate-400">Scegli un indirizzo dai suggerimenti per compilare le coordinate, oppure inserisci latitudine e longitudine per ricavare l'indirizzo.</p><Button className="lg:col-span-2"><Sparkles className="size-4" /> Pubblica esperienza</Button></form></Card></Shell></Protected>
}

function Dashboard() {
  const miei = useAsync(() => api.mieiEventi(), [])
  return <Protected><Shell><PageTitle icon={LayoutDashboard} title="Cockpit" subtitle="Il centro di controllo dei tuoi eventi." action={<Link to="/crea"><Button><Plus className="size-4" /> Crea</Button></Link>} /><div className="grid gap-4 md:grid-cols-3"><Metric label="Eventi gestiti" value={miei.data?.totalElements ?? 0} /><Metric label="Prossimo lancio" value={miei.data?.content[0] ? giorno(miei.data.content[0].dataEvento) : '--'} /><Metric label="Stack" value="Live" /></div><div className="mt-6 grid gap-4 md:grid-cols-2">{miei.data?.content.map(e => <EventCard key={e.id} evento={e} />)}</div></Shell></Protected>
}
function Metric({ label, value }: { label: string; value: string | number }) { return <Card><p className="text-sm text-slate-400">{label}</p><p className="mt-2 text-4xl font-black">{value}</p></Card> }

function Mappa() {
  return <Shell><MapDashboard /></Shell>
}
function TicketPage() { const ticket = useAsync<TicketType[]>(() => api.ticket(), []); return <Protected><Shell><PageTitle icon={Ticket} title="Wallet ticket" subtitle="Tutti i tuoi pass digitali in una vista premium." /><StatusBox {...ticket} />{ticket.data?.length === 0 && <Card className="text-center"><QrCode className="mx-auto mb-4 size-12 text-cyan-200" /><h3 className="text-2xl font-black">Nessun ticket ancora</h3><p className="mt-2 text-slate-400">Esplora gli eventi e prenota il primo pass.</p><Link to="/eventi"><Button className="mt-5"><Compass className="size-4" /> Esplora eventi</Button></Link></Card>}<div className="grid gap-4 md:grid-cols-2">{ticket.data?.map(t => <Card key={t.id} className="relative overflow-hidden"><QrCode className="absolute right-5 top-5 size-20 text-white/10" /><Badge>{t.codice}</Badge><h3 className="mt-4 text-2xl font-black">{t.nomeEvento}</h3><p className="mt-2 text-slate-400">{dataBella(t.dataEvento)} · {t.luogoEvento}</p><Button className="mt-5" variant="danger" onClick={() => api.annullaTicket(t.id).then(ticket.reload)}>Annulla</Button></Card>)}</div></Shell></Protected> }

function NotifichePage() { const notifiche = useAsync<Notifica[]>(() => api.notifiche(), []); return <Protected><Shell><PageTitle icon={Bell} title="Centro notifiche" subtitle="Segnali live da organizzatori, ticket e community." action={<Button disabled={!notifiche.data?.length} onClick={() => api.leggiTutte().then(notifiche.reload)}>Segna tutte lette</Button>} /><StatusBox {...notifiche} />{notifiche.data?.length === 0 && <Card className="text-center"><Bell className="mx-auto mb-4 size-12 text-cyan-200" /><h3 className="text-2xl font-black">Nessuna notifica</h3><p className="mt-2 text-slate-400">Quando succede qualcosa sui tuoi eventi o ticket, lo vedrai qui. La navigazione resta disponibile dalla barra in alto.</p></Card>}<div className="grid gap-3">{notifiche.data?.map(n => <Card key={n.id} className={cn(!n.letta && 'border-cyan-300/30 bg-cyan-300/10')}><div className="flex items-center justify-between gap-4"><div><Badge>{n.tipo}</Badge><p className="mt-3 font-bold">{n.messaggio}</p><p className="text-sm text-slate-400">{dataBella(n.creataIl)}</p></div>{!n.letta && <Button variant="glass" onClick={() => api.leggiNotifica(n.id).then(notifiche.reload)}>Letta</Button>}</div></Card>)}</div></Shell></Protected> }

function Rete() { const amici = useAsync<Amicizia[]>(() => api.amici(), []); const richieste = useAsync<Amicizia[]>(() => api.richiesteAmicizia(), []); const [chatId, setChatId] = useState<number | null>(null); const chat = useAsync<Messaggio[]>(async () => chatId ? (await api.chat(chatId)).content : [], [chatId]); const [testo, setTesto] = useState(''); async function invia() { if (!chatId || !testo.trim()) return; await api.messaggio(chatId, testo); setTesto(''); await chat.reload() } return <Protected><Shell><PageTitle icon={Users} title="Rete & chat" subtitle="Trasforma partecipanti in connessioni e conversazioni." /><div className="grid gap-6 lg:grid-cols-[.75fr_1.25fr]"><div className="grid gap-4"><Card><h3 className="font-black">Amici</h3>{amici.data?.map(a => <button key={a.id} className="mt-3 flex w-full items-center justify-between rounded-2xl bg-white/5 p-3 text-left" onClick={() => setChatId(a.destinatario.id)}><span>{a.destinatario.nome} {a.destinatario.cognome}</span><MessageCircle className="size-4" /></button>)}</Card><Card><h3 className="font-black">Richieste</h3>{richieste.data?.map(r => <div key={r.id} className="mt-3 flex items-center justify-between rounded-2xl bg-white/5 p-3"><span>{r.richiedente.nome}</span><Button variant="glass" onClick={() => api.accettaAmicizia(r.id).then(richieste.reload)}>Accetta</Button></div>)}</Card></div><Card className="min-h-[520px]"><h3 className="text-2xl font-black">Chat {chatId ? `#${chatId}` : ''}</h3><div className="mt-4 h-80 overflow-auto rounded-3xl bg-black/20 p-4">{chat.data?.map(m => <div key={m.id} className="mb-3 rounded-2xl bg-white/10 p-3"><b>{m.nomeMittente}</b><p>{m.contenuto}</p></div>)}</div><div className="mt-4 flex gap-3"><Field placeholder="Scrivi un messaggio" value={testo} onChange={e => setTesto(e.target.value)} /><Button onClick={invia}><Send className="size-4" /></Button></div></Card></div></Shell></Protected> }

function Profilo() { const { utente, refresh } = useAuth(); const [form, setForm] = useState(() => ({ nome: utente?.nome ?? '', cognome: utente?.cognome ?? '', indirizzo: utente?.indirizzo ?? '', telefono: utente?.telefono ?? '' })); async function salva(e: FormEvent) { e.preventDefault(); await api.aggiornaProfilo(form); await refresh() } return <Protected><Shell><PageTitle icon={User} title="Profilo" subtitle="La tua identità dentro EventVerse." /><Card><form onSubmit={salva} className="grid gap-4 md:grid-cols-2"><Field value={form.nome} onChange={e => setForm({ ...form, nome: e.target.value })} /><Field value={form.cognome} onChange={e => setForm({ ...form, cognome: e.target.value })} /><Field value={form.indirizzo} onChange={e => setForm({ ...form, indirizzo: e.target.value })} /><Field value={form.telefono} onChange={e => setForm({ ...form, telefono: e.target.value })} /><Button className="md:col-span-2">Salva profilo</Button></form></Card></Shell></Protected> }

export default function App() {
  return <AnimatePresence mode="wait"><Routes><Route path="/" element={<Home />} /><Route path="/accesso" element={<Accesso />} /><Route path="/eventi" element={<Eventi />} /><Route path="/eventi/:id" element={<DettaglioEvento />} /><Route path="/crea" element={<CreaEvento />} /><Route path="/dashboard" element={<Dashboard />} /><Route path="/mappa" element={<Mappa />} /><Route path="/ticket" element={<TicketPage />} /><Route path="/notifiche" element={<NotifichePage />} /><Route path="/rete" element={<Rete />} /><Route path="/profilo" element={<Profilo />} /></Routes></AnimatePresence>
}