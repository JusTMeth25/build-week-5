package it.epicode.eventi.amicizia.web;

import it.epicode.eventi.amicizia.Amicizia;
import it.epicode.eventi.amicizia.StatoAmicizia;
import it.epicode.eventi.utente.Utente;
import it.epicode.eventi.utente.web.RispostaUtentePubblico;

import java.time.Instant;

public record RispostaAmicizia(
		Long id,
		RispostaUtentePubblico richiedente,
		RispostaUtentePubblico destinatario,
		StatoAmicizia stato,
		Instant creataIl) {

	public static RispostaAmicizia da(Amicizia amicizia) {
		return new RispostaAmicizia(
				amicizia.getId(),
				RispostaUtentePubblico.da(amicizia.getRichiedente()),
				RispostaUtentePubblico.da(amicizia.getDestinatario()),
				amicizia.getStato(),
				amicizia.getCreataIl());
	}

	public static RispostaAmicizia amico(Amicizia amicizia, Utente riferimento) {
		Utente altro = amicizia.altro(riferimento);
		return new RispostaAmicizia(
				amicizia.getId(),
				RispostaUtentePubblico.da(riferimento),
				RispostaUtentePubblico.da(altro),
				amicizia.getStato(),
				amicizia.getCreataIl());
	}
}
