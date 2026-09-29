package it.epicode.eventi.utente;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Long> {

	Optional<Utente> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);
}
