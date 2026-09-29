package it.epicode.eventi.chat;

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

import java.time.Instant;

@Entity
@Table(name = "messaggi",
		indexes = @Index(name = "ix_messaggi_conv",
				columnList = "mittente_id, destinatario_id, inviato_il"))
public class Messaggio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "mittente_id", nullable = false)
	private Utente mittente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "destinatario_id", nullable = false)
	private Utente destinatario;

	@Column(nullable = false, length = 2000)
	private String contenuto;

	@Column(name = "inviato_il", nullable = false)
	private Instant inviatoIl = Instant.now();

	@Column(name = "letto_il")
	private Instant lettoIl;

	protected Messaggio() {
	}

	public Messaggio(Utente mittente, Utente destinatario, String contenuto) {
		this.mittente = mittente;
		this.destinatario = destinatario;
		this.contenuto = contenuto;
	}

	public Long getId() {
		return id;
	}

	public Utente getMittente() {
		return mittente;
	}

	public Utente getDestinatario() {
		return destinatario;
	}

	public String getContenuto() {
		return contenuto;
	}

	public void rimuoviContenuto() {
		this.contenuto = "";
	}

	public Instant getInviatoIl() {
		return inviatoIl;
	}

	public Instant getLettoIl() {
		return lettoIl;
	}

	public void segnaLetto() {
		if (lettoIl == null) {
			this.lettoIl = Instant.now();
		}
	}
}
