package works.bosk.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import works.bosk.Bosk;
import works.bosk.jackson.BoskJacksonModule;
import works.bosk.jackson.JacksonPlugin;

@Configuration
@EnableConfigurationProperties(WebProperties.class)
public class BoskAutoConfiguration {
	@Bean
	@ConditionalOnProperty(
		prefix = "bosk.web",
		name = "read-context",
		matchIfMissing = true)
	@ConditionalOnBean(Bosk.class) // Because of matchIfMissing
	ReadContextFilter readContextFilter(
		Bosk<?> bosk
	) {
		return new ReadContextFilter(bosk);
	}

	@Bean
	@ConditionalOnProperty(prefix = "bosk.web", name = "service-path")
	ServiceEndpoints serviceEndpoints(
		Bosk<?> bosk,
		ObjectMapper mapper,
		JacksonPlugin plugin
	) {
		return new ServiceEndpoints(bosk, mapper, plugin);
	}

	@Bean
	@ConditionalOnMissingBean
	JacksonPlugin jacksonPlugin() {
		return new JacksonPlugin();
	}

	@Bean
	BoskJacksonModule boskJacksonModule(Bosk<?> bosk, JacksonPlugin jacksonPlugin) {
		return jacksonPlugin.moduleFor(bosk);
	}

}
