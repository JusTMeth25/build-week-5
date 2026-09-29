package it.epicode.eventi.mappa;

import it.epicode.eventi.evento.EventoRepository;
import it.epicode.eventi.evento.ServizioEventi;
import it.epicode.eventi.mappa.web.RispostaAnteprimaEvento;
import it.epicode.eventi.mappa.web.RispostaEventoMappa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ServizioMappa {

	private final EventoRepository eventi;
	private final ServizioEventi servizioEventi;

	public ServizioMappa(EventoRepository eventi, ServizioEventi servizioEventi) {
		this.eventi = eventi;
		this.servizioEventi = servizioEventi;
	}

	@Transactional(readOnly = true)
	public List<RispostaEventoMappa> eventi(Optional<Double> latitudine,
			Optional<Double> longitudine,
			int massimo) {
		if (latitudine.isPresent() && longitudine.isPresent()) {
			return eventi.vicini(Instant.now(), latitudine.get(), longitudine.get(), massimo)
					.stream()
					.map(RispostaEventoMappa::conDistanza)
					.toList();
		}
		return eventi.prossimi(Instant.now(), massimo).stream()
				.map(RispostaEventoMappa::da)
				.toList();
	}

	@Transactional(readOnly = true)
	public RispostaAnteprimaEvento anteprima(Long id) {
		return RispostaAnteprimaEvento.da(servizioEventi.carica(id));
	}
}
