package it.epicode.eventi.chat.web;

import it.epicode.eventi.chat.ServizioChat;
import it.epicode.eventi.utente.UtenteCorrente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Validated
public class ControllerChat {

	private final ServizioChat servizio;
	private final UtenteCorrente utenteCorrente;

	public ControllerChat(ServizioChat servizio, UtenteCorrente utenteCorrente) {
		this.servizio = servizio;
		this.utenteCorrente = utenteCorrente;
	}

	@PostMapping("/messaggi")
	@ResponseStatus(HttpStatus.CREATED)
	public RispostaMessaggio invia(@Valid @RequestBody RichiestaMessaggio richiesta) {
		return servizio.invia(richiesta.destinatarioId(), richiesta.contenuto(),
				utenteCorrente.richiedi());
	}

	@GetMapping("/{utenteId}/messaggi")
	public Page<RispostaMessaggio> conversazione(@PathVariable Long utenteId,
			@RequestParam(defaultValue = "0") @Min(0) int pagina,
			@RequestParam(defaultValue = "30") @Min(1) @Max(100) int dimensione) {
		return servizio.conversazione(utenteId, utenteCorrente.richiedi(),
				PageRequest.of(pagina, dimensione));
	}

	@GetMapping("/non-letti")
	public Map<String, Long> nonLetti() {
		return Map.of("nonLetti", servizio.contaNonLetti(utenteCorrente.richiedi()));
	}
}
