package it.epicode.eventi.ai;

import it.epicode.eventi.comune.eccezioni.RichiestaNonValida;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "app.ai.abilitata", havingValue = "true")
public class MiglioratoreDescrizioneOpenRouter implements MiglioratoreDescrizione {

	private static final Logger log = LoggerFactory.getLogger(MiglioratoreDescrizioneOpenRouter.class);

	private static final String ISTRUZIONI = """
			Sei il redattore di una piattaforma di eventi.
			Ricevi il titolo di un evento e la descrizione scritta dall'organizzatore.
			Riscrivi la descrizione in italiano, piu' chiara e invitante, restando fedele
			ai fatti forniti: non inventare date, luoghi, artisti, prezzi o dettagli che
			non compaiono nel testo.
			Rispondi con la sola descrizione riscritta, fra 400 e 900 caratteri,
			senza titoli, senza elenchi e senza marcatori di formattazione.
			""";

	private final RestClient cliente;
	private final String modello;

	public MiglioratoreDescrizioneOpenRouter(RestClient clienteOpenRouter,
			@Value("${app.ai.modello}") String modello) {
		this.cliente = clienteOpenRouter;
		this.modello = modello;
	}

	@Override
	public String migliora(String titolo, String descrizioneOriginale, Optional<String> urlImmagine) {
		if (descrizioneOriginale == null || descrizioneOriginale.isBlank()) {
			throw new RichiestaNonValida("Serve una descrizione di partenza da migliorare");
		}

		String contenutoUtente = urlImmagine
				.map(url -> "Titolo: %s%n%nDescrizione da migliorare:%n%s%n%nLocandina: %s"
						.formatted(titolo, descrizioneOriginale, url))
				.orElse("Titolo: %s%n%nDescrizione da migliorare:%n%s".formatted(titolo, descrizioneOriginale));

		RichiestaChatCompletion richiesta = new RichiestaChatCompletion(modello, List.of(
				new Messaggio("system", ISTRUZIONI),
				new Messaggio("user", contenutoUtente)));

		try {
			RispostaChatCompletion risposta = cliente.post()
					.uri("/chat/completions")
					.body(richiesta)
					.retrieve()
					.body(RispostaChatCompletion.class);

			String testo = Optional.ofNullable(risposta)
					.map(RispostaChatCompletion::choices)
					.filter(scelte -> !scelte.isEmpty())
					.map(scelte -> scelte.get(0).message().content())
					.map(String::strip)
					.orElse("");

			if (testo.isBlank()) {
				throw new RichiestaNonValida("Il servizio AI non ha restituito una descrizione");
			}
			return testo;
		} catch (RestClientResponseException eccezione) {
			log.error("Chiamata al servizio AI non riuscita: status={}, body={}",
					eccezione.getStatusCode().value(), eccezione.getResponseBodyAsString());
			throw new RichiestaNonValida(messaggioErroreOpenRouter(eccezione));
		} catch (RestClientException eccezione) {
			log.error("Chiamata al servizio AI non riuscita: {}", eccezione.getMessage());
			throw new RichiestaNonValida("Il servizio AI non e' raggiungibile, riprova piu' tardi");
		}
	}

	private String messaggioErroreOpenRouter(RestClientResponseException eccezione) {
		int stato = eccezione.getStatusCode().value();
		return switch (stato) {
			case 401, 403 -> "Configurazione AI non valida: controlla OPENROUTER_API_KEY e le restrizioni dell'account";
			case 404 -> "Il modello AI configurato non e' disponibile su OpenRouter: aggiorna AI_MODELLO";
			case 429 -> "Limite OpenRouter raggiunto: attendi oppure scegli un modello/account con quota disponibile";
			default -> "OpenRouter ha risposto con errore " + stato + ": riprova piu' tardi";
		};
	}

	private record RichiestaChatCompletion(String model, List<Messaggio> messages) {
	}

	private record Messaggio(String role, String content) {
	}

	private record RispostaChatCompletion(List<Scelta> choices) {
	}

	private record Scelta(Messaggio message) {
	}
}
