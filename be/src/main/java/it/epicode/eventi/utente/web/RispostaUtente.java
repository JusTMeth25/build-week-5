package it.epicode.eventi.utente.web;

import it.epicode.eventi.utente.Ruolo;
import it.epicode.eventi.utente.Utente;

import java.time.LocalDate;

public record RispostaUtente(
		Long id,
		String email,
		String nome,
		String cognome,
		String indirizzo,
		LocalDate dataNascita,
		Integer eta,
		String telefono,
		Double latitudine,
		Double longitudine,
		Ruolo ruolo,
		boolean verificato) {

	public static RispostaUtente da(Utente utente) {
		return new RispostaUtente(
				utente.getId(),
				utente.getEmail(),
				utente.getNome(),
				utente.getCognome(),
				utente.getIndirizzo(),
				utente.getDataNascita(),
				utente.getEta(),
				utente.getTelefono(),
				utente.getLatitudine(),
				utente.getLongitudine(),
				utente.getRuolo(),
				utente.isVerificato());
	}
}
