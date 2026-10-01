import { AnimatePresence, motion, type Transition } from 'framer-motion'
import { Marker } from 'react-map-gl/mapbox'
import { Link } from 'react-router-dom'
import { CalendarDays, Cpu, MapPin, Music, Palette, Sparkles, Ticket, Trophy, UtensilsCrossed, type LucideIcon } from 'lucide-react'
import type { EventoMappa } from '@/lib/api'
import { dataBella } from '@/lib/utils'

export type Categoria = 'MUSICA' | 'ARTE' | 'SPORT' | 'CIBO' | 'TECH' | 'ALTRO'

export type EventoSullaMappa = EventoMappa & { categoria: Categoria }

// Classi scritte per intero: Tailwind non vede quelle composte a runtime.
export const CATEGORIE: Record<Categoria, { etichetta: string; icona: LucideIcon; sfondo: string; ombra: string; parole: string[] }> = {
  MUSICA: { etichetta: 'Musica', icona: Music, sfondo: 'bg-fuchsia-500', ombra: 'shadow-fuchsia-500/40', parole: ['concerto', 'musica', 'jazz', 'rock', 'dj', 'live', 'festival', 'sinfonic'] },
  ARTE: { etichetta: 'Arte', icona: Palette, sfondo: 'bg-amber-500', ombra: 'shadow-amber-500/40', parole: ['mostra', 'arte', 'opera', 'teatro', 'tosca', 'museo', 'cinema'] },
  SPORT: { etichetta: 'Sport', icona: Trophy, sfondo: 'bg-emerald-500', ombra: 'shadow-emerald-500/40', parole: ['maratona', 'partita', 'corsa', 'sport', 'torneo', 'gara'] },
  CIBO: { etichetta: 'Cibo', icona: UtensilsCrossed, sfondo: 'bg-rose-500', ombra: 'shadow-rose-500/40', parole: ['food', 'vini', 'vino', 'sagra', 'degustazione', 'cucina', 'aperitivo'] },
  TECH: { etichetta: 'Tech', icona: Cpu, sfondo: 'bg-indigo-500', ombra: 'shadow-indigo-500/40', parole: ['tech', 'hackathon', 'startup', 'coding', ' ai ', 'digital'] },
  ALTRO: { etichetta: 'Altro', icona: Sparkles, sfondo: 'bg-cyan-500', ombra: 'shadow-cyan-500/40', parole: [] },
}

// Provvisorio: il backend non ha ancora un campo categoria, quindi la si deduce dal titolo.
export function deduciCategoria(titolo: string): Categoria {
  const testo = ` ${titolo.toLowerCase()} `
  const trovata = (Object.keys(CATEGORIE) as Categoria[]).find((c) => CATEGORIE[c].parole.some((p) => testo.includes(p)))
  return trovata ?? 'ALTRO'
}

// Molla condivisa da tutti i componenti: rimbalzo breve, assestamento rapido.
export const molla: Transition = { type: 'spring', stiffness: 420, damping: 28, mass: 0.8 }

const immagineRiserva = 'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?q=80&w=800&auto=format&fit=crop'

const formattaOra = (iso: string) => new Intl.DateTimeFormat('it-IT', { hour: '2-digit', minute: '2-digit' }).format(new Date(iso))

interface Props {
  evento: EventoSullaMappa
  selezionato: boolean
  onSeleziona: (id: number) => void
}

export function EventMapMarker({ evento, selezionato, onSeleziona }: Props) {
  const { etichetta, icona: Icona, sfondo, ombra } = CATEGORIE[evento.categoria]

  return (
    <Marker
      longitude={evento.longitudine}
      latitude={evento.latitudine}
      anchor="bottom"
      style={{ zIndex: selezionato ? 10 : 1 }}
      onClick={(e) => {
        // Senza questo il click arriva anche alla mappa, che deseleziona.
        e.originalEvent.stopPropagation()
        onSeleziona(evento.id)
      }}
    >
      <motion.button
        type="button"
        aria-label={evento.titolo}
        animate={{ scale: selezionato ? 1.12 : 1, y: selezionato ? -6 : 0 }}
        whileHover={{ scale: selezionato ? 1.12 : 1.06, y: -4 }}
        whileTap={{ scale: 0.94 }}
        transition={molla}
        className={`flex items-center gap-1.5 rounded-2xl bg-slate-950/80 py-1 pl-1 pr-3 text-xs font-bold text-white shadow-lg ring-1 backdrop-blur-md ${
          selezionato ? `ring-white/60 ${ombra}` : 'shadow-black/30 ring-white/15'
        }`}
      >
        <span className={`grid size-6 place-items-center rounded-xl text-white ${sfondo}`}>
          <Icona className="size-3.5" strokeWidth={2.5} />
        </span>
        <span className="text-slate-400">{formattaOra(evento.dataEvento)}</span>
        <span className="max-w-32 truncate">{evento.titolo}</span>
      </motion.button>

      <AnimatePresence>
        {selezionato && (
          <div className="absolute bottom-full left-1/2 mb-4 -translate-x-1/2">
            <motion.article
              initial={{ opacity: 0, y: 12, scale: 0.9 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 8, scale: 0.95 }}
              transition={molla}
              style={{ transformOrigin: 'bottom center' }}
              className="w-72 overflow-hidden rounded-3xl bg-slate-950/90 text-left shadow-2xl shadow-cyan-500/15 ring-1 ring-white/10 backdrop-blur-2xl"
            >
              <div className="relative h-28">
                <img src={evento.immaginePrincipale || immagineRiserva} alt="" className="h-full w-full object-cover" />
                <div className="absolute inset-0 bg-gradient-to-t from-slate-950 to-transparent" />
                <span className={`absolute left-4 top-4 inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-bold text-white ${sfondo}`}>
                  <Icona className="size-3" /> {etichetta}
                </span>
              </div>
              <div className="px-5 pb-4">
                <h3 className="text-lg font-black leading-snug tracking-tight text-white">{evento.titolo}</h3>
                <p className="mt-2 flex items-center gap-1.5 text-sm text-slate-400">
                  <CalendarDays className="size-4" /> {dataBella(evento.dataEvento)}
                </p>
                <p className="mt-1 flex items-center gap-1.5 text-sm text-slate-400">
                  <MapPin className="size-4" /> {evento.luogo}
                  {evento.distanzaKm != null && <span className="text-cyan-200">· {evento.distanzaKm} km</span>}
                </p>
              </div>
              <Link
                to={`/eventi/${evento.id}`}
                className="flex items-center justify-center gap-2 border-t border-white/10 py-3.5 text-sm font-bold text-cyan-200 transition-colors duration-150 hover:bg-white/5"
              >
                <Ticket className="size-4" /> Dettagli e ticket
              </Link>
            </motion.article>
          </div>
        )}
      </AnimatePresence>
    </Marker>
  )
}
