import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function dataBella(value?: string) {
  if (!value) return 'Da definire'
  return new Intl.DateTimeFormat('it-IT', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}

export function giorno(value?: string) {
  if (!value) return '--'
  return new Intl.DateTimeFormat('it-IT', { day: '2-digit', month: 'short' }).format(new Date(value))
}

export function iniziali(nome?: string, cognome?: string) {
  return `${nome?.[0] ?? 'E'}${cognome?.[0] ?? 'V'}`.toUpperCase()
}