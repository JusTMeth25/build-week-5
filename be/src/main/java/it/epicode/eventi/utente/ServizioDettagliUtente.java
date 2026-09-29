package it.epicode.eventi.utente;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServizioDettagliUtente implements UserDetailsService {

	private final UtenteRepository utenti;

	public ServizioDettagliUtente(UtenteRepository utenti) {
		this.utenti = utenti;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return utenti.findByEmailIgnoreCase(email)
				.map(UtenteAutenticato::new)
				.orElseThrow(() -> new UsernameNotFoundException("Credenziali non valide"));
	}
}
