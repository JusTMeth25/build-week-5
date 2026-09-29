package it.epicode.eventi.amicizia.web;

import it.epicode.eventi.amicizia.StatoAmicizia;

public record RispostaPartecipante(
		Long id,
		String nome,
		String cognome,
		StatoAmicizia statoAmicizia,
		boolean richiestaInviataDaMe) {
}
