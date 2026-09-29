package it.epicode.eventi.ai;

import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@ConditionalOnProperty(name = "app.ai.abilitata", havingValue = "false", matchIfMissing = true)
public class MiglioratoreDescrizioneNonAttivo implements MiglioratoreDescrizione {

	@Override
	public String migliora(String titolo, String descrizioneOriginale, Optional<String> urlImmagine) {
		throw new OperazioneNonConsentita(
				"Il miglioramento tramite AI non e' attivo su questo ambiente");
	}
}
