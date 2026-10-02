package it.epicode.eventi.utente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenResetRepository extends JpaRepository<TokenReset, Long> {

	Optional<TokenReset> findFirstByTokenAndUsatoFalse(String token);

	@Modifying
	@Query("update TokenReset t set t.usato = true where t.utente = :utente and t.usato = false")
	void invalidaPrecedenti(@Param("utente") Utente utente);
}
