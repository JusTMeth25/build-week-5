package it.epicode.eventi.ai;

import java.util.Optional;

public interface MiglioratoreDescrizione {

	String migliora(String titolo, String descrizioneOriginale, Optional<String> urlImmagine);
}
