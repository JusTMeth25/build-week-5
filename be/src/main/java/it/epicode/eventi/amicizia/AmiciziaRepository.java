package it.epicode.eventi.amicizia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AmiciziaRepository extends JpaRepository<Amicizia, Long> {

	@Query("""
			select a from Amicizia a
			join fetch a.richiedente
			join fetch a.destinatario
			where (a.richiedente.id = :primo and a.destinatario.id = :secondo)
			   or (a.richiedente.id = :secondo and a.destinatario.id = :primo)
			""")
	Optional<Amicizia> traUtenti(@Param("primo") Long primo, @Param("secondo") Long secondo);

	@Query("""
			select count(a) > 0 from Amicizia a
			where a.stato = it.epicode.eventi.amicizia.StatoAmicizia.ACCETTATA
			  and ((a.richiedente.id = :primo and a.destinatario.id = :secondo)
			    or (a.richiedente.id = :secondo and a.destinatario.id = :primo))
			""")
	boolean amiciziaAccettata(@Param("primo") Long primo, @Param("secondo") Long secondo);

	@Query("""
			select a from Amicizia a
			join fetch a.richiedente
			join fetch a.destinatario
			where a.stato = it.epicode.eventi.amicizia.StatoAmicizia.ACCETTATA
			  and (a.richiedente.id = :utenteId or a.destinatario.id = :utenteId)
			order by a.aggiornataIl desc
			""")
	List<Amicizia> amiciDi(@Param("utenteId") Long utenteId);

	@Query("""
			select a from Amicizia a
			join fetch a.richiedente
			join fetch a.destinatario
			where a.destinatario.id = :utenteId
			  and a.stato = it.epicode.eventi.amicizia.StatoAmicizia.IN_ATTESA
			order by a.creataIl desc
			""")
	List<Amicizia> richiesteRicevute(@Param("utenteId") Long utenteId);
}
