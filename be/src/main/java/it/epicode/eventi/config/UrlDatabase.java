package it.epicode.eventi.config;

import java.net.URI;

public final class UrlDatabase {

	private UrlDatabase() {
	}

	public static void applicaSePresente() {
		String grezzo = System.getenv("DATABASE_URL");
		if (grezzo == null || grezzo.isBlank()) {
			return;
		}
		if (grezzo.startsWith("jdbc:")) {
			System.setProperty("spring.datasource.url", grezzo);
			return;
		}

		URI uri = URI.create(grezzo.trim());
		String[] credenziali = uri.getUserInfo() == null ? new String[0] : uri.getUserInfo().split(":", 2);
		int porta = uri.getPort() == -1 ? 5432 : uri.getPort();
		String jdbc = "jdbc:postgresql://%s:%d%s?sslmode=require".formatted(uri.getHost(), porta, uri.getPath());

		System.setProperty("spring.datasource.url", jdbc);
		if (credenziali.length > 0) {
			System.setProperty("spring.datasource.username", credenziali[0]);
		}
		if (credenziali.length > 1) {
			System.setProperty("spring.datasource.password", credenziali[1]);
		}
	}
}
