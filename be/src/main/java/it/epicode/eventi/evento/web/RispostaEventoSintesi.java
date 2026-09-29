package it.epicode.eventi.evento.web;

import it.epicode.eventi.evento.SintesiEvento;

import java.time.Instant;

public record RispostaEventoSintesi(
		Long id,
		String titolo,
		Instant dataEvento,
		String luogo,
		Double latitudine,
		Double longitudine,
		String immaginePrincipale) {

	public static RispostaEventoSintesi da(SintesiEvento sintesi) {
		return new RispostaEventoSintesi(
				sintesi.getId(),
				sintesi.getTitolo(),
				sintesi.getDataEvento(),
				sintesi.getLuogo(),
				sintesi.getLatitudine(),
				sintesi.getLongitudine(),
				sintesi.getImmaginePrincipale());
	}
}
