package it.epicode.eventi.evento;

import it.epicode.eventi.utente.Utente;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "eventi", indexes = {
		@Index(name = "ix_eventi_data", columnList = "data_evento"),
		@Index(name = "ix_eventi_coord", columnList = "latitudine, longitudine")
})
public class Evento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 140)
	private String titolo;

	@Column(columnDefinition = "text")
	private String descrizione;

	@Column(name = "data_evento", nullable = false)
	private Instant dataEvento;

	@Column(nullable = false, length = 140)
	private String luogo;

	@Column(length = 255)
	private String indirizzo;

	@Column(nullable = false)
	private Double latitudine;

	@Column(nullable = false)
	private Double longitudine;

	private Integer capienza;

	// Nullable per non rompere l'update dello schema su eventi gia' esistenti:
	// gli eventi creati/modificati dopo questa feature hanno sempre un valore (default ALTRO).
	@Column(length = 30)
	private String genere = "ALTRO";

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "proprietario_id", nullable = false)
	private Utente proprietario;

	@OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private Set<ImmagineEvento> immagini = new LinkedHashSet<>();

	@OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id")
	private Set<MarkerEvento> marker = new LinkedHashSet<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "eventi_artisti",
			joinColumns = @JoinColumn(name = "evento_id"),
			inverseJoinColumns = @JoinColumn(name = "artista_id"))
	private Set<Artista> artisti = new LinkedHashSet<>();

	@Column(name = "creato_il", nullable = false)
	private Instant creatoIl = Instant.now();

	@Column(name = "aggiornato_il")
	private Instant aggiornatoIl;

	public Long getId() {
		return id;
	}

	public String getTitolo() {
		return titolo;
	}

	public void setTitolo(String titolo) {
		this.titolo = titolo;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public Instant getDataEvento() {
		return dataEvento;
	}

	public void setDataEvento(Instant dataEvento) {
		this.dataEvento = dataEvento;
	}

	public String getLuogo() {
		return luogo;
	}

	public void setLuogo(String luogo) {
		this.luogo = luogo;
	}

	public String getIndirizzo() {
		return indirizzo;
	}

	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}

	public Double getLatitudine() {
		return latitudine;
	}

	public void setLatitudine(Double latitudine) {
		this.latitudine = latitudine;
	}

	public Double getLongitudine() {
		return longitudine;
	}

	public void setLongitudine(Double longitudine) {
		this.longitudine = longitudine;
	}

	public Integer getCapienza() {
		return capienza;
	}

	public void setCapienza(Integer capienza) {
		this.capienza = capienza;
	}

	public String getGenere() {
		return genere;
	}

	public void setGenere(String genere) {
		this.genere = genere;
	}

	public Utente getProprietario() {
		return proprietario;
	}

	public void setProprietario(Utente proprietario) {
		this.proprietario = proprietario;
	}

	public Set<ImmagineEvento> getImmagini() {
		return immagini;
	}

	public Set<MarkerEvento> getMarker() {
		return marker;
	}

	public Set<Artista> getArtisti() {
		return artisti;
	}

	public Instant getCreatoIl() {
		return creatoIl;
	}

	public Instant getAggiornatoIl() {
		return aggiornatoIl;
	}

	public void segnaAggiornato() {
		this.aggiornatoIl = Instant.now();
	}

	public boolean appartieneA(Utente utente) {
		return proprietario != null && proprietario.getId().equals(utente.getId());
	}

	public Optional<ImmagineEvento> immaginePrincipale() {
		return immagini.stream()
				.filter(ImmagineEvento::isPrincipale)
				.findFirst()
				.or(() -> immagini.stream().findFirst());
	}
}
