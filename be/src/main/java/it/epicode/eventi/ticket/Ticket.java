package it.epicode.eventi.ticket;

import it.epicode.eventi.evento.Evento;
import it.epicode.eventi.utente.Utente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket",
		uniqueConstraints = @UniqueConstraint(name = "uq_ticket_evento_partecipante",
				columnNames = {"evento_id", "partecipante_id"}),
		indexes = @Index(name = "ix_ticket_evento", columnList = "evento_id"))
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 36)
	private String codice;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "evento_id", nullable = false)
	private Evento evento;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "partecipante_id", nullable = false)
	private Utente partecipante;

	@Column(name = "nome_evento", nullable = false, length = 140)
	private String nomeEvento;

	@Column(name = "data_evento", nullable = false)
	private Instant dataEvento;

	@Column(name = "nome_partecipante", nullable = false, length = 140)
	private String nomePartecipante;

	@Column(name = "luogo_evento", nullable = false, length = 140)
	private String luogoEvento;

	@Column(name = "emesso_il", nullable = false)
	private Instant emessoIl = Instant.now();

	@Column(nullable = false)
	private boolean annullato = false;

	protected Ticket() {
	}

	public Ticket(Evento evento, Utente partecipante) {
		this.codice = UUID.randomUUID().toString();
		this.evento = evento;
		this.partecipante = partecipante;
		this.nomeEvento = evento.getTitolo();
		this.dataEvento = evento.getDataEvento();
		this.luogoEvento = evento.getLuogo();
		this.nomePartecipante = partecipante.getNomeCompleto();
	}

	public Long getId() {
		return id;
	}

	public String getCodice() {
		return codice;
	}

	public Evento getEvento() {
		return evento;
	}

	public Utente getPartecipante() {
		return partecipante;
	}

	public String getNomeEvento() {
		return nomeEvento;
	}

	public Instant getDataEvento() {
		return dataEvento;
	}

	public String getNomePartecipante() {
		return nomePartecipante;
	}

	public void setNomePartecipante(String nomePartecipante) {
		this.nomePartecipante = nomePartecipante;
	}

	public String getLuogoEvento() {
		return luogoEvento;
	}

	public Instant getEmessoIl() {
		return emessoIl;
	}

	public boolean isAnnullato() {
		return annullato;
	}

	public void annulla() {
		this.annullato = true;
	}
}
