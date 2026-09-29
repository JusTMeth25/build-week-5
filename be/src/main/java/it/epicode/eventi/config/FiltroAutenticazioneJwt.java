package it.epicode.eventi.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import it.epicode.eventi.utente.ServizioJwt;
import it.epicode.eventi.utente.Utente;
import it.epicode.eventi.utente.UtenteAutenticato;
import it.epicode.eventi.utente.UtenteRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class FiltroAutenticazioneJwt extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(FiltroAutenticazioneJwt.class);
	private static final String PREFISSO = "Bearer ";

	private final ServizioJwt jwt;
	private final UtenteRepository utenti;

	public FiltroAutenticazioneJwt(ServizioJwt jwt, UtenteRepository utenti) {
		this.jwt = jwt;
		this.utenti = utenti;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest richiesta, HttpServletResponse risposta,
			FilterChain catena) throws ServletException, IOException {
		estraiToken(richiesta).ifPresent(token -> autentica(token, richiesta));
		catena.doFilter(richiesta, risposta);
	}

	private Optional<String> estraiToken(HttpServletRequest richiesta) {
		String intestazione = richiesta.getHeader("Authorization");
		if (intestazione != null && intestazione.startsWith(PREFISSO)) {
			return Optional.of(intestazione.substring(PREFISSO.length()));
		}
		return Optional.empty();
	}

	private void autentica(String token, HttpServletRequest richiesta) {
		if (SecurityContextHolder.getContext().getAuthentication() != null) {
			return;
		}
		try {
			Claims rivendicazioni = jwt.valida(token);
			Long id = rivendicazioni.get("id", Number.class).longValue();
			utenti.findById(id).filter(this::attivabile).ifPresent(utente -> imposta(utente, richiesta));
		} catch (JwtException | IllegalArgumentException eccezione) {
			log.debug("Token JWT non valido: {}", eccezione.getMessage());
		}
	}

	private boolean attivabile(Utente utente) {
		return utente.isAttivo() && utente.isVerificato() && !utente.isBloccatoPerTentativi();
	}

	private void imposta(Utente utente, HttpServletRequest richiesta) {
		UtenteAutenticato dettagli = new UtenteAutenticato(utente);
		UsernamePasswordAuthenticationToken autenticazione = new UsernamePasswordAuthenticationToken(
				dettagli, null, dettagli.getAuthorities());
		autenticazione.setDetails(new WebAuthenticationDetailsSource().buildDetails(richiesta));
		SecurityContextHolder.getContext().setAuthentication(autenticazione);
	}
}
