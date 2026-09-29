package it.epicode.eventi.mappa.web;

import it.epicode.eventi.evento.SintesiEvento;
import it.epicode.eventi.evento.SintesiEventoVicino;

import java.time.Instant;

public record RispostaEventoMappa(
		Long id,
		String titolo,
		Instant dataEvento,
		String luogo,
		Double latitudine,
		Double longitudine,
		String immaginePrincipale,
		Double distanzaKm) {

	public static RispostaEventoMappa da(SintesiEvento sintesi) {
		return new RispostaEventoMappa(
				sintesi.getId(),
				sintesi.getTitolo(),
				sintesi.getDataEvento(),
				sintesi.getLuogo(),
				sintesi.getLatitudine(),
				sintesi.getLongitudine(),
				sintesi.getImmaginePrincipale(),
				null);
	}

	public static RispostaEventoMappa conDistanza(SintesiEventoVicino sintesi) {
		return new RispostaEventoMappa(
				sintesi.getId(),
				sintesi.getTitolo(),
				sintesi.getDataEvento(),
				sintesi.getLuogo(),
				sintesi.getLatitudine(),
				sintesi.getLongitudine(),
				sintesi.getImmaginePrincipale(),
				arrotonda(sintesi.getDistanzaKm()));
	}

	private static Double arrotonda(Double distanza) {
		return distanza == null ? null : Math.round(distanza * 10.0) / 10.0;
	}
}
