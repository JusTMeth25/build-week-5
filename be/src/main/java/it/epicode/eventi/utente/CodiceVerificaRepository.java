package it.epicode.eventi.utente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CodiceVerificaRepository extends JpaRepository<CodiceVerifica, Long> {

	Optional<CodiceVerifica> findFirstByUtenteAndCodiceAndUsatoFalseOrderByIdDesc(Utente utente, String codice);

	@Modifying
	@Query("update CodiceVerifica c set c.usato = true where c.utente = :utente and c.usato = false")
	void invalidaPrecedenti(@Param("utente") Utente utente);
}
