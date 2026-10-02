package it.epicode.eventi.geocoding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Geocoding (indirizzo -> coordinate e viceversa) tramite Google Geocoding API e
 * suggerimenti degli indirizzi tramite Places API (New). La chiave arriva dalla
 * variabile d'ambiente GOOGLE_MAPS_API_KEY e resta sul backend: il browser non la vede.
 * Senza chiave (o con geocoding disattivato) ogni metodo restituisce un risultato vuoto.
 *
 * Non si usa la Places API legacy (place/autocomplete/json, place/details/json):
 * Google l'ha deprecata, al suo posto c'e' places.googleapis.com/v1.
 */
@Service
public class ServizioGeocodifica {

	private static final Logger log = LoggerFactory.getLogger(ServizioGeocodifica.class);
	private static final String URL = "https://maps.googleapis.com/maps/api/geocode/json";
	private static final String URL_PLACES = "https://places.googleapis.com/v1";
	private static final String LINGUA = "it";

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
		return primoRisultato(URL + "?address={indirizzo}&language={lingua}&key={chiave}",
				indirizzo, LINGUA, chiave)
				.map(risultato -> new Coordinate(
						risultato.geometry().location().lat(), risultato.geometry().location().lng()));
	}

	/** Geocoding inverso: l'indirizzo piu' preciso che Google trova per quelle coordinate. */
	public Optional<Indirizzo> indirizzo(double latitudine, double longitudine) {
		if (!disponibile()) {
			return Optional.empty();
		}
		return primoRisultato(URL + "?latlng={lat},{lng}&language={lingua}&key={chiave}",
				latitudine, longitudine, LINGUA, chiave)
				.map(risultato -> new Indirizzo(risultato.formatted_address(), null,
						latitudine, longitudine));
	}

	/**
	 * Suggerimenti mentre si scrive. Il token di sessione raggruppa le battute e la
	 * successiva chiamata a {@link #luogo} in un'unica sessione di fatturazione Google.
	 */
	public List<Suggerimento> suggerimenti(String testo, String sessione) {
		if (!disponibile() || testo == null || testo.isBlank()) {
			return List.of();
		}
		Map<String, Object> corpo = new HashMap<>();
		corpo.put("input", testo);
		corpo.put("languageCode", LINGUA);
		corpo.put("regionCode", LINGUA);
		if (sessione != null && !sessione.isBlank()) {
			corpo.put("sessionToken", sessione);
		}
		try {
			RispostaAutocomplete risposta = cliente.post()
					.uri(URL_PLACES + "/places:autocomplete")
					.header("X-Goog-Api-Key", chiave)
					.contentType(MediaType.APPLICATION_JSON)
					.body(corpo)
					.retrieve()
					.body(RispostaAutocomplete.class);
			if (risposta == null || risposta.suggestions() == null) {
				return List.of();
			}
			return risposta.suggestions().stream()
					.map(RispostaAutocomplete.Voce::placePrediction)
					.filter(Objects::nonNull)
					.map(previsione -> new Suggerimento(
							previsione.placeId(),
							testo(previsione.text()),
							previsione.structuredFormat() == null ? null
									: testo(previsione.structuredFormat().mainText()),
							previsione.structuredFormat() == null ? null
									: testo(previsione.structuredFormat().secondaryText())))
					.toList();
		} catch (RestClientException eccezione) {
			log.error("Chiamata a Places Autocomplete non riuscita: {}", eccezione.getMessage());
			return List.of();
		}
	}

	/** Dettagli del suggerimento scelto: indirizzo completo, nome del luogo e coordinate. */
	public Optional<Indirizzo> luogo(String idLuogo, String sessione) {
		if (!disponibile() || idLuogo == null || idLuogo.isBlank()) {
			return Optional.empty();
		}
		try {
			boolean conSessione = sessione != null && !sessione.isBlank();
			DettaglioLuogo dettaglio = cliente.get()
					.uri(URL_PLACES + "/places/{id}?languageCode={lingua}"
							+ (conSessione ? "&sessionToken={sessione}" : ""),
							idLuogo, LINGUA, sessione)
					.header("X-Goog-Api-Key", chiave)
					.header("X-Goog-FieldMask", "formattedAddress,location,displayName")
					.retrieve()
					.body(DettaglioLuogo.class);
			if (dettaglio == null || dettaglio.location() == null) {
				return Optional.empty();
			}
			return Optional.of(new Indirizzo(dettaglio.formattedAddress(), testo(dettaglio.displayName()),
					dettaglio.location().latitude(), dettaglio.location().longitude()));
		} catch (RestClientException eccezione) {
			log.error("Chiamata a Place Details non riuscita: {}", eccezione.getMessage());
			return Optional.empty();
		}
	}

	private Optional<Risultato> primoRisultato(String uri, Object... parametri) {
		try {
			RispostaGeocoding risposta = cliente.get()
					.uri(uri, parametri)
					.retrieve()
					.body(RispostaGeocoding.class);

			if (risposta == null || !"OK".equals(risposta.status())
					|| risposta.results() == null || risposta.results().isEmpty()) {
				log.warn("Geocoding senza risultati: {}",
						risposta == null ? "nessuna risposta" : risposta.status());
				return Optional.empty();
			}
			return Optional.of(risposta.results().get(0));
		} catch (RestClientException | NullPointerException eccezione) {
			log.error("Chiamata a Google Geocoding non riuscita: {}", eccezione.getMessage());
			return Optional.empty();
		}
	}

	private static String testo(Testo testo) {
		return testo == null ? null : testo.text();
	}

	public record Suggerimento(String idLuogo, String descrizione, String principale, String secondario) {
	}

	public record Indirizzo(String indirizzo, String nomeLuogo, double latitudine, double longitudine) {
	}

	private record RispostaGeocoding(String status, List<Risultato> results) {
	}

	private record Risultato(String formatted_address, Geometria geometry) {
	}

	private record Geometria(Posizione location) {
	}

	private record Posizione(double lat, double lng) {
	}

	private record Testo(String text) {
	}

	private record RispostaAutocomplete(List<Voce> suggestions) {

		private record Voce(Previsione placePrediction) {
		}

		private record Previsione(String placeId, Testo text, Formato structuredFormat) {
		}

		private record Formato(Testo mainText, Testo secondaryText) {
		}
	}

	private record DettaglioLuogo(String formattedAddress, Testo displayName, PosizionePlaces location) {
	}

	private record PosizionePlaces(double latitude, double longitude) {
	}
}
