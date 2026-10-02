package it.epicode.eventi.utente.web;

import it.epicode.eventi.utente.ServizioAutenticazione;
import it.epicode.eventi.utente.ServizioUtenti;
import it.epicode.eventi.utente.UtenteCorrente;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class ControllerAutenticazione {

	private final ServizioUtenti servizioUtenti;
	private final ServizioAutenticazione servizioAutenticazione;
	private final UtenteCorrente utenteCorrente;

	public ControllerAutenticazione(ServizioUtenti servizioUtenti,
			ServizioAutenticazione servizioAutenticazione,
			UtenteCorrente utenteCorrente) {
		this.servizioUtenti = servizioUtenti;
		this.servizioAutenticazione = servizioAutenticazione;
		this.utenteCorrente = utenteCorrente;
	}

	@PostMapping("/registrazione")
	@ResponseStatus(HttpStatus.CREATED)
	public RispostaUtente registrazione(@Valid @RequestBody RichiestaRegistrazione richiesta) {
		return RispostaUtente.da(servizioUtenti.registra(richiesta));
	}

	@PostMapping("/verifica")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void verifica(@Valid @RequestBody RichiestaVerifica richiesta) {
		servizioUtenti.verifica(richiesta.email(), richiesta.codice());
	}

	@PostMapping("/codice")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reinviaCodice(@Valid @RequestBody RichiestaCodice richiesta) {
		servizioUtenti.reinviaCodice(richiesta.email());
	}

	@PostMapping("/password-dimenticata")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void passwordDimenticata(@Valid @RequestBody RichiestaPasswordDimenticata richiesta) {
		servizioUtenti.richiediResetPassword(richiesta.email());
	}

	@PostMapping("/reimposta-password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reimpostaPassword(@Valid @RequestBody RichiestaReimpostaPassword richiesta) {
		servizioUtenti.reimpostaPassword(richiesta.token(), richiesta.password());
	}

	@PostMapping("/login")
	public RispostaAccesso login(@Valid @RequestBody RichiestaLogin richiesta) {
		var accesso = servizioAutenticazione.accedi(richiesta.email(), richiesta.password());
		return new RispostaAccesso(accesso.token(), RispostaUtente.da(accesso.utente()));
	}

	@GetMapping("/io")
	public RispostaUtente io() {
		return RispostaUtente.da(utenteCorrente.richiedi());
	}
}
