package it.epicode.eventi.evento.web;

import it.epicode.eventi.evento.Artista;

public record RispostaArtista(Long id, String nome, String genere) {

	public static RispostaArtista da(Artista artista) {
		return new RispostaArtista(artista.getId(), artista.getNome(), artista.getGenere());
	}
}
