import { useEffect, useMemo, useRef, useState } from 'react'
import { motion } from 'framer-motion'
import Map, { type MapRef } from 'react-map-gl/mapbox'
import 'mapbox-gl/dist/mapbox-gl.css'
import { api, type EventoMappa } from '@/lib/api'
import { CATEGORIE, deduciCategoria, EventMapMarker, molla, type Categoria, type EventoSullaMappa } from './EventMapMarker'
import { FloatingSearchBar, type Periodo, type Ricerca } from './FloatingSearchBar'
import { GlassPanelList } from './GlassPanelList'

// Italia intera, inclinata: lo stile "standard" mostra gli edifici in 3D.
const VISTA_INIZIALE = { longitude: 12.5, latitude: 42.2, zoom: 5.2, pitch: 50, bearing: -12 }

const GIORNO_MS = 86_400_000

function intervallo(periodo: Periodo): [number, number] | null {
  const ora = new Date()
  const mezzanotte = new Date(ora.getFullYear(), ora.getMonth(), ora.getDate()).getTime()
  switch (periodo) {
    case 'TUTTI':
      return null
    case 'OGGI':
      return [mezzanotte, mezzanotte + GIORNO_MS]
    case 'WEEKEND': {
      // Da sabato a domenica compresi; nel weekend conta da oggi.
      const giorno = ora.getDay()
      const finoADomenica = (7 - giorno) % 7
      const inizio = giorno === 0 || giorno === 6 ? mezzanotte : mezzanotte + (finoADomenica - 1) * GIORNO_MS
      return [inizio, mezzanotte + (finoADomenica + 1) * GIORNO_MS]
    }
    case 'SETTIMANA':
      return [ora.getTime(), ora.getTime() + 7 * GIORNO_MS]
    case 'MESE':
      return [ora.getTime(), ora.getTime() + 30 * GIORNO_MS]
  }
}

type Posizione = { latitudine: number; longitudine: number }

// Se il permesso non e' ancora stato dato, il browser mostra la richiesta e il callback
// arriva solo quando l'utente accetta: per questo non c'e' timeout.
function chiediPosizione(onPosizione: (posizione: Posizione) => void) {
  navigator.geolocation.getCurrentPosition(
    (pos) => onPosizione({ latitudine: pos.coords.latitude, longitudine: pos.coords.longitude }),
    () => {}, // negata o non disponibile: restano gli eventi ordinati per data
    { maximumAge: 120_000 },
  )
}

const preparaEventi = (lista: EventoMappa[]): EventoSullaMappa[] =>
  lista
    .filter((e) => Number.isFinite(e.latitudine) && Number.isFinite(e.longitudine))
    .map((e) => ({ ...e, categoria: deduciCategoria(e.titolo) }))

export default function MapDashboard() {
  const mappa = useRef<MapRef>(null)
  const [eventi, setEventi] = useState<EventoSullaMappa[]>([])
  const [errore, setErrore] = useState<string | null>(null)
  const [ricerca, setRicerca] = useState<Ricerca>({ testo: '', luogo: '', periodo: 'TUTTI' })
  const [filtri, setFiltri] = useState<Set<Categoria>>(new Set())
  const [selezionatoId, setSelezionatoId] = useState<number | null>(null)
  const [posizione, setPosizione] = useState<Posizione | null>(null)

  // Subito senza posizione (ordine per data), poi di nuovo appena la posizione arriva:
  // il backend ordina per vicinanza e aggiunge distanzaKm.
  useEffect(() => {
    let annullato = false
    api
      .mappa(posizione?.latitudine, posizione?.longitudine)
      .then((lista) => {
        if (annullato) return
        setEventi(preparaEventi(lista))
        setErrore(null)
      })
      .catch((e) => !annullato && setErrore(e instanceof Error ? e.message : String(e)))
    return () => {
      annullato = true
    }
  }, [posizione])

  // Il consenso puo' arrivare dalla richiesta del browser o piu' tardi dalle sue
  // impostazioni: in entrambi i casi gli eventi si riordinano senza ricaricare la pagina.
  useEffect(() => {
    if (!navigator.geolocation) return
    let attivo = true
    let permesso: PermissionStatus | undefined
    const alCambio = () => permesso?.state === 'granted' && chiediPosizione(setPosizione)

    chiediPosizione((p) => attivo && setPosizione(p))
    navigator.permissions
      ?.query({ name: 'geolocation' })
      .then((stato) => {
        if (!attivo) return
        permesso = stato
        stato.addEventListener('change', alCambio)
      })
      .catch(() => {})

    return () => {
      attivo = false
      permesso?.removeEventListener('change', alCambio)
    }
  }, [])

  const visibili = useMemo(() => {
    const testo = ricerca.testo.trim().toLowerCase()
    const luogo = ricerca.luogo.trim().toLowerCase()
    const finestra = intervallo(ricerca.periodo)
    return eventi.filter((e) => {
      const quando = new Date(e.dataEvento).getTime()
      return (
        (filtri.size === 0 || filtri.has(e.categoria)) &&
        (!testo || e.titolo.toLowerCase().includes(testo)) &&
        (!luogo || e.luogo.toLowerCase().includes(luogo)) &&
        (!finestra || (quando >= finestra[0] && quando < finestra[1]))
      )
    })
  }, [eventi, ricerca, filtri])

  function seleziona(id: number) {
    setSelezionatoId(id)
    const evento = eventi.find((e) => e.id === id)
    if (evento) {
      mappa.current?.flyTo({ center: [evento.longitudine, evento.latitudine], zoom: 15, pitch: 60, duration: 1400, essential: true })
    }
  }

  function alternaFiltro(categoria: Categoria) {
    setFiltri((prima) => {
      const dopo = new Set(prima)
      if (!dopo.delete(categoria)) dopo.add(categoria)
      return dopo
    })
  }

  return (
    // Fissa sotto l'header di Shell: la barra di navigazione resta sopra la mappa, in vetro.
    <div className="fixed inset-0 overflow-hidden bg-slate-950">
      <Map
        ref={mappa}
        mapboxAccessToken={import.meta.env.VITE_MAPBOX_TOKEN}
        initialViewState={VISTA_INIZIALE}
        mapStyle="mapbox://styles/mapbox/standard"
        style={{ position: 'absolute', inset: 0 }}
        onLoad={(e) => e.target.setConfigProperty('basemap', 'lightPreset', 'night')}
        onClick={() => setSelezionatoId(null)}
      >
        {visibili.map((evento) => (
          <EventMapMarker key={evento.id} evento={evento} selezionato={evento.id === selezionatoId} onSeleziona={seleziona} />
        ))}
      </Map>

      {/* I contenitori ignorano il puntatore: fra un pannello e l'altro la mappa resta trascinabile. */}
      <div className="pointer-events-none absolute left-28 right-4 top-24 flex justify-center lg:right-[23rem]">
        <div className="pointer-events-auto w-full max-w-xl">
          <FloatingSearchBar ricerca={ricerca} onCambia={setRicerca} />
        </div>
      </div>

      <nav
        aria-label="Filtra per tipo di evento"
        className="absolute left-4 top-[calc(50%+2rem)] flex -translate-y-1/2 flex-col gap-1 rounded-3xl bg-slate-950/55 p-2 shadow-xl shadow-black/30 ring-1 ring-white/10 backdrop-blur-2xl sm:left-6"
      >
        {(Object.keys(CATEGORIE) as Categoria[]).map((categoria) => {
          const { etichetta, icona: Icona, sfondo, ombra } = CATEGORIE[categoria]
          const attivo = filtri.has(categoria)
          return (
            <motion.button
              key={categoria}
              type="button"
              aria-pressed={attivo}
              onClick={() => alternaFiltro(categoria)}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.92 }}
              transition={molla}
              className="flex w-16 flex-col items-center gap-1 rounded-2xl py-2 text-[11px] font-bold text-slate-300 hover:bg-white/10"
            >
              <span
                className={`grid size-10 place-items-center rounded-2xl transition-all duration-200 ${
                  attivo ? `${sfondo} text-white shadow-lg ${ombra}` : 'bg-white/10 text-slate-300'
                }`}
              >
                <Icona className="size-5" />
              </span>
              {etichetta}
            </motion.button>
          )
        })}
      </nav>

      <aside className="absolute bottom-6 right-6 top-24 hidden w-80 lg:block">
        <GlassPanelList eventi={visibili} selezionatoId={selezionatoId} onSeleziona={seleziona} />
      </aside>

      {errore && (
        <p className="absolute bottom-6 left-1/2 -translate-x-1/2 rounded-2xl bg-rose-950/80 px-4 py-3 text-sm text-rose-100 shadow-lg shadow-black/30 ring-1 ring-rose-300/30 backdrop-blur-xl">
          Eventi non caricati: {errore}
        </p>
      )}
    </div>
  )
}
