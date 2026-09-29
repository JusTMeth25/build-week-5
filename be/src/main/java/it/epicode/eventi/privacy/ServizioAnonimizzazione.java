package it.epicode.eventi.privacy;

import it.epicode.eventi.chat.MessaggioRepository;
import it.epicode.eventi.comune.eccezioni.RichiestaNonValida;
import it.epicode.eventi.ticket.TicketRepository;
import it.epicode.eventi.utente.Utente;
import it.epicode.eventi.utente.UtenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ServizioAnonimizzazione {

	private static final Logger log = LoggerFactory.getLogger(ServizioAnonimizzazione.class);
	private static final SecureRandom CASUALE = new SecureRandom();
	private static final String NOME_RIMOSSO = "Utente";
	private static final String COGNOME_RIMOSSO = "rimosso";

	private final UtenteRepository utenti;
	private final TicketRepository ticket;
	private final MessaggioRepository messaggi;
	private final PasswordEncoder cifratore;

	public ServizioAnonimizzazione(UtenteRepository utenti,
			TicketRepository ticket,
			MessaggioRepository messaggi,
			PasswordEncoder cifratore) {
		this.utenti = utenti;
		this.ticket = ticket;
		this.messaggi = messaggi;
		this.cifratore = cifratore;
	}

	@Transactional
	public void anonimizza(Utente utente, String password, boolean confermato) {
		if (!confermato) {
			throw new RichiestaNonValida(
					"L'anonimizzazione va confermata: comporta la disattivazione dell'account");
		}
		if (!cifratore.matches(password, utente.getPasswordHash())) {
			throw new RichiestaNonValida("Password non corretta");
		}

		Long id = utente.getId();
		ticket.anonimizzaPartecipante(id, NOME_RIMOSSO + " " + COGNOME_RIMOSSO);
		messaggi.rimuoviContenutiDi(id);

		utente.setEmail("rimosso-%d@anonimo.invalid".formatted(id));
		utente.setPasswordHash(cifratore.encode(passwordInutilizzabile()));
		utente.setNome(NOME_RIMOSSO);
		utente.setCognome(COGNOME_RIMOSSO);
		utente.setIndirizzo(null);
		utente.setTelefono(null);
		utente.setDataNascita(null);
		utente.setLatitudine(null);
		utente.setLongitudine(null);
		utente.setAnonimizzato(true);
		utente.setAttivo(false);
		utenti.save(utente);

		log.info("Anonimizzazione completata per l'utente {}", id);
	}

	private String passwordInutilizzabile() {
		byte[] casuale = new byte[48];
		CASUALE.nextBytes(casuale);
		return Base64.getEncoder().encodeToString(casuale);
	}
}
