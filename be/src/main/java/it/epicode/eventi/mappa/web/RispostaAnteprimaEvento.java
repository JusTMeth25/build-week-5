package it.epicode.eventi.mappa.web;

import it.epicode.eventi.evento.Artista;
import it.epicode.eventi.evento.Evento;
import it.epicode.eventi.evento.ImmagineEvento;
import it.epicode.eventi.evento.MarkerEvento;
import it.epicode.eventi.evento.TipoMarker;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record RispostaAnteprimaEvento(
		Long id,
		String titolo,
		Instant dataEvento,
		String luogo,
		String indirizzo,
		Double latitudine,
		Double longitudine,
		String immaginePrincipale,
		List<String> artisti,
		Map<TipoMarker, Long> marker,
		String collegamento) {

	public static RispostaAnteprimaEvento da(Evento evento) {
		return new RispostaAnteprimaEvento(
				evento.getId(),
				evento.getTitolo(),
				evento.getDataEvento(),
				evento.getLuogo(),
				evento.getIndirizzo(),
				evento.getLatitudine(),
				evento.getLongitudine(),
				evento.immaginePrincipale().map(ImmagineEvento::getUrl).orElse(null),
				evento.getArtisti().stream().map(Artista::getNome).toList(),
				evento.getMarker().stream().collect(Collectors.groupingBy(
						MarkerEvento::getTipo, Collectors.counting())),
				"/api/eventi/" + evento.getId());
	}
}
