import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, sessione, type Utente } from '@/lib/api'

type AuthContextValue = {
  utente: Utente | null
  caricamento: boolean
  login: (email: string, password: string) => Promise<void>
  logout: () => void
  refresh: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [utente, setUtente] = useState<Utente | null>(null)
  const [caricamento, setCaricamento] = useState(Boolean(sessione.token()))

  const refresh = useCallback(async () => {
    if (!sessione.token()) {
      setUtente(null)
      setCaricamento(false)
      return
    }
    try {
      setUtente(await api.io())
    } catch {
      sessione.cancella()
      setUtente(null)
    } finally {
      setCaricamento(false)
    }
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  const login = useCallback(async (email: string, password: string) => {
    const accesso = await api.login(email, password)
    sessione.salva(accesso.token)
    setUtente(accesso.utente)
  }, [])

  const logout = useCallback(() => {
    sessione.cancella()
    setUtente(null)
  }, [])

  const value = useMemo(() => ({ utente, caricamento, login, logout, refresh }), [utente, caricamento, login, logout, refresh])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth deve essere usato dentro AuthProvider')
  return value
}