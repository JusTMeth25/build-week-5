package it.epicode.eventi.ticket.web;

import it.epicode.eventi.ticket.ServizioTicket;
import it.epicode.eventi.utente.UtenteCorrente;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ControllerTicket {

	private final ServizioTicket servizio;
	private final UtenteCorrente utenteCorrente;

	public ControllerTicket(ServizioTicket servizio, UtenteCorrente utenteCorrente) {
		this.servizio = servizio;
		this.utenteCorrente = utenteCorrente;
	}

	@PostMapping("/eventi/{eventoId}/iscrizione")
	@ResponseStatus(HttpStatus.CREATED)
	public RispostaTicket iscrivi(@PathVariable Long eventoId) {
		return servizio.iscrivi(eventoId, utenteCorrente.richiedi());
	}

	@GetMapping("/ticket")
	public List<RispostaTicket> miei() {
		return servizio.miei(utenteCorrente.richiedi());
	}

	@DeleteMapping("/ticket/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void annulla(@PathVariable Long id) {
		servizio.annulla(id, utenteCorrente.richiedi());
	}
}
