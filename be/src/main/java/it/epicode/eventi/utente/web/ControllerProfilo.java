package it.epicode.eventi.utente.web;

import it.epicode.eventi.utente.ServizioUtenti;
import it.epicode.eventi.utente.UtenteCorrente;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/utenti")
public class ControllerProfilo {

	private final ServizioUtenti servizioUtenti;
	private final UtenteCorrente utenteCorrente;

	public ControllerProfilo(ServizioUtenti servizioUtenti, UtenteCorrente utenteCorrente) {
		this.servizioUtenti = servizioUtenti;
		this.utenteCorrente = utenteCorrente;
	}

	@GetMapping("/me")
	public RispostaUtente profilo() {
		return RispostaUtente.da(utenteCorrente.richiedi());
	}

	@PutMapping("/me")
	public RispostaUtente aggiorna(@Valid @RequestBody RichiestaAggiornamentoProfilo richiesta) {
		return RispostaUtente.da(servizioUtenti.aggiornaProfilo(utenteCorrente.richiedi(), richiesta));
	}
}
