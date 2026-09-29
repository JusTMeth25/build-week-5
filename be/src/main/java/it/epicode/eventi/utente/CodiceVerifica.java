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

@Entity
@Table(name = "codici_verifica")
public class CodiceVerifica {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "utente_id", nullable = false)
	private Utente utente;

	@Column(nullable = false, length = 6)
	private String codice;

	@Column(name = "scade_il", nullable = false)
	private Instant scadeIl;

	@Column(nullable = false)
	private boolean usato = false;

	protected CodiceVerifica() {
	}

	public CodiceVerifica(Utente utente, String codice, Instant scadeIl) {
		this.utente = utente;
		this.codice = codice;
		this.scadeIl = scadeIl;
	}

	public Long getId() {
		return id;
	}

	public Utente getUtente() {
		return utente;
	}

	public String getCodice() {
		return codice;
	}

	public Instant getScadeIl() {
		return scadeIl;
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
