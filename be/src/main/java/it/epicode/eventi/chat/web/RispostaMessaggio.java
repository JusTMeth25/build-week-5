package it.epicode.eventi.chat.web;

import it.epicode.eventi.chat.Messaggio;

import java.time.Instant;

public record RispostaMessaggio(
		Long id,
		Long mittenteId,
		String nomeMittente,
		Long destinatarioId,
		String contenuto,
		Instant inviatoIl,
		Instant lettoIl) {

	public static RispostaMessaggio da(Messaggio messaggio) {
		return new RispostaMessaggio(
				messaggio.getId(),
				messaggio.getMittente().getId(),
				messaggio.getMittente().getNomeCompleto(),
				messaggio.getDestinatario().getId(),
				messaggio.getContenuto(),
				messaggio.getInviatoIl(),
				messaggio.getLettoIl());
	}
}
