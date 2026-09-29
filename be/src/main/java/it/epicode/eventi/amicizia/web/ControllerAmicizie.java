package it.epicode.eventi.amicizia.web;

import it.epicode.eventi.amicizia.ServizioAmicizie;
import it.epicode.eventi.amicizia.ServizioPartecipanti;
import it.epicode.eventi.utente.UtenteCorrente;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ControllerAmicizie {

	private final ServizioAmicizie servizioAmicizie;
	private final ServizioPartecipanti servizioPartecipanti;
	private final UtenteCorrente utenteCorrente;

	public ControllerAmicizie(ServizioAmicizie servizioAmicizie,
			ServizioPartecipanti servizioPartecipanti,
			UtenteCorrente utenteCorrente) {
		this.servizioAmicizie = servizioAmicizie;
		this.servizioPartecipanti = servizioPartecipanti;
		this.utenteCorrente = utenteCorrente;
	}

	@GetMapping("/eventi/{eventoId}/partecipanti")
	public List<RispostaPartecipante> partecipanti(@PathVariable Long eventoId) {
		return servizioPartecipanti.dellEvento(eventoId, utenteCorrente.richiedi());
	}

	@PostMapping("/amicizie/{utenteId}")
	@ResponseStatus(HttpStatus.CREATED)
	public RispostaAmicizia richiedi(@PathVariable Long utenteId) {
		return servizioAmicizie.richiedi(utenteId, utenteCorrente.richiedi());
	}

	@PostMapping("/amicizie/{id}/accetta")
	public RispostaAmicizia accetta(@PathVariable Long id) {
		return servizioAmicizie.accetta(id, utenteCorrente.richiedi());
	}

	@PostMapping("/amicizie/{id}/rifiuta")
	public RispostaAmicizia rifiuta(@PathVariable Long id) {
		return servizioAmicizie.rifiuta(id, utenteCorrente.richiedi());
	}

	@GetMapping("/amicizie")
	public List<RispostaAmicizia> amici() {
		return servizioAmicizie.amici(utenteCorrente.richiedi());
	}

	@GetMapping("/amicizie/richieste")
	public List<RispostaAmicizia> richieste() {
		return servizioAmicizie.richiesteRicevute(utenteCorrente.richiedi());
	}
}
