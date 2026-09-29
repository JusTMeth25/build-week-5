package it.epicode.eventi.utente;

import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UtenteCorrente {

	private final UtenteRepository utenti;

	public UtenteCorrente(UtenteRepository utenti) {
		this.utenti = utenti;
	}

	@Transactional(readOnly = true)
	public Utente richiedi() {
		Authentication autenticazione = SecurityContextHolder.getContext().getAuthentication();
		if (autenticazione == null || !(autenticazione.getPrincipal() instanceof UtenteAutenticato dettagli)) {
			throw new OperazioneNonConsentita("Serve una sessione autenticata");
		}
		return utenti.findById(dettagli.getId())
				.orElseThrow(() -> new OperazioneNonConsentita("Sessione non piu' valida"));
	}
}
