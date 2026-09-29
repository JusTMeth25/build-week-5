package it.epicode.eventi.comune.web;

import it.epicode.eventi.comune.eccezioni.Conflitto;
import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import it.epicode.eventi.comune.eccezioni.RichiestaNonValida;
import it.epicode.eventi.comune.eccezioni.RisorsaNonTrovata;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GestoreErrori {

	private static final Logger log = LoggerFactory.getLogger(GestoreErrori.class);

	@ExceptionHandler(RisorsaNonTrovata.class)
	public ResponseEntity<RispostaErrore> risorsaNonTrovata(RisorsaNonTrovata eccezione) {
		return risposta(HttpStatus.NOT_FOUND, "Risorsa non trovata", eccezione.getMessage());
	}

	@ExceptionHandler(OperazioneNonConsentita.class)
	public ResponseEntity<RispostaErrore> operazioneNonConsentita(OperazioneNonConsentita eccezione) {
		return risposta(HttpStatus.FORBIDDEN, "Operazione non consentita", eccezione.getMessage());
	}

	@ExceptionHandler(Conflitto.class)
	public ResponseEntity<RispostaErrore> conflitto(Conflitto eccezione) {
		return risposta(HttpStatus.CONFLICT, "Conflitto", eccezione.getMessage());
	}

	@ExceptionHandler(RichiestaNonValida.class)
	public ResponseEntity<RispostaErrore> richiestaNonValida(RichiestaNonValida eccezione) {
		return risposta(HttpStatus.BAD_REQUEST, "Richiesta non valida", eccezione.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<RispostaErrore> validazioneCorpo(MethodArgumentNotValidException eccezione) {
		Map<String, String> campi = new LinkedHashMap<>();
		for (FieldError errore : eccezione.getBindingResult().getFieldErrors()) {
			campi.putIfAbsent(errore.getField(), errore.getDefaultMessage());
		}
		return ResponseEntity.badRequest().body(RispostaErrore.diCampi(
				HttpStatus.BAD_REQUEST.value(),
				"Richiesta non valida",
				"Alcuni campi non sono validi",
				campi));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<RispostaErrore> validazioneParametri(ConstraintViolationException eccezione) {
		Map<String, String> campi = new LinkedHashMap<>();
		eccezione.getConstraintViolations().forEach(violazione ->
				campi.putIfAbsent(violazione.getPropertyPath().toString(), violazione.getMessage()));
		return ResponseEntity.badRequest().body(RispostaErrore.diCampi(
				HttpStatus.BAD_REQUEST.value(),
				"Richiesta non valida",
				"Alcuni parametri non sono validi",
				campi));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<RispostaErrore> integrita(DataIntegrityViolationException eccezione) {
		log.warn("Violazione di integrita': {}", eccezione.getMostSpecificCause().getMessage());
		return risposta(HttpStatus.CONFLICT, "Conflitto", "L'operazione viola un vincolo dei dati");
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<RispostaErrore> autenticazione(AuthenticationException eccezione) {
		return risposta(HttpStatus.UNAUTHORIZED, "Non autenticato", eccezione.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<RispostaErrore> accessoNegato(AccessDeniedException eccezione) {
		return risposta(HttpStatus.FORBIDDEN, "Accesso negato", "Non hai i permessi per questa operazione");
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<RispostaErrore> percorsoInesistente(NoResourceFoundException eccezione) {
		return risposta(HttpStatus.NOT_FOUND, "Risorsa non trovata", "Il percorso richiesto non esiste");
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<RispostaErrore> imprevisto(Exception eccezione) {
		log.error("Errore non gestito", eccezione);
		return risposta(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno",
				"Si e' verificato un errore imprevisto");
	}

	private ResponseEntity<RispostaErrore> risposta(HttpStatus stato, String errore, String messaggio) {
		return ResponseEntity.status(stato).body(RispostaErrore.di(stato.value(), errore, messaggio));
	}
}
