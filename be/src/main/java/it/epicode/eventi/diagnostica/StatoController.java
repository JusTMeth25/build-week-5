package it.epicode.eventi.diagnostica;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/stato")
public class StatoController {

	private final JdbcTemplate jdbc;

	public StatoController(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@GetMapping
	public Map<String, Object> stato() {
		String database = jdbc.queryForObject("SELECT current_database()", String.class);
		return Map.of(
				"servizio", "attivo",
				"database", database == null ? "sconosciuto" : database,
				"ora", Instant.now().toString());
	}
}
