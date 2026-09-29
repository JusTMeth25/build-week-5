package it.epicode.eventi.utente;

import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import it.epicode.eventi.comune.eccezioni.RichiestaNonValida;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class ServizioAutenticazione {

	private final AuthenticationManager gestoreAutenticazione;
	private final UtenteRepository utenti;
	private final ServizioJwt jwt;
	private final int tentativiMassimi;
	private final Duration duratabBlocco;

	public ServizioAutenticazione(AuthenticationManager gestoreAutenticazione,
			UtenteRepository utenti,
			ServizioJwt jwt,
			@Value("${app.sicurezza.tentativi-massimi}") int tentativiMassimi,
			@Value("${app.sicurezza.blocco-minuti}") long bloccoMinuti) {
		this.gestoreAutenticazione = gestoreAutenticazione;
		this.utenti = utenti;
		this.jwt = jwt;
		this.tentativiMassimi = tentativiMassimi;
		this.duratabBlocco = Duration.ofMinutes(bloccoMinuti);
	}

	@Transactional
	public RispostaAccesso accedi(String email, String password) {
		String emailNormalizzata = email.trim().toLowerCase();

		Authentication autenticazione;
		try {
			autenticazione = gestoreAutenticazione.authenticate(
					new UsernamePasswordAuthenticationToken(emailNormalizzata, password));
		} catch (DisabledException eccezione) {
			throw new OperazioneNonConsentita(
					"L'account non e' ancora verificato: inserisci il codice ricevuto per email");
		} catch (LockedException eccezione) {
			throw new OperazioneNonConsentita("Troppi tentativi falliti: riprova tra qualche minuto");
		} catch (BadCredentialsException eccezione) {
			registraTentativoFallito(emailNormalizzata);
			throw new RichiestaNonValida("Email o password non corretti");
		}

		Utente utente = ((UtenteAutenticato) autenticazione.getPrincipal()).getUtente();
		azzeraTentativi(utente);
		return new RispostaAccesso(jwt.genera(utente), utente);
	}

	private void registraTentativoFallito(String email) {
		utenti.findByEmailIgnoreCase(email).ifPresent(utente -> {
			utente.setTentativiFalliti(utente.getTentativiFalliti() + 1);
			if (utente.getTentativiFalliti() >= tentativiMassimi) {
				utente.setBloccatoFinoIl(Instant.now().plus(duratabBlocco));
			}
			utenti.save(utente);
		});
	}

	private void azzeraTentativi(Utente utente) {
		if (utente.getTentativiFalliti() > 0 || utente.getBloccatoFinoIl() != null) {
			utente.setTentativiFalliti(0);
			utente.setBloccatoFinoIl(null);
			utenti.save(utente);
		}
	}

	public record RispostaAccesso(String token, Utente utente) {
	}
}
