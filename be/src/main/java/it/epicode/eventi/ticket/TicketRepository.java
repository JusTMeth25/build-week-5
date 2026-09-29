package it.epicode.eventi.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	@Modifying
	@Query("update Ticket t set t.nomePartecipante = :nome where t.partecipante.id = :partecipanteId")
	int anonimizzaPartecipante(@Param("partecipanteId") Long partecipanteId,
			@Param("nome") String nome);

	boolean existsByEventoIdAndPartecipanteIdAndAnnullatoFalse(Long eventoId, Long partecipanteId);

	long countByEventoIdAndAnnullatoFalse(Long eventoId);

	Optional<Ticket> findByCodice(String codice);

	@Query("""
			select t from Ticket t
			join fetch t.evento
			where t.partecipante.id = :partecipanteId and t.annullato = false
			order by t.dataEvento asc
			""")
	List<Ticket> deiPartecipante(@Param("partecipanteId") Long partecipanteId);

	@Query("""
			select t from Ticket t
			join fetch t.partecipante
			where t.evento.id = :eventoId and t.annullato = false
			order by t.emessoIl asc
			""")
	List<Ticket> dellEvento(@Param("eventoId") Long eventoId);

	@Query("""
			select t.partecipante.id from Ticket t
			where t.evento.id = :eventoId and t.annullato = false
			""")
	List<Long> idPartecipanti(@Param("eventoId") Long eventoId);

	@Query("""
			select count(t) > 0 from Ticket t
			where t.annullato = false
			  and t.partecipante.id = :primo
			  and t.evento.id in (
				select a.evento.id from Ticket a
				where a.annullato = false and a.partecipante.id = :secondo)
			""")
	boolean condividonoUnEvento(@Param("primo") Long primo, @Param("secondo") Long secondo);
}
