package it.epicode.eventi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class ConfigurazioneWebSocket implements WebSocketMessageBrokerConfigurer {

	private final List<String> origini;
	private final InterceptorAutenticazioneWebSocket interceptorAutenticazione;

	public ConfigurazioneWebSocket(@Value("${app.cors.allowed-origins}") List<String> origini,
			InterceptorAutenticazioneWebSocket interceptorAutenticazione) {
		this.origini = origini;
		this.interceptorAutenticazione = interceptorAutenticazione;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registro) {
		registro.addEndpoint("/ws").setAllowedOrigins(origini.toArray(String[]::new));
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registrazione) {
		registrazione.interceptors(interceptorAutenticazione);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registro) {
		registro.enableSimpleBroker("/topic", "/queue");
		registro.setApplicationDestinationPrefixes("/app");
		registro.setUserDestinationPrefix("/utente");
	}
}
