package it.epicode.eventi.utente.web;

import it.epicode.eventi.utente.Utente;

public record RispostaUtentePubblico(Long id, String nome, String cognome) {

	public static RispostaUtentePubblico da(Utente utente) {
		return new RispostaUtentePubblico(utente.getId(), utente.getNome(), utente.getCognome());
	}
}
