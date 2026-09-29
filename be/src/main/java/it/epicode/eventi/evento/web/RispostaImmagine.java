package it.epicode.eventi.evento.web;

import it.epicode.eventi.evento.ImmagineEvento;

public record RispostaImmagine(Long id, String url, boolean principale) {

	public static RispostaImmagine da(ImmagineEvento immagine) {
		return new RispostaImmagine(immagine.getId(), immagine.getUrl(), immagine.isPrincipale());
	}
}
