package it.epicode.eventi.notifica;

import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import it.epicode.eventi.comune.eccezioni.RisorsaNonTrovata;
import it.epicode.eventi.evento.Evento;
import it.epicode.eventi.notifica.web.RispostaNotifica;
import it.epicode.eventi.utente.Utente;
import it.epicode.eventi.utente.UtenteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServizioNotifiche {

	private static final String CODA_NOTIFICHE = "/queue/notifiche";

	private final NotificaRepository notifiche;
	private final UtenteRepository utenti;
	private final SimpMessagingTemplate canale;

	public ServizioNotifiche(NotificaRepository notifiche,
			UtenteRepository utenti,
			SimpMessagingTemplate canale) {
		this.notifiche = notifiche;
		this.utenti = utenti;
		this.canale = canale;
	}

	@Transactional
	public void invia(Utente destinatario, Evento evento, TipoNotifica tipo, String messaggio) {
		Notifica notifica = notifiche.save(new Notifica(destinatario, evento, tipo, messaggio));
		canale.convertAndSendToUser(destinatario.getEmail(), CODA_NOTIFICHE,
				RispostaNotifica.da(notifica));
	}

	@Transactional
	public void inviaATutti(List<Long> idDestinatari, Evento evento, TipoNotifica tipo,
			String messaggio) {
		utenti.findAllById(idDestinatari)
				.forEach(destinatario -> invia(destinatario, evento, tipo, messaggio));
	}

	@Transactional(readOnly = true)
	public Page<RispostaNotifica> elenco(Utente destinatario, Pageable pagina) {
		return notifiche.findByDestinatarioIdOrderByCreataIlDesc(destinatario.getId(), pagina)
				.map(RispostaNotifica::da);
	}

	@Transactional(readOnly = true)
	public long contaNonLette(Utente destinatario) {
		return notifiche.countByDestinatarioIdAndLettaFalse(destinatario.getId());
	}

	@Transactional
	public void segnaLetta(Long id, Utente destinatario) {
		Notifica notifica = notifiche.findById(id)
				.orElseThrow(() -> new RisorsaNonTrovata("Notifica non trovata"));
		if (!notifica.getDestinatario().getId().equals(destinatario.getId())) {
			throw new OperazioneNonConsentita("La notifica non e' tua");
		}
		notifica.segnaLetta();
	}

	@Transactional
	public void segnaTutteLette(Utente destinatario) {
		notifiche.segnaTutteLette(destinatario.getId());
	}
}
