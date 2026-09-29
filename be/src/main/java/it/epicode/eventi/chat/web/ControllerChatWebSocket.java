package it.epicode.eventi.chat.web;

import it.epicode.eventi.chat.ServizioChat;
import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import it.epicode.eventi.utente.Utente;
import it.epicode.eventi.utente.UtenteAutenticato;
import it.epicode.eventi.utente.UtenteRepository;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class ControllerChatWebSocket {

	private final ServizioChat servizio;
	private final UtenteRepository utenti;

	public ControllerChatWebSocket(ServizioChat servizio, UtenteRepository utenti) {
		this.servizio = servizio;
		this.utenti = utenti;
	}

	@MessageMapping("/chat")
	public void invia(@Valid @Payload RichiestaMessaggio richiesta, Principal principale) {
		servizio.invia(richiesta.destinatarioId(), richiesta.contenuto(), mittente(principale));
	}

	@MessageExceptionHandler
	@SendToUser(destinations = "/queue/errori", broadcast = false)
	public Map<String, String> errore(Exception eccezione) {
		return Map.of("errore", eccezione.getMessage());
	}

	private Utente mittente(Principal principale) {
		if (principale instanceof Authentication autenticazione
				&& autenticazione.getPrincipal() instanceof UtenteAutenticato dettagli) {
			return utenti.findById(dettagli.getId())
					.orElseThrow(() -> new OperazioneNonConsentita("Sessione non piu' valida"));
		}
		throw new OperazioneNonConsentita("Serve una sessione autenticata");
	}
}
