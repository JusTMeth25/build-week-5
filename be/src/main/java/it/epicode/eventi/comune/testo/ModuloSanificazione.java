package it.epicode.eventi.comune.testo;

import org.springframework.stereotype.Component;
import tools.jackson.databind.module.SimpleModule;

@Component
public class ModuloSanificazione extends SimpleModule {

	public ModuloSanificazione() {
		super("sanificazione-testo");
		addDeserializer(String.class, new DeserializzatoreTestoSanificato());
	}
}
