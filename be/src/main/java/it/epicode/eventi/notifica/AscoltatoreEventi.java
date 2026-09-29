package it.epicode.eventi.notifica;

import it.epicode.eventi.evento.EventoAggiornato;
import it.epicode.eventi.evento.EventoRepository;
import it.epicode.eventi.ticket.TicketRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class AscoltatoreEventi {

	private final TicketRepository ticket;
	private final EventoRepository eventi;
	private final ServizioNotifiche servizioNotifiche;

	public AscoltatoreEventi(TicketRepository ticket,
			EventoRepository eventi,
			ServizioNotifiche servizioNotifiche) {
		this.ticket = ticket;
		this.eventi = eventi;
		this.servizioNotifiche = servizioNotifiche;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void eventoAggiornato(EventoAggiornato segnale) {
		List<Long> destinatari = ticket.idPartecipanti(segnale.eventoId());
		if (destinatari.isEmpty()) {
			return;
		}
		eventi.findById(segnale.eventoId()).ifPresent(evento -> servizioNotifiche.inviaATutti(
				destinatari, evento, TipoNotifica.EVENTO_MODIFICATO, segnale.riepilogo()));
	}
}
