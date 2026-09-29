package it.epicode.eventi.evento.web;

import it.epicode.eventi.evento.ServizioEventi;
import it.epicode.eventi.utente.UtenteCorrente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eventi")
@Validated
public class ControllerEventi {

	private final ServizioEventi servizio;
	private final UtenteCorrente utenteCorrente;

	public ControllerEventi(ServizioEventi servizio, UtenteCorrente utenteCorrente) {
		this.servizio = servizio;
		this.utenteCorrente = utenteCorrente;
	}

	@GetMapping
	public Page<RispostaEventoSintesi> elenco(
			@RequestParam(required = false) String testo,
			@RequestParam(defaultValue = "0") @Min(0) int pagina,
			@RequestParam(defaultValue = "12") @Min(1) @Max(50) int dimensione) {
		return servizio.cerca(testo, PageRequest.of(pagina, dimensione));
	}

	@GetMapping("/{id}")
	public RispostaEvento dettaglio(@PathVariable Long id) {
		return servizio.dettaglio(id);
	}

	@GetMapping("/miei")
	public Page<RispostaEventoSintesi> miei(
			@RequestParam(defaultValue = "0") @Min(0) int pagina,
			@RequestParam(defaultValue = "12") @Min(1) @Max(50) int dimensione) {
		return servizio.miei(utenteCorrente.richiedi(), PageRequest.of(pagina, dimensione));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RispostaEvento crea(@Valid @RequestBody RichiestaEvento richiesta) {
		return servizio.crea(richiesta, utenteCorrente.richiedi());
	}

	@PutMapping("/{id}")
	public RispostaEvento aggiorna(@PathVariable Long id, @Valid @RequestBody RichiestaEvento richiesta) {
		return servizio.aggiorna(id, richiesta, utenteCorrente.richiedi());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void elimina(@PathVariable Long id) {
		servizio.elimina(id, utenteCorrente.richiedi());
	}

	@PostMapping("/{id}/descrizione/migliora")
	public RispostaDescrizione miglioraDescrizione(@PathVariable Long id,
			@Valid @RequestBody RichiestaMiglioramentoDescrizione richiesta) {
		return servizio.proponiDescrizione(id, richiesta.descrizione(), utenteCorrente.richiedi());
	}
}
