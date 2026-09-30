package it.epicode.eventi.geocoding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Optional;

/**
 * Ricava le coordinate di un indirizzo tramite Google Geocoding API.
 * La chiave arriva dalla variabile d'ambiente GOOGLE_MAPS_API_KEY.
 * Senza chiave (o con geocoding disattivato) restituisce sempre Optional.empty().
 */
@Service
public class ServizioGeocodifica {

	private static final Logger log = LoggerFactory.getLogger(ServizioGeocodifica.class);
	private static final String URL = "https://maps.googleapis.com/maps/api/geocode/json";

	private final boolean abilitato;
	private final String chiave;
	private final RestClient cliente = RestClient.create();

	public ServizioGeocodifica(@Value("${app.geocoding.abilitato:true}") boolean abilitato,
			@Value("${app.geocoding.chiave:}") String chiave) {
		this.abilitato = abilitato;
		this.chiave = chiave;
	}

	public boolean disponibile() {
		return abilitato && !chiave.isBlank();
	}

	public Optional<Coordinate> coordinate(String indirizzo) {
		if (!disponibile() || indirizzo == null || indirizzo.isBlank()) {
			return Optional.empty();
		}
		try {
			RispostaGeocoding risposta = cliente.get()
					.uri(URL + "?address={indirizzo}&key={chiave}", indirizzo, chiave)
					.retrieve()
					.body(RispostaGeocoding.class);

			if (risposta == null || !"OK".equals(risposta.status())
					|| risposta.results() == null || risposta.results().isEmpty()) {
				log.warn("Geocoding senza risultati per '{}': {}", indirizzo,
						risposta == null ? "nessuna risposta" : risposta.status());
				return Optional.empty();
			}

			Posizione posizione = risposta.results().get(0).geometry().location();
			return Optional.of(new Coordinate(posizione.lat(), posizione.lng()));
		} catch (RestClientException | NullPointerException eccezione) {
			log.error("Chiamata a Google Geocoding non riuscita: {}", eccezione.getMessage());
			return Optional.empty();
		}
	}

	private record RispostaGeocoding(String status, List<Risultato> results) {
	}

	private record Risultato(Geometria geometry) {
	}

	private record Geometria(Posizione location) {
	}

	private record Posizione(double lat, double lng) {
	}
}
