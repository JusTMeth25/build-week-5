package it.epicode.eventi.evento;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

	@Query("""
			select e from Evento e
			join fetch e.proprietario
			left join fetch e.immagini
			left join fetch e.marker
			left join fetch e.artisti
			where e.id = :id
			""")
	Optional<Evento> caricaCompleto(@Param("id") Long id);

	@Query("""
			select e.id as id,
			       e.titolo as titolo,
			       e.dataEvento as dataEvento,
			       e.luogo as luogo,
			       e.latitudine as latitudine,
			       e.longitudine as longitudine,
			       (select i.url from ImmagineEvento i
			         where i.evento = e and i.principale = true) as immaginePrincipale
			from Evento e
			where e.dataEvento > :istante
			  and (lower(e.titolo) like :filtro or lower(e.luogo) like :filtro)
			order by e.dataEvento asc
			""")
	Page<SintesiEvento> cerca(@Param("istante") Instant istante,
			@Param("filtro") String filtro,
			Pageable pagina);

	@Query("""
			select e.id as id,
			       e.titolo as titolo,
			       e.dataEvento as dataEvento,
			       e.luogo as luogo,
			       e.latitudine as latitudine,
			       e.longitudine as longitudine,
			       (select i.url from ImmagineEvento i
			         where i.evento = e and i.principale = true) as immaginePrincipale
			from Evento e
			where e.proprietario.id = :proprietarioId
			order by e.dataEvento desc
			""")
	Page<SintesiEvento> diProprietario(@Param("proprietarioId") Long proprietarioId, Pageable pagina);

	@Query(value = """
			select e.id as "id",
			       e.titolo as "titolo",
			       e.data_evento as "dataEvento",
			       e.luogo as "luogo",
			       e.latitudine as "latitudine",
			       e.longitudine as "longitudine",
			       (select i.url from immagini_evento i
			         where i.evento_id = e.id and i.principale = true limit 1) as "immaginePrincipale",
			       6371 * acos(least(1, greatest(-1,
			           cos(radians(:lat)) * cos(radians(e.latitudine))
			           * cos(radians(e.longitudine) - radians(:lon))
			           + sin(radians(:lat)) * sin(radians(e.latitudine))
			       ))) as "distanzaKm"
			from eventi e
			where e.data_evento > :istante
			order by "distanzaKm" asc
			limit :massimo
			""", nativeQuery = true)
	List<SintesiEventoVicino> vicini(@Param("istante") Instant istante,
			@Param("lat") double latitudine,
			@Param("lon") double longitudine,
			@Param("massimo") int massimo);

	@Query(value = """
			select e.id as "id",
			       e.titolo as "titolo",
			       e.data_evento as "dataEvento",
			       e.luogo as "luogo",
			       e.latitudine as "latitudine",
			       e.longitudine as "longitudine",
			       (select i.url from immagini_evento i
			         where i.evento_id = e.id and i.principale = true limit 1) as "immaginePrincipale"
			from eventi e
			where e.data_evento > :istante
			order by e.data_evento asc
			limit :massimo
			""", nativeQuery = true)
	List<SintesiEvento> prossimi(@Param("istante") Instant istante, @Param("massimo") int massimo);
}
