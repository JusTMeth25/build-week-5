package it.epicode.eventi.amicizia;

import it.epicode.eventi.amicizia.web.RispostaPartecipante;
import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import it.epicode.eventi.evento.Evento;
import it.epicode.eventi.evento.ServizioEventi;
import it.epicode.eventi.ticket.Ticket;
import it.epicode.eventi.ticket.TicketRepository;
import it.epicode.eventi.utente.Utente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServizioPartecipanti {

	private final TicketRepository ticket;
	private final ServizioEventi servizioEventi;
	private final AmiciziaRepository amicizie;

	public ServizioPartecipanti(TicketRepository ticket,
			ServizioEventi servizioEventi,
			AmiciziaRepository amicizie) {
		this.ticket = ticket;
		this.servizioEventi = servizioEventi;
		this.amicizie = amicizie;
	}

	@Transactional(readOnly = true)
	public List<RispostaPartecipante> dellEvento(Long eventoId, Utente richiedente) {
		Evento evento = servizioEventi.carica(eventoId);
		boolean autorizzato = evento.appartieneA(richiedente)
				|| ticket.existsByEventoIdAndPartecipanteIdAndAnnullatoFalse(eventoId,
						richiedente.getId());
		if (!autorizzato) {
			throw new OperazioneNonConsentita(
					"L'elenco dei partecipanti e' visibile solo a chi possiede un ticket");
		}

		return ticket.dellEvento(eventoId).stream()
				.map(Ticket::getPartecipante)
				.filter(partecipante -> !partecipante.getId().equals(richiedente.getId()))
				.map(partecipante -> descrivi(partecipante, richiedente))
				.toList();
	}

	private RispostaPartecipante descrivi(Utente partecipante, Utente richiedente) {
		return amicizie.traUtenti(richiedente.getId(), partecipante.getId())
				.map(amicizia -> new RispostaPartecipante(
						partecipante.getId(),
						partecipante.getNome(),
						partecipante.getCognome(),
						amicizia.getStato(),
						amicizia.getRichiedente().getId().equals(richiedente.getId())))
				.orElseGet(() -> new RispostaPartecipante(
						partecipante.getId(),
						partecipante.getNome(),
						partecipante.getCognome(),
						null,
						false));
	}
}
