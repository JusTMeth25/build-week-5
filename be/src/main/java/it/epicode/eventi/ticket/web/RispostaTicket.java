package it.epicode.eventi.ticket.web;

import it.epicode.eventi.ticket.Ticket;

import java.time.Instant;

public record RispostaTicket(
		Long id,
		String codice,
		Long eventoId,
		String nomeEvento,
		Instant dataEvento,
		String luogoEvento,
		String nomePartecipante,
		Instant emessoIl) {

	public static RispostaTicket da(Ticket ticket) {
		return new RispostaTicket(
				ticket.getId(),
				ticket.getCodice(),
				ticket.getEvento().getId(),
				ticket.getNomeEvento(),
				ticket.getDataEvento(),
				ticket.getLuogoEvento(),
				ticket.getNomePartecipante(),
				ticket.getEmessoIl());
	}
}
