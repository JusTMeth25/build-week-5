package it.epicode.eventi.mail;

import java.time.Instant;

public record DatiTicket(
		String codice,
		String nomeEvento,
		Instant dataEvento,
		String luogo,
		String nomePartecipante) {
}
