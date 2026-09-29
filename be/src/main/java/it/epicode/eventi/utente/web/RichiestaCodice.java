package it.epicode.eventi.utente.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RichiestaCodice(@NotBlank @Email String email) {
}
