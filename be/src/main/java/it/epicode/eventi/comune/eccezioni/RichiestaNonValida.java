package it.epicode.eventi.comune.eccezioni;

public class RichiestaNonValida extends RuntimeException {

	public RichiestaNonValida(String messaggio) {
		super(messaggio);
	}
}
