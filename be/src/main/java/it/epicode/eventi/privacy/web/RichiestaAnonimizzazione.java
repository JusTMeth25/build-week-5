package it.epicode.eventi.privacy.web;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

public record RichiestaAnonimizzazione(
		@JsonDeserialize(using = StringDeserializer.class)
		@NotBlank String password,

		boolean confermo) {
}
