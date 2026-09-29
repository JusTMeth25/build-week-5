package it.epicode.eventi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import it.epicode.eventi.utente.ServizioJwt;
import it.epicode.eventi.utente.Utente;
import it.epicode.eventi.utente.UtenteAutenticato;
import it.epicode.eventi.utente.UtenteRepository;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Il browser non puo' impostare intestazioni HTTP custom sull'handshake WebSocket,
 * quindi il token JWT arriva nell'intestazione nativa "Authorization" del frame STOMP CONNECT.
 */
@Component
public class InterceptorAutenticazioneWebSocket implements ChannelInterceptor {

	private static final String PREFISSO = "Bearer ";

	private final ServizioJwt jwt;
	private final UtenteRepository utenti;

	public InterceptorAutenticazioneWebSocket(ServizioJwt jwt, UtenteRepository utenti) {
		this.jwt = jwt;
		this.utenti = utenti;
	}

	@Override
	public Message<?> preSend(@NonNull Message<?> messaggio, @NonNull MessageChannel canale) {
		StompHeaderAccessor accessore = StompHeaderAccessor.wrap(messaggio);
		if (StompCommand.CONNECT.equals(accessore.getCommand())) {
			accessore.setUser(autentica(accessore.getFirstNativeHeader("Authorization")));
		}
		return messaggio;
	}

	private UsernamePasswordAuthenticationToken autentica(String intestazione) {
		String token = estraiToken(intestazione);
		if (token == null) {
			throw new MessagingException("Token JWT mancante nella connessione WebSocket");
		}
		try {
			Claims rivendicazioni = jwt.valida(token);
			Long id = rivendicazioni.get("id", Number.class).longValue();
			Utente utente = utenti.findById(id)
					.filter(u -> u.isAttivo() && u.isVerificato() && !u.isBloccatoPerTentativi())
					.orElseThrow(() -> new MessagingException("Utente non valido o non piu' attivo"));
			UtenteAutenticato dettagli = new UtenteAutenticato(utente);
			return new UsernamePasswordAuthenticationToken(dettagli, null, dettagli.getAuthorities());
		} catch (JwtException | IllegalArgumentException eccezione) {
			throw new MessagingException("Token JWT non valido", eccezione);
		}
	}

	private String estraiToken(String intestazione) {
		if (intestazione != null && intestazione.startsWith(PREFISSO)) {
			return intestazione.substring(PREFISSO.length());
		}
		return null;
	}
}
