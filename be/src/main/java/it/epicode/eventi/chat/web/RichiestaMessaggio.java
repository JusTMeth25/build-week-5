package it.epicode.eventi.chat.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RichiestaMessaggio(
		@NotNull Long destinatarioId,
		@NotBlank @Size(max = 2000) String contenuto) {
}
