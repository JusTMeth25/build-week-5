package it.epicode.eventi.evento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArtistaRepository extends JpaRepository<Artista, Long> {

	Optional<Artista> findByNomeIgnoreCase(String nome);

	List<Artista> findAllByOrderByNomeAsc();
}
