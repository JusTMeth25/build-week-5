package it.epicode.eventi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class ConfigurazioneCors {

	private final List<String> origini;

	public ConfigurazioneCors(@Value("${app.cors.allowed-origins}") List<String> origini) {
		this.origini = origini;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configurazione = new CorsConfiguration();
		configurazione.setAllowedOrigins(origini);
		configurazione.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configurazione.setAllowedHeaders(List.of("Content-Type", "Accept", "Authorization", "X-Requested-With"));
		configurazione.setExposedHeaders(List.of("Location"));
		configurazione.setAllowCredentials(true);
		configurazione.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource sorgente = new UrlBasedCorsConfigurationSource();
		sorgente.registerCorsConfiguration("/api/**", configurazione);
		sorgente.registerCorsConfiguration("/ws/**", configurazione);
		return sorgente;
	}
}
