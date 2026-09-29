package it.epicode.eventi.utente;

import it.epicode.eventi.comune.eccezioni.Conflitto;
import it.epicode.eventi.comune.eccezioni.RichiestaNonValida;
import it.epicode.eventi.comune.eccezioni.RisorsaNonTrovata;
import it.epicode.eventi.mail.ServizioMail;
import it.epicode.eventi.utente.web.RichiestaAggiornamentoProfilo;
import it.epicode.eventi.utente.web.RichiestaRegistrazione;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
public class ServizioUtenti {

	private static final SecureRandom CASUALE = new SecureRandom();

	private final UtenteRepository utenti;
	private final CodiceVerificaRepository codici;
	private final PasswordEncoder cifratore;
	private final ServizioMail mail;
	private final Duration durataCodice;

	public ServizioUtenti(UtenteRepository utenti,
			CodiceVerificaRepository codici,
			PasswordEncoder cifratore,
			ServizioMail mail,
			@Value("${app.verifica.durata-codice-minuti}") long durataCodiceMinuti) {
		this.utenti = utenti;
		this.codici = codici;
		this.cifratore = cifratore;
		this.mail = mail;
		this.durataCodice = Duration.ofMinutes(durataCodiceMinuti);
	}

	@Transactional
	public Utente registra(RichiestaRegistrazione richiesta) {
		String email = normalizzaEmail(richiesta.email());
		if (utenti.existsByEmailIgnoreCase(email)) {
			throw new Conflitto("Esiste gia' un account con questa email");
		}

		Utente utente = new Utente();
		utente.setEmail(email);
		utente.setPasswordHash(cifratore.encode(richiesta.password()));
		utente.setNome(richiesta.nome());
		utente.setCognome(richiesta.cognome());
		utente.setIndirizzo(richiesta.indirizzo());
		utente.setDataNascita(richiesta.dataNascita());
		utente.setTelefono(richiesta.telefono());
		utente.setRuolo(Ruolo.UTENTE);
		utenti.save(utente);

		inviaNuovoCodice(utente);
		return utente;
	}

	@Transactional
	public void reinviaCodice(String emailRichiesta) {
		Utente utente = trovaPerEmail(emailRichiesta);
		if (utente.isVerificato()) {
			throw new Conflitto("L'account e' gia' verificato");
		}
		inviaNuovoCodice(utente);
	}

	@Transactional
	public void verifica(String emailRichiesta, String codice) {
		Utente utente = trovaPerEmail(emailRichiesta);
		if (utente.isVerificato()) {
			throw new Conflitto("L'account e' gia' verificato");
		}

		CodiceVerifica trovato = codici
				.findFirstByUtenteAndCodiceAndUsatoFalseOrderByIdDesc(utente, codice)
				.orElseThrow(() -> new RichiestaNonValida("Codice non valido"));

		if (trovato.isScaduto()) {
			throw new RichiestaNonValida("Codice scaduto, richiedine uno nuovo");
		}

		trovato.segnaUsato();
		utente.setVerificato(true);
	}

	@Transactional
	public Utente aggiornaProfilo(Utente utente, RichiestaAggiornamentoProfilo richiesta) {
		utente.setNome(richiesta.nome());
		utente.setCognome(richiesta.cognome());
		utente.setIndirizzo(richiesta.indirizzo());
		utente.setDataNascita(richiesta.dataNascita());
		utente.setTelefono(richiesta.telefono());
		utente.setLatitudine(richiesta.latitudine());
		utente.setLongitudine(richiesta.longitudine());
		return utenti.save(utente);
	}

	@Transactional(readOnly = true)
	public Utente trovaPerId(Long id) {
		return utenti.findById(id)
				.orElseThrow(() -> new RisorsaNonTrovata("Utente non trovato"));
	}

	private Utente trovaPerEmail(String email) {
		return utenti.findByEmailIgnoreCase(normalizzaEmail(email))
				.orElseThrow(() -> new RisorsaNonTrovata("Nessun account con questa email"));
	}

	private void inviaNuovoCodice(Utente utente) {
		codici.invalidaPrecedenti(utente);
		String codice = "%06d".formatted(CASUALE.nextInt(1_000_000));
		codici.save(new CodiceVerifica(utente, codice, Instant.now().plus(durataCodice)));
		mail.inviaCodiceVerifica(utente.getEmail(), utente.getNome(), codice);
	}

	private String normalizzaEmail(String email) {
		return email.trim().toLowerCase();
	}
}
