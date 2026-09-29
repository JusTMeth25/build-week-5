package it.epicode.eventi.chat;

import it.epicode.eventi.amicizia.ServizioAmicizie;
import it.epicode.eventi.chat.web.RispostaMessaggio;
import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import it.epicode.eventi.comune.eccezioni.RichiestaNonValida;
import it.epicode.eventi.comune.eccezioni.RisorsaNonTrovata;
import it.epicode.eventi.utente.Utente;
import it.epicode.eventi.utente.UtenteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServizioChat {

	private static final String CODA_MESSAGGI = "/queue/messaggi";

	private final MessaggioRepository messaggi;
	private final UtenteRepository utenti;
	private final ServizioAmicizie amicizie;
	private final SimpMessagingTemplate canale;

	public ServizioChat(MessaggioRepository messaggi,
			UtenteRepository utenti,
			ServizioAmicizie amicizie,
			SimpMessagingTemplate canale) {
		this.messaggi = messaggi;
		this.utenti = utenti;
		this.amicizie = amicizie;
		this.canale = canale;
	}

	@Transactional
	public RispostaMessaggio invia(Long destinatarioId, String contenuto, Utente mittente) {
		if (destinatarioId.equals(mittente.getId())) {
			throw new RichiestaNonValida("Non puoi scrivere a te stesso");
		}
		Utente destinatario = utenti.findById(destinatarioId)
				.orElseThrow(() -> new RisorsaNonTrovata("Utente non trovato"));
		if (!amicizie.sonoAmici(mittente.getId(), destinatarioId)) {
			throw new OperazioneNonConsentita(
					"La chat e' disponibile solo fra utenti con amicizia accettata");
		}

		Messaggio salvato = messaggi.save(new Messaggio(mittente, destinatario, contenuto));
		RispostaMessaggio risposta = RispostaMessaggio.da(salvato);
		canale.convertAndSendToUser(destinatario.getEmail(), CODA_MESSAGGI, risposta);
		canale.convertAndSendToUser(mittente.getEmail(), CODA_MESSAGGI, risposta);
		return risposta;
	}

	@Transactional
	public Page<RispostaMessaggio> conversazione(Long altroId, Utente utente, Pageable pagina) {
		if (!amicizie.sonoAmici(utente.getId(), altroId)) {
			throw new OperazioneNonConsentita(
					"La chat e' disponibile solo fra utenti con amicizia accettata");
		}
		messaggi.segnaLetti(utente.getId(), altroId);
		return messaggi.conversazione(utente.getId(), altroId, pagina).map(RispostaMessaggio::da);
	}

	@Transactional(readOnly = true)
	public long contaNonLetti(Utente utente) {
		return messaggi.countByDestinatarioIdAndLettoIlIsNull(utente.getId());
	}
}
