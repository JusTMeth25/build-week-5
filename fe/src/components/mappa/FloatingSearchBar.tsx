import { useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { MapPin, Search, X } from 'lucide-react'
import { molla } from './EventMapMarker'

export type Periodo = 'TUTTI' | 'OGGI' | 'WEEKEND' | 'SETTIMANA' | 'MESE'

export const PERIODI: { valore: Periodo; etichetta: string }[] = [
  { valore: 'TUTTI', etichetta: 'Sempre' },
  { valore: 'OGGI', etichetta: 'Oggi' },
  { valore: 'WEEKEND', etichetta: 'Weekend' },
  { valore: 'SETTIMANA', etichetta: '7 giorni' },
  { valore: 'MESE', etichetta: '30 giorni' },
]

export interface Ricerca {
  testo: string
  luogo: string
  periodo: Periodo
}

interface Props {
  ricerca: Ricerca
  onCambia: (ricerca: Ricerca) => void
}

function CampoPillola({ icona: Icona, valore, onCambia, segnaposto }: { icona: typeof Search; valore: string; onCambia: (v: string) => void; segnaposto: string }) {
  return (
    <label className="flex min-w-0 flex-1 items-center gap-2.5">
      <Icona className="size-4 shrink-0 text-slate-400" />
      <input
        value={valore}
        onChange={(e) => onCambia(e.target.value)}
        placeholder={segnaposto}
        aria-label={segnaposto}
        className="min-w-0 flex-1 bg-transparent text-[15px] text-white outline-none placeholder:text-slate-400"
      />
      <AnimatePresence>
        {valore && (
          <motion.button
            type="button"
            aria-label={`Svuota ${segnaposto.toLowerCase()}`}
            onClick={() => onCambia('')}
            initial={{ opacity: 0, scale: 0.6 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.6 }}
            transition={molla}
            className="grid size-6 shrink-0 place-items-center rounded-full bg-white/10 text-slate-300 hover:bg-white/20"
          >
            <X className="size-3" strokeWidth={3} />
          </motion.button>
        )}
      </AnimatePresence>
    </label>
  )
}

export function FloatingSearchBar({ ricerca, onCambia }: Props) {
  const [attiva, setAttiva] = useState(false)

  return (
    <div className="grid gap-2">
      <motion.form
        role="search"
        onSubmit={(e) => e.preventDefault()}
        onFocus={() => setAttiva(true)}
        onBlur={() => setAttiva(false)}
        animate={{ scale: attiva ? 1.02 : 1 }}
        transition={molla}
        className={`flex h-14 items-center gap-3 rounded-full bg-slate-950/60 pl-5 pr-2 ring-1 backdrop-blur-xl transition-shadow duration-200 ${
          attiva ? 'shadow-2xl shadow-cyan-500/20 ring-cyan-300/40' : 'shadow-xl shadow-black/30 ring-white/10'
        }`}
      >
        <CampoPillola icona={Search} segnaposto="Cerca evento" valore={ricerca.testo} onCambia={(testo) => onCambia({ ...ricerca, testo })} />
        <span className="h-6 w-px shrink-0 bg-white/15" />
        <CampoPillola icona={MapPin} segnaposto="Luogo" valore={ricerca.luogo} onCambia={(luogo) => onCambia({ ...ricerca, luogo })} />
        <motion.button
          type="submit"
          aria-label="Cerca"
          whileHover={{ scale: 1.06 }}
          whileTap={{ scale: 0.9 }}
          transition={molla}
          className="grid size-10 shrink-0 place-items-center rounded-full bg-cyan-300 text-slate-950 shadow-lg shadow-cyan-500/30"
        >
          <Search className="size-[18px]" strokeWidth={2.5} />
        </motion.button>
      </motion.form>

      <div role="radiogroup" aria-label="Quando" className="hide-scrollbar flex justify-center gap-1.5 overflow-x-auto">
        {PERIODI.map(({ valore, etichetta }) => {
          const attivo = ricerca.periodo === valore
          return (
            <motion.button
              key={valore}
              type="button"
              role="radio"
              aria-checked={attivo}
              onClick={() => onCambia({ ...ricerca, periodo: valore })}
              whileTap={{ scale: 0.92 }}
              transition={molla}
              className={`relative shrink-0 rounded-full px-3.5 py-1.5 text-xs font-bold ring-1 backdrop-blur-xl transition-colors duration-150 ${
                attivo ? 'text-slate-950 ring-white' : 'bg-slate-950/55 text-slate-300 ring-white/10 hover:bg-slate-900/70'
              }`}
            >
              {attivo && <motion.span layoutId="periodo-attivo" transition={molla} className="absolute inset-0 rounded-full bg-white" />}
              <span className="relative">{etichetta}</span>
            </motion.button>
          )
        })}
      </div>
    </div>
  )
}
