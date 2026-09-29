package it.epicode.eventi.evento.web;

import it.epicode.eventi.evento.MarkerEvento;
import it.epicode.eventi.evento.TipoMarker;

public record RispostaMarker(
		Long id,
		TipoMarker tipo,
		String etichetta,
		Double latitudine,
		Double longitudine) {

	public static RispostaMarker da(MarkerEvento marker) {
		return new RispostaMarker(
				marker.getId(),
				marker.getTipo(),
				marker.getEtichetta(),
				marker.getLatitudine(),
				marker.getLongitudine());
	}
}
