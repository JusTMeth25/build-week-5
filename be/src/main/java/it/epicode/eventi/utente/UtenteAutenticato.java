package it.epicode.eventi.utente;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UtenteAutenticato implements UserDetails {

	private final Utente utente;

	public UtenteAutenticato(Utente utente) {
		this.utente = utente;
	}

	public Utente getUtente() {
		return utente;
	}

	public Long getId() {
		return utente.getId();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + utente.getRuolo().name()));
	}

	@Override
	public String getPassword() {
		return utente.getPasswordHash();
	}

	@Override
	public String getUsername() {
		return utente.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return utente.isAttivo() && !utente.isBloccatoPerTentativi();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return utente.isVerificato() && utente.isAttivo();
	}
}
