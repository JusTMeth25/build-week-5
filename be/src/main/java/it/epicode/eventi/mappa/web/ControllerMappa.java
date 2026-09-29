package it.epicode.eventi.mappa.web;

import it.epicode.eventi.mappa.ServizioMappa;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mappa")
@Validated
public class ControllerMappa {

	private final ServizioMappa servizio;

	public ControllerMappa(ServizioMappa servizio) {
		this.servizio = servizio;
	}

	@GetMapping("/eventi")
	public List<RispostaEventoMappa> eventi(
			@RequestParam(required = false) @DecimalMin("-90.0") @DecimalMax("90.0") Double latitudine,
			@RequestParam(required = false) @DecimalMin("-180.0") @DecimalMax("180.0") Double longitudine,
			@RequestParam(defaultValue = "100") @Min(1) @Max(500) int massimo) {
		return servizio.eventi(Optional.ofNullable(latitudine), Optional.ofNullable(longitudine),
				massimo);
	}

	@GetMapping("/eventi/{id}")
	public RispostaAnteprimaEvento anteprima(@PathVariable Long id) {
		return servizio.anteprima(id);
	}
}
