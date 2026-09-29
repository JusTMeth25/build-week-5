package it.epicode.eventi.evento.web;

import jakarta.validation.constraints.Size;

public record RichiestaMiglioramentoDescrizione(@Size(max = 5000) String descrizione) {
}
