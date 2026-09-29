package it.epicode.eventi.privacy.web;

import it.epicode.eventi.privacy.ServizioAnonimizzazione;
import it.epicode.eventi.utente.UtenteCorrente;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/privacy")
public class ControllerPrivacy {

	private final ServizioAnonimizzazione servizio;
	private final UtenteCorrente utenteCorrente;

	public ControllerPrivacy(ServizioAnonimizzazione servizio, UtenteCorrente utenteCorrente) {
		this.servizio = servizio;
		this.utenteCorrente = utenteCorrente;
	}

	@PostMapping("/anonimizzazione")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void anonimizza(@Valid @RequestBody RichiestaAnonimizzazione richiesta) {
		servizio.anonimizza(utenteCorrente.richiedi(), richiesta.password(), richiesta.confermo());
		SecurityContextHolder.clearContext();
	}
}
