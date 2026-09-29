package it.epicode.eventi.evento.web;

import it.epicode.eventi.evento.ArtistaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/artisti")
public class ControllerArtisti {

	private final ArtistaRepository artisti;

	public ControllerArtisti(ArtistaRepository artisti) {
		this.artisti = artisti;
	}

	@GetMapping
	public List<RispostaArtista> elenco() {
		return artisti.findAllByOrderByNomeAsc().stream().map(RispostaArtista::da).toList();
	}
}
