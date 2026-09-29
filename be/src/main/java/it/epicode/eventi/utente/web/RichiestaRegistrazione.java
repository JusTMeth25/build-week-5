package it.epicode.eventi.utente.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;


public record RichiestaRegistrazione(
		@NotBlank @Email @Size(max = 255) String email,

		@JsonDeserialize(using = StringDeserializer.class)
		@NotBlank
		@Size(min = 10, max = 100, message = "la password deve avere almeno 10 caratteri")
		@Pattern(regexp = ".*[A-Za-z].*", message = "la password deve contenere almeno una lettera")
		@Pattern(regexp = ".*\\d.*", message = "la password deve contenere almeno una cifra")
		String password,

		@NotBlank @Size(max = 60) String nome,
		@NotBlank @Size(max = 60) String cognome,
		@Size(max = 255) String indirizzo,
		@Past LocalDate dataNascita,
		@Size(max = 30) String telefono) {
}
