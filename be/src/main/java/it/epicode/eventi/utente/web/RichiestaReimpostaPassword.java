package it.epicode.eventi.utente.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

public record RichiestaReimpostaPassword(
		@NotBlank String token,

		@JsonDeserialize(using = StringDeserializer.class)
		@NotBlank
		@Size(min = 10, max = 100, message = "la password deve avere almeno 10 caratteri")
		@Pattern(regexp = ".*[A-Za-z].*", message = "la password deve contenere almeno una lettera")
		@Pattern(regexp = ".*\\d.*", message = "la password deve contenere almeno una cifra")
		String password) {
}
