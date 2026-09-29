package it.epicode.eventi.evento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "artisti")
public class Artista {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 120)
	private String nome;

	@Column(length = 60)
	private String genere;

	protected Artista() {
	}

	public Artista(String nome, String genere) {
		this.nome = nome;
		this.genere = genere;
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getGenere() {
		return genere;
	}

	public void setGenere(String genere) {
		this.genere = genere;
	}
}
