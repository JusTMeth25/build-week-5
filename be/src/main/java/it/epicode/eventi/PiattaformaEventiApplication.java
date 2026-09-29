package it.epicode.eventi;

import it.epicode.eventi.config.UrlDatabase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PiattaformaEventiApplication {

	public static void main(String[] args) {
		UrlDatabase.applicaSePresente();
		SpringApplication.run(PiattaformaEventiApplication.class, args);
	}
}
