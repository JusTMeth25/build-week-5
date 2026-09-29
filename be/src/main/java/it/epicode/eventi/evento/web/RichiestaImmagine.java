package it.epicode.eventi.evento.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RichiestaImmagine(
		@NotBlank
		@Size(max = 500)
		@Pattern(regexp = "^https://.+", message = "l'indirizzo dell'immagine deve iniziare con https://")
		String url,
		boolean principale) {
}
