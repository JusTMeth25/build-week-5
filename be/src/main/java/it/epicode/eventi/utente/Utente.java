package it.epicode.eventi.utente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "utenti", indexes = @Index(name = "ix_utenti_email", columnList = "email", unique = true))
public class Utente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 255)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(nullable = false, length = 60)
	private String nome;

	@Column(nullable = false, length = 60)
	private String cognome;

	@Column(length = 255)
	private String indirizzo;

	@Column(name = "data_nascita")
	private LocalDate dataNascita;

	@Column(length = 30)
	private String telefono;

	private Double latitudine;

	private Double longitudine;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Ruolo ruolo = Ruolo.UTENTE;

	@Column(nullable = false)
	private boolean verificato = false;

	@Column(nullable = false)
	private boolean attivo = true;

	@Column(nullable = false)
	private boolean anonimizzato = false;

	@Column(name = "creato_il", nullable = false)
	private Instant creatoIl = Instant.now();

	@Column(name = "tentativi_falliti", nullable = false)
	@ColumnDefault("0")
	private int tentativiFalliti = 0;

	@Column(name = "bloccato_fino_il")
	private Instant bloccatoFinoIl;

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public String getNomeCompleto() {
		return nome + " " + cognome;
	}

	public String getIndirizzo() {
		return indirizzo;
	}

	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}

	public LocalDate getDataNascita() {
		return dataNascita;
	}

	public void setDataNascita(LocalDate dataNascita) {
		this.dataNascita = dataNascita;
	}

	public Integer getEta() {
		return dataNascita == null ? null : Period.between(dataNascita, LocalDate.now()).getYears();
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
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

	public Ruolo getRuolo() {
		return ruolo;
	}

	public void setRuolo(Ruolo ruolo) {
		this.ruolo = ruolo;
	}

	public boolean isVerificato() {
		return verificato;
	}

	public void setVerificato(boolean verificato) {
		this.verificato = verificato;
	}

	public boolean isAttivo() {
		return attivo;
	}

	public void setAttivo(boolean attivo) {
		this.attivo = attivo;
	}

	public boolean isAnonimizzato() {
		return anonimizzato;
	}

	public void setAnonimizzato(boolean anonimizzato) {
		this.anonimizzato = anonimizzato;
	}

	public Instant getCreatoIl() {
		return creatoIl;
	}

	public int getTentativiFalliti() {
		return tentativiFalliti;
	}

	public void setTentativiFalliti(int tentativiFalliti) {
		this.tentativiFalliti = tentativiFalliti;
	}

	public Instant getBloccatoFinoIl() {
		return bloccatoFinoIl;
	}

	public void setBloccatoFinoIl(Instant bloccatoFinoIl) {
		this.bloccatoFinoIl = bloccatoFinoIl;
	}

	public boolean isBloccatoPerTentativi() {
		return bloccatoFinoIl != null && bloccatoFinoIl.isAfter(Instant.now());
	}
}
