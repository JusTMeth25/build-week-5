package it.epicode.eventi.comune.eccezioni;

public class RisorsaNonTrovata extends RuntimeException {

	public RisorsaNonTrovata(String messaggio) {
		super(messaggio);
	}
}
