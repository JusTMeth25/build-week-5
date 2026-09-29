package it.epicode.eventi.comune.testo;

import org.springframework.web.util.HtmlUtils;

public final class SanificatoreHtml {

	private SanificatoreHtml() {
	}

	public static String pulisci(String valore) {
		if (valore == null) {
			return null;
		}
		String normalizzato = valore.replace("\u0000", "").strip();
		return HtmlUtils.htmlEscape(normalizzato);
	}
}
