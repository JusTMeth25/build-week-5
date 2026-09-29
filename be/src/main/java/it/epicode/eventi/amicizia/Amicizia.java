package it.epicode.eventi.amicizia;

import it.epicode.eventi.utente.Utente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(name = "amicizie",
		uniqueConstraints = @UniqueConstraint(name = "uq_amicizia_coppia",
				columnNames = {"richiedente_id", "destinatario_id"}))
public class Amicizia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "richiedente_id", nullable = false)
	private Utente richiedente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "destinatario_id", nullable = false)
	private Utente destinatario;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatoAmicizia stato = StatoAmicizia.IN_ATTESA;

	@Column(name = "creata_il", nullable = false)
	private Instant creataIl = Instant.now();

	@Column(name = "aggiornata_il")
	private Instant aggiornataIl;

	protected Amicizia() {
	}

	public Amicizia(Utente richiedente, Utente destinatario) {
		this.richiedente = richiedente;
		this.destinatario = destinatario;
	}

	public Long getId() {
		return id;
	}

	public Utente getRichiedente() {
		return richiedente;
	}

	public Utente getDestinatario() {
		return destinatario;
	}

	public StatoAmicizia getStato() {
		return stato;
	}

	public Instant getCreataIl() {
		return creataIl;
	}

	public Instant getAggiornataIl() {
		return aggiornataIl;
	}

	public void accetta() {
		this.stato = StatoAmicizia.ACCETTATA;
		this.aggiornataIl = Instant.now();
	}

	public void rifiuta() {
		this.stato = StatoAmicizia.RIFIUTATA;
		this.aggiornataIl = Instant.now();
	}

	public void riapri() {
		this.stato = StatoAmicizia.IN_ATTESA;
		this.aggiornataIl = Instant.now();
	}

	public Utente altro(Utente utente) {
		return richiedente.getId().equals(utente.getId()) ? destinatario : richiedente;
	}
}
