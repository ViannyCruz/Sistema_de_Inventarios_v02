package com.sistema_de_inventarios_v02.Config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    @Order(1)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .oauth2Login(oauth2 -> oauth2.disable())
                .logout(logout -> logout.disable())
                .headers(headers -> headers.frameOptions().disable())
                .build();
    }

    @Bean
    @Primary
    public ClientRegistrationRepository testClientRegistrationRepository() {
        ClientRegistration dummyRegistration = ClientRegistration.withRegistrationId("test-keycloak")
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/login/oauth2/code/test-keycloak")
                .authorizationUri("http://localhost:8080/realms/test-realm/protocol/openid-connect/auth")
                .tokenUri("http://localhost:8080/realms/test-realm/protocol/openid-connect/token")
                .userInfoUri("http://localhost:8080/realms/test-realm/protocol/openid-connect/userinfo")
                .jwkSetUri("http://localhost:8080/realms/test-realm/protocol/openid-connect/certs")
                .userNameAttributeName("preferred_username")
                .clientName("Test Keycloak")
                .build();

        return new InMemoryClientRegistrationRepository(dummyRegistration);
    }
}