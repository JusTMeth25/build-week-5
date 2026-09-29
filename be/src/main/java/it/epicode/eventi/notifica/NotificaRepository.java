package it.epicode.eventi.notifica;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificaRepository extends JpaRepository<Notifica, Long> {

	Page<Notifica> findByDestinatarioIdOrderByCreataIlDesc(Long destinatarioId, Pageable pagina);

	long countByDestinatarioIdAndLettaFalse(Long destinatarioId);

	@Modifying
	@Query("update Notifica n set n.letta = true where n.destinatario.id = :destinatarioId and n.letta = false")
	int segnaTutteLette(@Param("destinatarioId") Long destinatarioId);
}
