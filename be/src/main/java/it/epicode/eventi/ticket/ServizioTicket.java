package it.epicode.eventi.ticket;

import it.epicode.eventi.comune.eccezioni.Conflitto;
import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import it.epicode.eventi.comune.eccezioni.RisorsaNonTrovata;
import it.epicode.eventi.evento.Evento;
import it.epicode.eventi.evento.ServizioEventi;
import it.epicode.eventi.mail.DatiTicket;
import it.epicode.eventi.mail.ServizioMail;
import it.epicode.eventi.notifica.ServizioNotifiche;
import it.epicode.eventi.notifica.TipoNotifica;
import it.epicode.eventi.ticket.web.RispostaTicket;
import it.epicode.eventi.utente.Utente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class ServizioTicket {

	private final TicketRepository ticket;
	private final ServizioEventi servizioEventi;
	private final ServizioMail mail;
	private final ServizioNotifiche notifiche;

	public ServizioTicket(TicketRepository ticket,
			ServizioEventi servizioEventi,
			ServizioMail mail,
			ServizioNotifiche notifiche) {
		this.ticket = ticket;
		this.servizioEventi = servizioEventi;
		this.mail = mail;
		this.notifiche = notifiche;
	}

	@Transactional
	public RispostaTicket iscrivi(Long eventoId, Utente partecipante) {
		Evento evento = servizioEventi.carica(eventoId);

		if (evento.appartieneA(partecipante)) {
			throw new Conflitto("Il proprietario dell'evento non ha bisogno di un ticket");
		}
		if (evento.getDataEvento().isBefore(Instant.now())) {
			throw new Conflitto("L'evento si e' gia' svolto");
		}
		if (ticket.existsByEventoIdAndPartecipanteIdAndAnnullatoFalse(eventoId, partecipante.getId())) {
			throw new Conflitto("Hai gia' un ticket per questo evento");
		}
		if (evento.getCapienza() != null
				&& ticket.countByEventoIdAndAnnullatoFalse(eventoId) >= evento.getCapienza()) {
			throw new Conflitto("L'evento ha raggiunto la capienza massima");
		}

		Ticket emesso = ticket.save(new Ticket(evento, partecipante));

		mail.inviaTicket(partecipante.getEmail(), new DatiTicket(
				emesso.getCodice(),
				emesso.getNomeEvento(),
				emesso.getDataEvento(),
				emesso.getLuogoEvento(),
				emesso.getNomePartecipante()));

		Utente proprietario = evento.getProprietario();
		mail.avvisaProprietarioNuovaIscrizione(
				proprietario.getEmail(),
				proprietario.getNome(),
				evento.getTitolo(),
				partecipante.getNomeCompleto());
		notifiche.invia(proprietario, evento, TipoNotifica.NUOVA_ISCRIZIONE,
				"%s si e' iscritto a \"%s\"".formatted(
						partecipante.getNomeCompleto(), evento.getTitolo()));

		return RispostaTicket.da(emesso);
	}

	@Transactional
	public void annulla(Long ticketId, Utente richiedente) {
		Ticket trovato = ticket.findById(ticketId)
				.orElseThrow(() -> new RisorsaNonTrovata("Ticket non trovato"));
		if (!trovato.getPartecipante().getId().equals(richiedente.getId())) {
			throw new OperazioneNonConsentita("Il ticket non e' tuo");
		}
		trovato.annulla();
	}

	@Transactional(readOnly = true)
	public List<RispostaTicket> miei(Utente partecipante) {
		return ticket.deiPartecipante(partecipante.getId()).stream()
				.map(RispostaTicket::da)
				.toList();
	}

	@Transactional(readOnly = true)
	public boolean possiedeTicket(Long eventoId, Utente utente) {
		return ticket.existsByEventoIdAndPartecipanteIdAndAnnullatoFalse(eventoId, utente.getId());
	}

	@Transactional(readOnly = true)
	public List<Long> idPartecipanti(Long eventoId) {
		return ticket.idPartecipanti(eventoId);
	}
}
