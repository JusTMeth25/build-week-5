import { AnimatePresence, motion } from 'framer-motion'
import { ArrowUpRight, MapPin, SearchX } from 'lucide-react'
import { dataBella } from '@/lib/utils'
import { CATEGORIE, molla, type EventoSullaMappa } from './EventMapMarker'

interface Props {
  eventi: EventoSullaMappa[]
  selezionatoId: number | null
  onSeleziona: (id: number) => void
}

export function GlassPanelList({ eventi, selezionatoId, onSeleziona }: Props) {
  return (
    <section className="flex h-full flex-col overflow-hidden rounded-3xl bg-slate-950/55 shadow-xl shadow-black/30 ring-1 ring-white/10 backdrop-blur-2xl">
      <header className="flex items-baseline justify-between px-5 pb-3 pt-5">
        <h2 className="text-lg font-black tracking-tight text-white">Eventi</h2>
        <span className="text-sm tabular-nums text-slate-400">{eventi.length}</span>
      </header>

      <motion.ul layout className="flex-1 space-y-2 overflow-y-auto overscroll-contain px-3 pb-3">
        <AnimatePresence initial={false} mode="popLayout">
          {eventi.map((evento, i) => {
            const { icona: Icona, sfondo } = CATEGORIE[evento.categoria]
            const selezionato = evento.id === selezionatoId
            return (
              <motion.li
                key={evento.id}
                layout
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.95 }}
                transition={{ ...molla, delay: Math.min(i, 8) * 0.03 }}
              >
                <motion.button
                  type="button"
                  onClick={() => onSeleziona(evento.id)}
                  whileHover={{ y: -2 }}
                  whileTap={{ scale: 0.98 }}
                  transition={molla}
                  className={`group flex w-full items-start gap-3 rounded-2xl p-3 text-left transition-colors duration-150 ${
                    selezionato ? 'bg-white/15 shadow-lg shadow-cyan-500/10 ring-1 ring-cyan-300/30' : 'bg-white/5 hover:bg-white/10'
                  }`}
                >
                  <span className={`grid size-10 shrink-0 place-items-center rounded-2xl text-white ${sfondo}`}>
                    <Icona className="size-5" />
                  </span>
                  <span className="min-w-0 flex-1">
                    <span className="block text-xs font-medium text-slate-400">{dataBella(evento.dataEvento)}</span>
                    <span className="mt-0.5 block truncate font-bold text-white">{evento.titolo}</span>
                    <span className="mt-0.5 flex items-center gap-1 text-xs text-slate-400">
                      <MapPin className="size-3" /> <span className="truncate">{evento.luogo}</span>
                      {evento.distanzaKm != null && <span className="shrink-0 text-cyan-200">· {evento.distanzaKm} km</span>}
                    </span>
                  </span>
                  <ArrowUpRight className="size-4 shrink-0 text-slate-500 transition-colors duration-150 group-hover:text-cyan-300" />
                </motion.button>
              </motion.li>
            )
          })}
        </AnimatePresence>

        {eventi.length === 0 && (
          <li className="flex flex-col items-center gap-2 py-12 text-center text-sm text-slate-400">
            <SearchX className="size-6 text-slate-500" />
            Nessun evento con questi filtri
          </li>
        )}
      </motion.ul>
    </section>
  )
}
