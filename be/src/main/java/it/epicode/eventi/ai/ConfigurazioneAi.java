package it.epicode.eventi.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConditionalOnProperty(name = "app.ai.abilitata", havingValue = "true")
public class ConfigurazioneAi {

	@Bean
	@ConditionalOnMissingBean(RestClient.Builder.class)
	public RestClient.Builder costruttoreRestClient() {
		return RestClient.builder();
	}

	@Bean
	public RestClient clienteOpenRouter(RestClient.Builder builder, @Value("${app.ai.chiave}") String chiave) {
		if (chiave.isBlank()) {
			throw new IllegalStateException(
					"OPENROUTER_API_KEY non impostata: obbligatoria con app.ai.abilitata=true");
		}
		return builder
				.baseUrl("https://openrouter.ai/api/v1")
				.defaultHeader("Authorization", "Bearer " + chiave)
				.defaultHeader("HTTP-Referer", "http://localhost:5173")
				.defaultHeader("X-Title", "EventVerse")
				.build();
	}
}
