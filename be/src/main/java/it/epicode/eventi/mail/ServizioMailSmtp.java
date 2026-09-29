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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@ConditionalOnProperty(name = "app.mail.abilitato", havingValue = "true")
public class ServizioMailSmtp implements ServizioMail {

	private static final Logger log = LoggerFactory.getLogger(ServizioMailSmtp.class);

	private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter
			.ofPattern("dd/MM/yyyy 'alle' HH:mm", Locale.ITALIAN)
			.withZone(ZoneId.of("Europe/Rome"));

	private final JavaMailSender postino;
	private final String mittente;

	public ServizioMailSmtp(JavaMailSender postino, @Value("${app.mail.mittente}") String mittente) {
		this.postino = postino;
		this.mittente = mittente;
	}

	@Override
	@Async
	public void inviaCodiceVerifica(String destinatario, String nome, String codice) {
		String corpo = """
				<p>Ciao %s,</p>
				<p>il codice per confermare il tuo account è:</p>
				<p style="font-size:24px;letter-spacing:4px;"><strong>%s</strong></p>
				<p>Il codice vale 15 minuti. Fino alla conferma l'account non è utilizzabile.</p>
				""".formatted(nome, codice);
		spedisci(destinatario, "Conferma il tuo account", corpo);
	}

	@Override
	@Async
	public void inviaTicket(String destinatario, DatiTicket ticket) {
		String corpo = """
				<p>Ciao %s,</p>
				<p>ecco il tuo ticket per <strong>%s</strong>.</p>
				<table cellpadding="6" style="border-collapse:collapse;">
				  <tr><td>Evento</td><td><strong>%s</strong></td></tr>
				  <tr><td>Data</td><td>%s</td></tr>
				  <tr><td>Luogo</td><td>%s</td></tr>
				  <tr><td>Partecipante</td><td>%s</td></tr>
				  <tr><td>Codice</td><td><strong>%s</strong></td></tr>
				</table>
				<p>Presenta questo codice all'ingresso. Lo trovi anche nella tua area personale.</p>
				""".formatted(
				ticket.nomePartecipante(),
				ticket.nomeEvento(),
				ticket.nomeEvento(),
				FORMATO_DATA.format(ticket.dataEvento()),
				ticket.luogo(),
				ticket.nomePartecipante(),
				ticket.codice());
		spedisci(destinatario, "Il tuo ticket per " + ticket.nomeEvento(), corpo);
	}

	@Override
	@Async
	public void avvisaProprietarioNuovaIscrizione(String destinatario, String nomeProprietario,
			String nomeEvento, String nomePartecipante) {
		String corpo = """
				<p>Ciao %s,</p>
				<p><strong>%s</strong> si è iscritto al tuo evento <strong>%s</strong>.</p>
				<p>Trovi l'elenco completo dei partecipanti nella pagina dell'evento.</p>
				""".formatted(nomeProprietario, nomePartecipante, nomeEvento);
		spedisci(destinatario, "Nuova iscrizione a " + nomeEvento, corpo);
	}

	private void spedisci(String destinatario, String oggetto, String corpoHtml) {
		try {
			MimeMessage messaggio = postino.createMimeMessage();
			MimeMessageHelper aiuto = new MimeMessageHelper(messaggio, "UTF-8");
			aiuto.setFrom(mittente);
			aiuto.setTo(destinatario);
			aiuto.setSubject(oggetto);
			aiuto.setText(corpoHtml, true);
			postino.send(messaggio);
			log.info("Email \"{}\" inviata a {}", oggetto, destinatario);
		} catch (MessagingException | MailException eccezione) {
			log.error("Invio di \"{}\" a {} non riuscito: {}", oggetto, destinatario,
					eccezione.getMessage());
		}
	}
}
