package com.kobal.FileStorageApp.websecurity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityFilterChain {
    private final Logger logger = LoggerFactory.getLogger(WebSecurityFilterChain.class);
    private final Environment environment;

    public WebSecurityFilterChain(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated());
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        http.sessionManagement(session ->session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }


}
