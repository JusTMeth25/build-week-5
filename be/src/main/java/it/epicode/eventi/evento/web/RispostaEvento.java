package it.epicode.eventi.evento.web;

import it.epicode.eventi.evento.Evento;
import it.epicode.eventi.utente.web.RispostaUtentePubblico;

import java.time.Instant;
import java.util.List;

public record RispostaEvento(
		Long id,
		String titolo,
		String descrizione,
		Instant dataEvento,
		String luogo,
		String indirizzo,
		Double latitudine,
		Double longitudine,
		Integer capienza,
		String genere,
		RispostaUtentePubblico proprietario,
		List<RispostaArtista> artisti,
		List<RispostaImmagine> immagini,
		List<RispostaMarker> marker,
		Instant creatoIl,
		Instant aggiornatoIl) {

	public static RispostaEvento da(Evento evento) {
		return new RispostaEvento(
				evento.getId(),
				evento.getTitolo(),
				evento.getDescrizione(),
				evento.getDataEvento(),
				evento.getLuogo(),
				evento.getIndirizzo(),
				evento.getLatitudine(),
				evento.getLongitudine(),
				evento.getCapienza(),
				evento.getGenere(),
				RispostaUtentePubblico.da(evento.getProprietario()),
				evento.getArtisti().stream().map(RispostaArtista::da).toList(),
				evento.getImmagini().stream().map(RispostaImmagine::da).toList(),
				evento.getMarker().stream().map(RispostaMarker::da).toList(),
				evento.getCreatoIl(),
				evento.getAggiornatoIl());
	}
}
