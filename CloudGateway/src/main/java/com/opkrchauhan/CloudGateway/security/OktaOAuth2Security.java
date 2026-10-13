
package com.opkrchauhan.CloudGateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.function.Consumer;

@Configuration
@EnableWebFluxSecurity
public class OktaOAuth2Security {

    @Value("${auth0.audience}")
    private String audience;

    private final ReactiveClientRegistrationRepository repository;

    public OktaOAuth2Security(
            ReactiveClientRegistrationRepository repository) {
        this.repository = repository;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/authenticate/login",
                                "/authenticate/print_token")
                        .authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationRequestResolver(
                                authorizationRequestResolver()
                        )
                )
                .build();
    }

    private ServerOAuth2AuthorizationRequestResolver
    authorizationRequestResolver() {

        DefaultServerOAuth2AuthorizationRequestResolver resolver =
                new DefaultServerOAuth2AuthorizationRequestResolver(
                        repository);

        resolver.setAuthorizationRequestCustomizer(
                oauthBuilderCustomization());

        return resolver;
    }

    private Consumer<OAuth2AuthorizationRequest.Builder>
    oauthBuilderCustomization() {

        return customizer ->
                customizer.additionalParameters(
                        params -> params.put("audience", audience)
                );
    }
}
