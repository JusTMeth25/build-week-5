package it.epicode.eventi.amicizia;

import it.epicode.eventi.amicizia.web.RispostaAmicizia;
import it.epicode.eventi.comune.eccezioni.Conflitto;
import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import it.epicode.eventi.comune.eccezioni.RichiestaNonValida;
import it.epicode.eventi.comune.eccezioni.RisorsaNonTrovata;
import it.epicode.eventi.notifica.ServizioNotifiche;
import it.epicode.eventi.notifica.TipoNotifica;
import it.epicode.eventi.ticket.TicketRepository;
import it.epicode.eventi.utente.Utente;
import it.epicode.eventi.utente.UtenteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServizioAmicizie {

	private final AmiciziaRepository amicizie;
	private final UtenteRepository utenti;
	private final TicketRepository ticket;
	private final ServizioNotifiche notifiche;

	public ServizioAmicizie(AmiciziaRepository amicizie,
			UtenteRepository utenti,
			TicketRepository ticket,
			ServizioNotifiche notifiche) {
		this.amicizie = amicizie;
		this.utenti = utenti;
		this.ticket = ticket;
		this.notifiche = notifiche;
	}

	@Transactional
	public RispostaAmicizia richiedi(Long destinatarioId, Utente richiedente) {
		if (destinatarioId.equals(richiedente.getId())) {
			throw new RichiestaNonValida("Non puoi chiedere l'amicizia a te stesso");
		}
		Utente destinatario = utenti.findById(destinatarioId)
				.orElseThrow(() -> new RisorsaNonTrovata("Utente non trovato"));
		if (!destinatario.isAttivo()) {
			throw new Conflitto("L'account non e' piu' attivo");
		}
		if (!ticket.condividonoUnEvento(richiedente.getId(), destinatarioId)) {
			throw new OperazioneNonConsentita(
					"L'amicizia si puo' chiedere solo a chi partecipa a un tuo stesso evento");
		}

		Amicizia amicizia = amicizie.traUtenti(richiedente.getId(), destinatarioId)
				.map(esistente -> riproponi(esistente, richiedente))
				.orElseGet(() -> amicizie.save(new Amicizia(richiedente, destinatario)));

		notifiche.invia(destinatario, null, TipoNotifica.RICHIESTA_AMICIZIA,
				"%s ti ha chiesto l'amicizia".formatted(richiedente.getNomeCompleto()));

		return RispostaAmicizia.da(amicizia);
	}

	@Transactional
	public RispostaAmicizia accetta(Long id, Utente destinatario) {
		Amicizia amicizia = caricaDaDecidere(id, destinatario);
		amicizia.accetta();
		notifiche.invia(amicizia.getRichiedente(), null, TipoNotifica.AMICIZIA_ACCETTATA,
				"%s ha accettato la tua richiesta di amicizia".formatted(destinatario.getNomeCompleto()));
		return RispostaAmicizia.da(amicizia);
	}

	@Transactional
	public RispostaAmicizia rifiuta(Long id, Utente destinatario) {
		Amicizia amicizia = caricaDaDecidere(id, destinatario);
		amicizia.rifiuta();
		return RispostaAmicizia.da(amicizia);
	}

	@Transactional(readOnly = true)
	public List<RispostaAmicizia> amici(Utente utente) {
		return amicizie.amiciDi(utente.getId()).stream()
				.map(amicizia -> RispostaAmicizia.amico(amicizia, utente))
				.toList();
	}

	@Transactional(readOnly = true)
	public List<RispostaAmicizia> richiesteRicevute(Utente utente) {
		return amicizie.richiesteRicevute(utente.getId()).stream()
				.map(RispostaAmicizia::da)
				.toList();
	}

	@Transactional(readOnly = true)
	public Optional<Amicizia> rapporto(Long primo, Long secondo) {
		return amicizie.traUtenti(primo, secondo);
	}

	@Transactional(readOnly = true)
	public boolean sonoAmici(Long primo, Long secondo) {
		return amicizie.amiciziaAccettata(primo, secondo);
	}

	private Amicizia riproponi(Amicizia esistente, Utente richiedente) {
		if (esistente.getStato() == StatoAmicizia.ACCETTATA) {
			throw new Conflitto("Siete gia' amici");
		}
		if (esistente.getStato() == StatoAmicizia.IN_ATTESA) {
			throw new Conflitto("La richiesta e' gia' in attesa di risposta");
		}
		if (!esistente.getRichiedente().getId().equals(richiedente.getId())) {
			throw new Conflitto("La richiesta precedente e' stata rifiutata");
		}
		esistente.riapri();
		return esistente;
	}

	private Amicizia caricaDaDecidere(Long id, Utente destinatario) {
		Amicizia amicizia = amicizie.findById(id)
				.orElseThrow(() -> new RisorsaNonTrovata("Richiesta non trovata"));
		if (!amicizia.getDestinatario().getId().equals(destinatario.getId())) {
			throw new OperazioneNonConsentita("Solo il destinatario puo' rispondere alla richiesta");
		}
		if (amicizia.getStato() != StatoAmicizia.IN_ATTESA) {
			throw new Conflitto("La richiesta e' gia' stata gestita");
		}
		return amicizia;
	}
}
