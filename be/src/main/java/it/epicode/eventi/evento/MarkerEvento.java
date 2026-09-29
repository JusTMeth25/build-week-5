package it.epicode.eventi.evento;

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

@Entity
@Table(name = "marker_evento")
public class MarkerEvento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "evento_id", nullable = false)
	private Evento evento;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TipoMarker tipo;

	@Column(length = 80)
	private String etichetta;

	@Column(nullable = false)
	private Double latitudine;

	@Column(nullable = false)
	private Double longitudine;

	protected MarkerEvento() {
	}

	public MarkerEvento(Evento evento, TipoMarker tipo, String etichetta, Double latitudine,
			Double longitudine) {
		this.evento = evento;
		this.tipo = tipo;
		this.etichetta = etichetta;
		this.latitudine = latitudine;
		this.longitudine = longitudine;
	}

	public Long getId() {
		return id;
	}

	public Evento getEvento() {
		return evento;
	}

	public TipoMarker getTipo() {
		return tipo;
	}

	public String getEtichetta() {
		return etichetta;
	}

	public Double getLatitudine() {
		return latitudine;
	}

	public Double getLongitudine() {
		return longitudine;
	}
}
