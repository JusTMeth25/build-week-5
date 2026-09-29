package it.epicode.eventi.comune.eccezioni;

public class OperazioneNonConsentita extends RuntimeException {

	public OperazioneNonConsentita(String messaggio) {
		super(messaggio);
	}
}
