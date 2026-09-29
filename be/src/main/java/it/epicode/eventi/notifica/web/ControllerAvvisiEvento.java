package it.epicode.eventi.notifica.web;

import it.epicode.eventi.notifica.ServizioAvvisiEvento;
import it.epicode.eventi.utente.UtenteCorrente;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/eventi/{eventoId}/notifiche")
public class ControllerAvvisiEvento {

	private final ServizioAvvisiEvento servizio;
	private final UtenteCorrente utenteCorrente;

	public ControllerAvvisiEvento(ServizioAvvisiEvento servizio, UtenteCorrente utenteCorrente) {
		this.servizio = servizio;
		this.utenteCorrente = utenteCorrente;
	}

	@PostMapping
	public Map<String, Integer> avvisa(@PathVariable Long eventoId,
			@Valid @RequestBody RichiestaMessaggioPartecipanti richiesta) {
		int avvisati = servizio.avvisaPartecipanti(eventoId, richiesta.messaggio(),
				utenteCorrente.richiedi());
		return Map.of("avvisati", avvisati);
	}
}
