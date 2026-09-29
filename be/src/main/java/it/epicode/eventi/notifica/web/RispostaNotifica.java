package it.epicode.eventi.notifica.web;

import it.epicode.eventi.notifica.Notifica;
import it.epicode.eventi.notifica.TipoNotifica;

import java.time.Instant;

public record RispostaNotifica(
		Long id,
		TipoNotifica tipo,
		String messaggio,
		Long eventoId,
		boolean letta,
		Instant creataIl) {

	public static RispostaNotifica da(Notifica notifica) {
		return new RispostaNotifica(
				notifica.getId(),
				notifica.getTipo(),
				notifica.getMessaggio(),
				notifica.getEvento() == null ? null : notifica.getEvento().getId(),
				notifica.isLetta(),
				notifica.getCreataIl());
	}
}
