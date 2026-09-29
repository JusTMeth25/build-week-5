package it.epicode.eventi.utente.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RichiestaAggiornamentoProfilo(
		@NotBlank @Size(max = 60) String nome,
		@NotBlank @Size(max = 60) String cognome,
		@Size(max = 255) String indirizzo,
		@Past LocalDate dataNascita,
		@Size(max = 30) String telefono,
		@DecimalMin("-90.0") @DecimalMax("90.0") Double latitudine,
		@DecimalMin("-180.0") @DecimalMax("180.0") Double longitudine) {
}
