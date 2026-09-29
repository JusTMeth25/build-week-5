package it.epicode.eventi.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessaggioRepository extends JpaRepository<Messaggio, Long> {

	@Query("""
			select m from Messaggio m
			join fetch m.mittente
			join fetch m.destinatario
			where (m.mittente.id = :primo and m.destinatario.id = :secondo)
			   or (m.mittente.id = :secondo and m.destinatario.id = :primo)
			order by m.inviatoIl desc
			""")
	Page<Messaggio> conversazione(@Param("primo") Long primo,
			@Param("secondo") Long secondo,
			Pageable pagina);

	@Modifying
	@Query("""
			update Messaggio m set m.lettoIl = CURRENT_TIMESTAMP
			where m.destinatario.id = :destinatarioId
			  and m.mittente.id = :mittenteId
			  and m.lettoIl is null
			""")
	int segnaLetti(@Param("destinatarioId") Long destinatarioId,
			@Param("mittenteId") Long mittenteId);

	long countByDestinatarioIdAndLettoIlIsNull(Long destinatarioId);

	@Modifying
	@Query("update Messaggio m set m.contenuto = '' where m.mittente.id = :mittenteId")
	int rimuoviContenutiDi(@Param("mittenteId") Long mittenteId);
}
