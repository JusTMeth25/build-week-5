package it.epicode.eventi.evento.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record RichiestaEvento(
		@NotBlank @Size(max = 140) String titolo,
		@Size(max = 5000) String descrizione,
		@NotNull @Future Instant dataEvento,
		@NotBlank @Size(max = 140) String luogo,
		@Size(max = 255) String indirizzo,
		@DecimalMin("-90.0") @DecimalMax("90.0") Double latitudine,
		@DecimalMin("-180.0") @DecimalMax("180.0") Double longitudine,
		@Positive Integer capienza,
		@Size(max = 30) String genere,
		@Size(max = 30) List<@Size(max = 120) String> artisti,
		@Size(max = 10) List<@Valid RichiestaImmagine> immagini,
		@Size(max = 50) List<@Valid RichiestaMarker> marker) {
}
