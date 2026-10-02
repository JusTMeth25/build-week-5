package it.epicode.eventi.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Render (piano gratuito) blocca le porte SMTP: le email partono via API HTTP di Mailjet.
@Service
@ConditionalOnProperty(name = "app.mail.abilitato", havingValue = "true")
public class ServizioMailMailjet implements ServizioMail {

	private static final Logger log = LoggerFactory.getLogger(ServizioMailMailjet.class);

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter
			.ofPattern("EEEE d MMMM yyyy 'alle' HH:mm", Locale.ITALIAN)
			.withZone(ZoneId.of("Europe/Rome"));

	private final RestClient clienteMailjet;
	private final ITemplateEngine motore;
	private final String mittente;
	private final String nomeMittente;
	private final String urlApplicazione;

	public ServizioMailMailjet(ITemplateEngine motore,
			@Value("${app.mail.mailjet.api-key}") String chiave,
			@Value("${app.mail.mailjet.secret-key}") String segreto,
			@Value("${app.mail.mittente}") String mittente,
			@Value("${app.mail.nome-mittente}") String nomeMittente,
			@Value("${app.mail.url-applicazione}") String urlApplicazione) {
		// Senza chiavi l'app parte lo stesso (comodo in locale), ma ogni invio fallira'.
		if (chiave.isBlank() || segreto.isBlank()) {
			log.warn("MAILJET_API_KEY o MAILJET_SECRET_KEY non impostate: le email non partiranno");
		}
		this.clienteMailjet = RestClient.builder()
				.baseUrl("https://api.mailjet.com/v3.1")
				.defaultHeaders(intestazioni -> intestazioni.setBasicAuth(chiave, segreto))
				.build();
		this.motore = motore;
		this.mittente = mittente;
		this.nomeMittente = nomeMittente;
		this.urlApplicazione = urlApplicazione.replaceAll("/+$", "");
	}

	@Override
	@Async
	public void inviaCodiceVerifica(String destinatario, String nome, String codice) {
		Context contesto = contesto();
		contesto.setVariable("nome", nome);
		contesto.setVariable("codice", codice);
		spedisci(destinatario, "Conferma il tuo account", "mail/verifica", contesto);
	}

	@Override
	@Async
	public void inviaResetPassword(String destinatario, String nome, String token) {
		Context contesto = contesto();
		contesto.setVariable("nome", nome);
		contesto.setVariable("linkReset", urlApplicazione + "/reimposta-password?token=" + token);
		spedisci(destinatario, "Reimposta la tua password", "mail/reset-password", contesto);
	}

	@Override
	@Async
	public void inviaTicket(String destinatario, DatiTicket ticket) {
		Context contesto = contesto();
		contesto.setVariable("ticket", ticket);
		contesto.setVariable("data", FORMATO_DATA.format(ticket.dataEvento()));
		spedisci(destinatario, "Il tuo ticket per " + ticket.nomeEvento(), "mail/ticket", contesto);
	}

	@Override
	@Async
	public void avvisaProprietarioNuovaIscrizione(String destinatario, String nomeProprietario,
			String nomeEvento, String nomePartecipante) {
		Context contesto = contesto();
		contesto.setVariable("nomeProprietario", nomeProprietario);
		contesto.setVariable("nomeEvento", nomeEvento);
		contesto.setVariable("nomePartecipante", nomePartecipante);
		spedisci(destinatario, "Nuova iscrizione a " + nomeEvento, "mail/nuova-iscrizione", contesto);
	}

	private Context contesto() {
		Context contesto = new Context(Locale.ITALIAN);
		contesto.setVariable("urlApplicazione", urlApplicazione);
		return contesto;
	}

	private void spedisci(String destinatario, String oggetto, String template, Context contesto) {
		// Formato della Send API v3.1: https://dev.mailjet.com/email/guides/send-api-v31/
		Map<String, Object> messaggio = Map.of(
				"From", Map.of("Email", mittente, "Name", nomeMittente),
				"To", List.of(Map.of("Email", destinatario)),
				"Subject", oggetto,
				"HTMLPart", motore.process(template, contesto));
		try {
			clienteMailjet.post()
					.uri("/send")
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("Messages", List.of(messaggio)))
					.retrieve()
					.toBodilessEntity();
			log.info("Email \"{}\" inviata a {}", oggetto, destinatario);
		} catch (RestClientException eccezione) {
			log.error("Invio di \"{}\" a {} non riuscito: {}", oggetto, destinatario,
					eccezione.getMessage());
		}
	}
}
