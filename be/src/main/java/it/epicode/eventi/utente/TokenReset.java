package it.epicode.eventi.utente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/** Token monouso per reimpostare la password: arriva per email dentro un link. */
@Entity
@Table(name = "token_reset")
public class TokenReset {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "utente_id", nullable = false)
	private Utente utente;

	@Column(nullable = false, unique = true, length = 100)
	private String token;

	@Column(name = "scade_il", nullable = false)
	private Instant scadeIl;

	@Column(nullable = false)
	private boolean usato = false;

	protected TokenReset() {
	}

	public TokenReset(Utente utente, String token, Instant scadeIl) {
		this.utente = utente;
		this.token = token;
		this.scadeIl = scadeIl;
	}

	public Utente getUtente() {
		return utente;
	}

	public boolean isUsato() {
		return usato;
	}

	public void segnaUsato() {
		this.usato = true;
	}

	public boolean isScaduto() {
		return Instant.now().isAfter(scadeIl);
	}
}
