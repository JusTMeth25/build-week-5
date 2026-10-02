import { Loader2, MapPin } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Field } from '@/components/ui'
import { api, type IndirizzoGeocodificato, type SuggerimentoIndirizzo } from '@/lib/api'
import { cn } from '@/lib/utils'

// Un token per sessione di ricerca: Google fattura battute + dettaglio come un'unica richiesta.
const nuovaSessione = () => crypto.randomUUID()

type Props = {
  value: string
  onChange: (testo: string) => void
  onSeleziona: (luogo: IndirizzoGeocodificato) => void
  className?: string
}

/** Campo indirizzo con i suggerimenti di Google Places (via backend, la chiave resta lì). */
export function IndirizzoAutocomplete({ value, onChange, onSeleziona, className }: Props) {
  const [suggerimenti, setSuggerimenti] = useState<SuggerimentoIndirizzo[]>([])
  const [aperto, setAperto] = useState(false)
  const [attivo, setAttivo] = useState(-1)
  const [caricamento, setCaricamento] = useState(false)
  const sessione = useRef(nuovaSessione())
  // Solo le battute dell'utente aprono i suggerimenti, non i valori impostati dal codice.
  const digitato = useRef(false)

  useEffect(() => {
    if (!digitato.current) return
    const testo = value.trim()
    if (testo.length < 3) { setSuggerimenti([]); return }
    let annullato = false
    const timer = setTimeout(async () => {
      setCaricamento(true)
      try {
        const risultati = await api.suggerimentiIndirizzo(testo, sessione.current)
        if (!annullato) { setSuggerimenti(risultati); setAttivo(-1); setAperto(true) }
      } catch {
        if (!annullato) setSuggerimenti([])
      } finally {
        if (!annullato) setCaricamento(false)
      }
    }, 300)
    return () => { annullato = true; clearTimeout(timer) }
  }, [value])

  async function seleziona(s: SuggerimentoIndirizzo) {
    setAperto(false)
    setSuggerimenti([])
    digitato.current = false
    onChange(s.descrizione)
    setCaricamento(true)
    try {
      onSeleziona(await api.dettaglioLuogo(s.idLuogo, sessione.current))
    } catch {
      // Senza dettagli resta il testo: le coordinate le ricava il backend al salvataggio.
    } finally {
      setCaricamento(false)
      sessione.current = nuovaSessione()
    }
  }

  function tasto(e: React.KeyboardEvent<HTMLInputElement>) {
    if (!aperto || suggerimenti.length === 0) return
    if (e.key === 'ArrowDown') { e.preventDefault(); setAttivo(i => (i + 1) % suggerimenti.length) }
    else if (e.key === 'ArrowUp') { e.preventDefault(); setAttivo(i => (i <= 0 ? suggerimenti.length - 1 : i - 1)) }
    else if (e.key === 'Enter' && attivo >= 0) { e.preventDefault(); void seleziona(suggerimenti[attivo]) }
    else if (e.key === 'Escape') setAperto(false)
  }

  return (
    <div className={cn('relative', className)}>
      <Field
        placeholder="Indirizzo (inizia a scrivere per i suggerimenti)"
        value={value}
        autoComplete="off"
        role="combobox"
        aria-expanded={aperto && suggerimenti.length > 0}
        aria-autocomplete="list"
        onChange={e => { digitato.current = true; onChange(e.target.value) }}
        onKeyDown={tasto}
        onFocus={() => suggerimenti.length > 0 && setAperto(true)}
        onBlur={() => setTimeout(() => setAperto(false), 150)}
      />
      {caricamento && <Loader2 className="absolute right-4 top-3.5 size-4 animate-spin text-slate-400" />}
      {aperto && suggerimenti.length > 0 && (
        <ul role="listbox" className="absolute z-20 mt-2 w-full overflow-hidden rounded-2xl border border-white/10 bg-slate-900/95 shadow-2xl backdrop-blur-xl">
          {suggerimenti.map((s, i) => (
            <li
              key={s.idLuogo}
              role="option"
              aria-selected={i === attivo}
              className={cn('flex cursor-pointer items-start gap-3 px-4 py-3 text-sm hover:bg-white/10', i === attivo && 'bg-white/10')}
              onMouseDown={e => { e.preventDefault(); void seleziona(s) }}
            >
              <MapPin className="mt-0.5 size-4 shrink-0 text-cyan-200" />
              <span>
                <span className="block font-bold text-white">{s.principale || s.descrizione}</span>
                {s.secondario && <span className="block text-xs text-slate-400">{s.secondario}</span>}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
