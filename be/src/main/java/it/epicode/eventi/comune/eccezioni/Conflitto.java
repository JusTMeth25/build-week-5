package it.epicode.eventi.comune.eccezioni;

public class Conflitto extends RuntimeException {

	public Conflitto(String messaggio) {
		super(messaggio);
	}
}
