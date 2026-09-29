package it.epicode.eventi.utente.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RichiestaVerifica(
		@NotBlank @Email String email,
		@NotBlank @Pattern(regexp = "\\d{6}", message = "il codice e' composto da sei cifre") String codice) {
}
