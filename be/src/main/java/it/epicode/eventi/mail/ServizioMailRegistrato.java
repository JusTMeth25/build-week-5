package it.epicode.eventi.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.abilitato", havingValue = "false", matchIfMissing = true)
public class ServizioMailRegistrato implements ServizioMail {

	private static final Logger log = LoggerFactory.getLogger(ServizioMailRegistrato.class);

	@Override
	public void inviaCodiceVerifica(String destinatario, String nome, String codice) {
		log.info("[mail non abilitata] codice di verifica per {}: {}", destinatario, codice);
	}

	@Override
	public void inviaTicket(String destinatario, DatiTicket ticket) {
		log.info("[mail non abilitata] ticket {} per {} all'evento {}",
				ticket.codice(), destinatario, ticket.nomeEvento());
	}

	@Override
	public void avvisaProprietarioNuovaIscrizione(String destinatario, String nomeProprietario,
			String nomeEvento, String nomePartecipante) {
		log.info("[mail non abilitata] avviso a {}: {} si e' iscritto a {}",
				destinatario, nomePartecipante, nomeEvento);
	}
}
