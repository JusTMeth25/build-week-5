package it.epicode.eventi.notifica.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RichiestaMessaggioPartecipanti(@NotBlank @Size(max = 500) String messaggio) {
}
