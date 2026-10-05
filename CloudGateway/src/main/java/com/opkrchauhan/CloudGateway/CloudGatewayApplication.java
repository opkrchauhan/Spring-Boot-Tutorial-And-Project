package com.opkrchauhan.CloudGateway;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;

import java.time.Duration;

@SpringBootApplication
public class CloudGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudGatewayApplication.class, args);
	}

	@Bean
	public KeyResolver userKeyResolver() {
		return exchange -> Mono.just(
				exchange.getRequest().getRemoteAddress() != null
						? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
						: "anonymous");
	}
	@Bean
	public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
		return factory -> factory.configureDefault(id ->
				new Resilience4JConfigBuilder(id)
						.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
						.timeLimiterConfig(TimeLimiterConfig.custom()
								.timeoutDuration(Duration.ofSeconds(4))
								.build())
						.build());
	}
}
