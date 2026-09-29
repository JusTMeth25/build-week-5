package it.epicode.eventi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ConfigurazioneAsincrona {

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor esecutore = new ThreadPoolTaskExecutor();
		esecutore.setCorePoolSize(2);
		esecutore.setMaxPoolSize(8);
		esecutore.setQueueCapacity(200);
		esecutore.setThreadNamePrefix("invio-");
		esecutore.setWaitForTasksToCompleteOnShutdown(true);
		esecutore.setAwaitTerminationSeconds(20);
		esecutore.initialize();
		return esecutore;
	}
}
