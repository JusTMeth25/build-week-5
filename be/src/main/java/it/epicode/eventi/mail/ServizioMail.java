package it.epicode.eventi.mail;

public interface ServizioMail {

	void inviaCodiceVerifica(String destinatario, String nome, String codice);

	void inviaResetPassword(String destinatario, String nome, String token);

	void inviaTicket(String destinatario, DatiTicket ticket);

	void avvisaProprietarioNuovaIscrizione(String destinatario, String nomeProprietario,
			String nomeEvento, String nomePartecipante);
}
