package it.epicode.eventi.notifica.web;

import it.epicode.eventi.notifica.ServizioNotifiche;
import it.epicode.eventi.utente.UtenteCorrente;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifiche")
@Validated
public class ControllerNotifiche {

	private final ServizioNotifiche servizio;
	private final UtenteCorrente utenteCorrente;

	public ControllerNotifiche(ServizioNotifiche servizio, UtenteCorrente utenteCorrente) {
		this.servizio = servizio;
		this.utenteCorrente = utenteCorrente;
	}

	@GetMapping
	public Page<RispostaNotifica> elenco(
			@RequestParam(defaultValue = "0") @Min(0) int pagina,
			@RequestParam(defaultValue = "20") @Min(1) @Max(50) int dimensione) {
		return servizio.elenco(utenteCorrente.richiedi(), PageRequest.of(pagina, dimensione));
	}

	@GetMapping("/non-lette")
	public Map<String, Long> nonLette() {
		return Map.of("nonLette", servizio.contaNonLette(utenteCorrente.richiedi()));
	}

	@PostMapping("/{id}/letta")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void segnaLetta(@PathVariable Long id) {
		servizio.segnaLetta(id, utenteCorrente.richiedi());
	}

	@PostMapping("/lette")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void segnaTutteLette() {
		servizio.segnaTutteLette(utenteCorrente.richiedi());
	}
}
