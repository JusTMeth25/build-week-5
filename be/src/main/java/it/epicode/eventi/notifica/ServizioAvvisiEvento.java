package it.epicode.eventi.notifica;

import it.epicode.eventi.comune.eccezioni.Conflitto;
import it.epicode.eventi.evento.Evento;
import it.epicode.eventi.evento.ServizioEventi;
import it.epicode.eventi.ticket.ServizioTicket;
import it.epicode.eventi.utente.Utente;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServizioAvvisiEvento {

	private final ServizioEventi servizioEventi;
	private final ServizioTicket servizioTicket;
	private final ServizioNotifiche servizioNotifiche;

	public ServizioAvvisiEvento(ServizioEventi servizioEventi,
			ServizioTicket servizioTicket,
			ServizioNotifiche servizioNotifiche) {
		this.servizioEventi = servizioEventi;
		this.servizioTicket = servizioTicket;
		this.servizioNotifiche = servizioNotifiche;
	}

	@Transactional
	public int avvisaPartecipanti(Long eventoId, String messaggio, Utente richiedente) {
		Evento evento = servizioEventi.caricaPerModifica(eventoId, richiedente);
		List<Long> destinatari = servizioTicket.idPartecipanti(eventoId);
		if (destinatari.isEmpty()) {
			throw new Conflitto("Nessun partecipante da avvisare");
		}
		servizioNotifiche.inviaATutti(destinatari, evento, TipoNotifica.MESSAGGIO_PROPRIETARIO,
				messaggio);
		return destinatari.size();
	}
}
