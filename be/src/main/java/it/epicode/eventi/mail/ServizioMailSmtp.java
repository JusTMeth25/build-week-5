package it.epicode.eventi.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@ConditionalOnProperty(name = "app.mail.abilitato", havingValue = "true")
public class ServizioMailSmtp implements ServizioMail {

	private static final Logger log = LoggerFactory.getLogger(ServizioMailSmtp.class);

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter
			.ofPattern("EEEE d MMMM yyyy 'alle' HH:mm", Locale.ITALIAN)
			.withZone(ZoneId.of("Europe/Rome"));

	private final JavaMailSender postino;
	private final ITemplateEngine motore;
	private final String mittente;
	private final String urlApplicazione;

	public ServizioMailSmtp(JavaMailSender postino, ITemplateEngine motore,
			@Value("${app.mail.mittente}") String mittente,
			@Value("${app.mail.url-applicazione}") String urlApplicazione) {
		this.postino = postino;
		this.motore = motore;
		this.mittente = mittente;
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
		try {
			MimeMessage messaggio = postino.createMimeMessage();
			MimeMessageHelper aiuto = new MimeMessageHelper(messaggio, "UTF-8");
			aiuto.setFrom(mittente);
			aiuto.setTo(destinatario);
			aiuto.setSubject(oggetto);
			aiuto.setText(motore.process(template, contesto), true);
			postino.send(messaggio);
			log.info("Email \"{}\" inviata a {}", oggetto, destinatario);
		} catch (MessagingException | MailException eccezione) {
			log.error("Invio di \"{}\" a {} non riuscito: {}", oggetto, destinatario,
					eccezione.getMessage());
		}
	}
}
