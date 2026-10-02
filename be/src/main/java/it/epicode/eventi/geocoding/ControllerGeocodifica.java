package it.epicode.eventi.geocoding;

import it.epicode.eventi.comune.eccezioni.RisorsaNonTrovata;
import it.epicode.eventi.geocoding.ServizioGeocodifica.Indirizzo;
import it.epicode.eventi.geocoding.ServizioGeocodifica.Suggerimento;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Proxy verso Google per il form di creazione evento: cosi' la chiave non finisce nel
 * bundle del frontend. Riservato agli utenti autenticati (chi crea eventi).
 */
@RestController
@RequestMapping("/api/geocoding")
@Validated
public class ControllerGeocodifica {

	private final ServizioGeocodifica servizio;

	public ControllerGeocodifica(ServizioGeocodifica servizio) {
		this.servizio = servizio;
	}

	@GetMapping("/suggerimenti")
	public List<Suggerimento> suggerimenti(
			@RequestParam @Size(min = 3, max = 200) String testo,
			@RequestParam(required = false) @Size(max = 100) String sessione) {
		return servizio.suggerimenti(testo, sessione);
	}

	@GetMapping("/luoghi/{idLuogo}")
	public Indirizzo luogo(@PathVariable @Size(max = 300) String idLuogo,
			@RequestParam(required = false) @Size(max = 100) String sessione) {
		return servizio.luogo(idLuogo, sessione)
				.orElseThrow(() -> new RisorsaNonTrovata("Luogo non trovato"));
	}

	@GetMapping("/inverso")
	public Indirizzo inverso(
			@RequestParam @DecimalMin("-90") @DecimalMax("90") double latitudine,
			@RequestParam @DecimalMin("-180") @DecimalMax("180") double longitudine) {
		return servizio.indirizzo(latitudine, longitudine)
				.orElseThrow(() -> new RisorsaNonTrovata("Nessun indirizzo per queste coordinate"));
	}
}
