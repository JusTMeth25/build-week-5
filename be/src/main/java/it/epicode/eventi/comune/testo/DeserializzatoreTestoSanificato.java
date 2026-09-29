package it.epicode.eventi.comune.testo;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class DeserializzatoreTestoSanificato extends ValueDeserializer<String> {

	@Override
	public String deserialize(JsonParser parser, DeserializationContext contesto) {
		return SanificatoreHtml.pulisci(parser.getValueAsString());
	}
}
