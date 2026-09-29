package it.epicode.eventi.notifica;

import it.epicode.eventi.evento.Evento;
import it.epicode.eventi.utente.Utente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notifiche",
		indexes = @Index(name = "ix_notifiche_dest", columnList = "destinatario_id, letta"))
public class Notifica {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "destinatario_id", nullable = false)
	private Utente destinatario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "evento_id")
	private Evento evento;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TipoNotifica tipo;

	@Column(nullable = false, length = 500)
	private String messaggio;

	@Column(nullable = false)
	private boolean letta = false;

	@Column(name = "creata_il", nullable = false)
	private Instant creataIl = Instant.now();

	protected Notifica() {
	}

	public Notifica(Utente destinatario, Evento evento, TipoNotifica tipo, String messaggio) {
		this.destinatario = destinatario;
		this.evento = evento;
		this.tipo = tipo;
		this.messaggio = messaggio;
	}

	public Long getId() {
		return id;
	}

	public Utente getDestinatario() {
		return destinatario;
	}

	public Evento getEvento() {
		return evento;
	}

	public TipoNotifica getTipo() {
		return tipo;
	}

	public String getMessaggio() {
		return messaggio;
	}

	public boolean isLetta() {
		return letta;
	}

	public void segnaLetta() {
		this.letta = true;
	}

	public Instant getCreataIl() {
		return creataIl;
	}
}
