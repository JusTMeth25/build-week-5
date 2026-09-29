package it.epicode.eventi.utente.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

public record RichiestaLogin(
		@NotBlank @Email String email,

		@JsonDeserialize(using = StringDeserializer.class)
		@NotBlank String password) {
}
