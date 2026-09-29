package it.epicode.eventi.evento;

import it.epicode.eventi.ai.MiglioratoreDescrizione;
import it.epicode.eventi.comune.eccezioni.OperazioneNonConsentita;
import it.epicode.eventi.comune.eccezioni.RisorsaNonTrovata;
import it.epicode.eventi.evento.web.RichiestaEvento;
import it.epicode.eventi.evento.web.RispostaDescrizione;
import it.epicode.eventi.evento.web.RispostaEvento;
import it.epicode.eventi.evento.web.RispostaEventoSintesi;
import it.epicode.eventi.evento.web.RichiestaImmagine;
import it.epicode.eventi.evento.web.RichiestaMarker;
import it.epicode.eventi.utente.Utente;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ServizioEventi {

	private final EventoRepository eventi;
	private final ArtistaRepository artisti;
	private final MiglioratoreDescrizione miglioratore;
	private final ApplicationEventPublisher pubblicatore;

	public ServizioEventi(EventoRepository eventi,
			ArtistaRepository artisti,
			MiglioratoreDescrizione miglioratore,
			ApplicationEventPublisher pubblicatore) {
		this.eventi = eventi;
		this.artisti = artisti;
		this.miglioratore = miglioratore;
		this.pubblicatore = pubblicatore;
	}

	@Transactional
	public RispostaEvento crea(RichiestaEvento richiesta, Utente proprietario) {
		Evento evento = new Evento();
		evento.setProprietario(proprietario);
		applica(richiesta, evento);
		return RispostaEvento.da(eventi.save(evento));
	}

	@Transactional
	public RispostaEvento aggiorna(Long id, RichiestaEvento richiesta, Utente richiedente) {
		Evento evento = caricaPerModifica(id, richiedente);
		applica(richiesta, evento);
		evento.segnaAggiornato();
		Evento salvato = eventi.save(evento);
		pubblicatore.publishEvent(new EventoAggiornato(salvato.getId(),
				"L'evento \"%s\" e' stato aggiornato".formatted(salvato.getTitolo())));
		return RispostaEvento.da(salvato);
	}

	@Transactional
	public void elimina(Long id, Utente richiedente) {
		Evento evento = caricaPerModifica(id, richiedente);
		eventi.delete(evento);
	}

	@Transactional(readOnly = true)
	public RispostaEvento dettaglio(Long id) {
		return RispostaEvento.da(carica(id));
	}

	@Transactional(readOnly = true)
	public Evento carica(Long id) {
		return eventi.caricaCompleto(id)
				.orElseThrow(() -> new RisorsaNonTrovata("Evento non trovato"));
	}

	@Transactional(readOnly = true)
	public Page<RispostaEventoSintesi> cerca(String testo, Pageable pagina) {
		String filtro = testo == null || testo.isBlank()
				? "%"
				: "%" + testo.strip().toLowerCase() + "%";
		return eventi.cerca(Instant.now(), filtro, pagina).map(RispostaEventoSintesi::da);
	}

	@Transactional(readOnly = true)
	public Page<RispostaEventoSintesi> miei(Utente proprietario, Pageable pagina) {
		return eventi.diProprietario(proprietario.getId(), pagina).map(RispostaEventoSintesi::da);
	}

	@Transactional(readOnly = true)
	public RispostaDescrizione proponiDescrizione(Long id, String descrizioneRichiesta,
			Utente richiedente) {
		Evento evento = caricaPerModifica(id, richiedente);
		String partenza = descrizioneRichiesta == null || descrizioneRichiesta.isBlank()
				? evento.getDescrizione()
				: descrizioneRichiesta;
		Optional<String> immagine = evento.immaginePrincipale().map(ImmagineEvento::getUrl);
		return new RispostaDescrizione(partenza, miglioratore.migliora(
				evento.getTitolo(), partenza, immagine));
	}

	@Transactional
	public Evento caricaPerModifica(Long id, Utente richiedente) {
		Evento evento = carica(id);
		if (!evento.appartieneA(richiedente)) {
			throw new OperazioneNonConsentita("Solo il proprietario puo' gestire questo evento");
		}
		return evento;
	}

	private void applica(RichiestaEvento richiesta, Evento evento) {
		evento.setTitolo(richiesta.titolo());
		evento.setDescrizione(richiesta.descrizione());
		evento.setDataEvento(richiesta.dataEvento());
		evento.setLuogo(richiesta.luogo());
		evento.setIndirizzo(richiesta.indirizzo());
		evento.setLatitudine(richiesta.latitudine());
		evento.setLongitudine(richiesta.longitudine());
		evento.setCapienza(richiesta.capienza());

		aggiornaArtisti(richiesta.artisti(), evento);
		aggiornaImmagini(richiesta.immagini(), evento);
		aggiornaMarker(richiesta.marker(), evento);
	}

	private void aggiornaArtisti(List<String> nomi, Evento evento) {
		if (nomi == null) {
			return;
		}
		evento.getArtisti().clear();
		nomi.stream()
				.filter(nome -> nome != null && !nome.isBlank())
				.map(String::strip)
				.distinct()
				.map(this::trovaOCreaArtista)
				.forEach(evento.getArtisti()::add);
	}

	private Artista trovaOCreaArtista(String nome) {
		return artisti.findByNomeIgnoreCase(nome)
				.orElseGet(() -> artisti.save(new Artista(nome, null)));
	}

	private void aggiornaImmagini(List<RichiestaImmagine> richieste, Evento evento) {
		if (richieste == null) {
			return;
		}
		evento.getImmagini().clear();
		boolean principaleAssegnata = false;
		for (RichiestaImmagine richiesta : richieste) {
			boolean principale = richiesta.principale() && !principaleAssegnata;
			principaleAssegnata = principaleAssegnata || principale;
			evento.getImmagini().add(new ImmagineEvento(evento, richiesta.url(), principale));
		}
		if (!principaleAssegnata) {
			evento.getImmagini().stream().findFirst()
					.ifPresent(immagine -> immagine.setPrincipale(true));
		}
	}

	private void aggiornaMarker(List<RichiestaMarker> richieste, Evento evento) {
		if (richieste == null) {
			return;
		}
		evento.getMarker().clear();
		richieste.forEach(richiesta -> evento.getMarker().add(new MarkerEvento(
				evento,
				richiesta.tipo(),
				richiesta.etichetta(),
				richiesta.latitudine(),
				richiesta.longitudine())));
	}
}
