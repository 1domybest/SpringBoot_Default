package com.example.Default_Project.config;


import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(builder -> {
            if ("x".equals(builder.build().getAttributes().get("registration_id"))) {
                String codeVerifier = PKCEUtil.generateCodeVerifier();
                String codeChallenge = PKCEUtil.generateCodeChallenge(codeVerifier);
                builder.additionalParameters(params -> {
                    params.put("code_challenge", codeChallenge);
                    params.put("code_challenge_method", "S256");
                });
                builder.attributes(attrs -> attrs.put("code_verifier", codeVerifier));
                System.out.println("PKCE Code Verifier: " + codeVerifier);
                System.out.println("PKCE Code Challenge: " + codeChallenge);
            }
        });
        return resolver;
    }

    @Bean
    public OAuth2AuthorizationRequestRedirectFilter customOAuth2RedirectFilter() {
        OAuth2AuthorizationRequestRedirectFilter filter = new OAuth2AuthorizationRequestRedirectFilter(customAuthorizationRequestResolver(clientRegistrationRepository));
        return filter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(customAuthorizationRequestResolver(clientRegistrationRepository))
                                .authorizationRequestRepository(new CustomAuthorizationRequestRepository())
                        )
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(new SessionDebugFilter(), SecurityContextPersistenceFilter.class);
        http.addFilterBefore(customOAuth2RedirectFilter(), OAuth2AuthorizationRequestRedirectFilter.class);

        return http.build();
    }
}