package it.epicode.eventi.comune.web;

import java.time.Instant;
import java.util.Map;

public record RispostaErrore(
		Instant istante,
		int stato,
		String errore,
		String messaggio,
		Map<String, String> campi) {

	public static RispostaErrore di(int stato, String errore, String messaggio) {
		return new RispostaErrore(Instant.now(), stato, errore, messaggio, Map.of());
	}

	public static RispostaErrore diCampi(int stato, String errore, String messaggio, Map<String, String> campi) {
		return new RispostaErrore(Instant.now(), stato, errore, messaggio, campi);
	}
}
