package it.epicode.eventi.config;

import it.epicode.eventi.utente.ServizioJwt;
import it.epicode.eventi.utente.UtenteRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfigurazioneSicurezza {

	private static final String[] PERCORSI_PUBBLICI = {
			"/api/stato",
			"/api/auth/registrazione",
			"/api/auth/verifica",
			"/api/auth/codice",
			"/api/auth/password-dimenticata",
			"/api/auth/reimposta-password",
			"/api/auth/login",
			"/actuator/health",
			"/actuator/health/**"
	};

	private static final String[] LETTURE_PUBBLICHE = {
			"/api/eventi",
			"/api/eventi/{id}",
			"/api/artisti",
			"/api/mappa/eventi",
			"/api/mappa/eventi/{id}"
	};

	private final CorsConfigurationSource sorgenteCors;

	public ConfigurazioneSicurezza(
			@Qualifier("corsConfigurationSource") CorsConfigurationSource sorgenteCors) {
		this.sorgenteCors = sorgenteCors;
	}

	@Bean
	public PasswordEncoder cifratorePassword() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	public AuthenticationManager gestoreAutenticazione(UserDetailsService dettagliUtente,
			PasswordEncoder cifratore) {
		DaoAuthenticationProvider fornitore = new DaoAuthenticationProvider(dettagliUtente);
		fornitore.setPasswordEncoder(cifratore);
		return new ProviderManager(fornitore);
	}

	@Bean
	public SecurityFilterChain catenaFiltri(HttpSecurity http, ServizioJwt jwt,
			UtenteRepository utenti) throws Exception {
		FiltroAutenticazioneJwt filtroAutenticazioneJwt = new FiltroAutenticazioneJwt(jwt, utenti);
		return http
				.cors(cors -> cors.configurationSource(sorgenteCors))
				.csrf(AbstractHttpConfigurer::disable)
				.headers(intestazioni -> intestazioni
						.contentSecurityPolicy(politica -> politica.policyDirectives(
								"default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
						.referrerPolicy(riferimento -> riferimento.policy(ReferrerPolicy.SAME_ORIGIN))
						.httpStrictTransportSecurity(trasporto -> trasporto
								.includeSubDomains(true)
								.maxAgeInSeconds(31536000))
						.permissionsPolicyHeader(permessi -> permessi.policy(
								"geolocation=(self), camera=(), microphone=(), payment=()"))
						.frameOptions(FrameOptionsConfig::deny))
				.sessionManagement(sessione -> sessione
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(richieste -> richieste
						.requestMatchers(PERCORSI_PUBBLICI).permitAll()
						.requestMatchers(HttpMethod.GET, LETTURE_PUBBLICHE).permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(gestione -> gestione
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.addFilterBefore(filtroAutenticazioneJwt, UsernamePasswordAuthenticationFilter.class)
				.build();
	}
}
