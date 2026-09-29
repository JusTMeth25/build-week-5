package it.epicode.eventi.utente;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class ServizioJwt {

	private static final int LUNGHEZZA_MINIMA_SEGRETO_BYTE = 32;

	private final SecretKey chiave;
	private final Duration scadenza;

	public ServizioJwt(@Value("${app.sicurezza.jwt.segreto}") String segreto,
			@Value("${app.sicurezza.jwt.scadenza-minuti}") long scadenzaMinuti) {
		if (segreto == null || segreto.isBlank()) {
			throw new IllegalStateException(
					"JWT_SECRET non impostata: obbligatoria per firmare i token di accesso");
		}
		byte[] segretoByte = segreto.getBytes(StandardCharsets.UTF_8);
		if (segretoByte.length < LUNGHEZZA_MINIMA_SEGRETO_BYTE) {
			throw new IllegalStateException(
					"JWT_SECRET troppo corta: servono almeno 32 byte (256 bit) per la firma HS256");
		}
		this.chiave = Keys.hmacShaKeyFor(segretoByte);
		this.scadenza = Duration.ofMinutes(scadenzaMinuti);
	}

	public String genera(Utente utente) {
		Instant adesso = Instant.now();
		return Jwts.builder()
				.subject(utente.getEmail())
				.claim("id", utente.getId())
				.claim("ruolo", utente.getRuolo().name())
				.issuedAt(Date.from(adesso))
				.expiration(Date.from(adesso.plus(scadenza)))
				.signWith(chiave)
				.compact();
	}

	/**
	 * Lancia {@link io.jsonwebtoken.JwtException} se firma, formato o scadenza non sono validi.
	 */
	public Claims valida(String token) {
		return Jwts.parser()
				.verifyWith(chiave)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
