package it.epicode.eventi.evento.web;

import it.epicode.eventi.evento.TipoMarker;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RichiestaMarker(
		@NotNull TipoMarker tipo,
		@Size(max = 80) String etichetta,
		@NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitudine,
		@NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitudine) {
}
