package it.epicode.eventi.evento;

import java.time.Instant;

public interface SintesiEvento {

	Long getId();

	String getTitolo();

	Instant getDataEvento();

	String getLuogo();

	Double getLatitudine();

	Double getLongitudine();

	String getImmaginePrincipale();
}
