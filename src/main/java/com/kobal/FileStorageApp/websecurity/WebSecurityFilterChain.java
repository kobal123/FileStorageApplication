package com.kobal.FileStorageApp.websecurity;

import com.kobal.FileStorageApp.websecurity.keycloak.KeyCloakLogoutHandler;
import com.kobal.FileStorageApp.user.oidc.CustomOidcUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityFilterChain {
    private final Logger logger = LoggerFactory.getLogger(WebSecurityFilterChain.class);
    private final Environment environment;
    private final CustomOidcUserService userService;
    private final KeyCloakLogoutHandler keyCloakLogoutHandler;

    public WebSecurityFilterChain(Environment environment, CustomOidcUserService userService, KeyCloakLogoutHandler keyCloakLogoutHandler) {
        this.environment = environment;
        this.userService = userService;
        this.keyCloakLogoutHandler = keyCloakLogoutHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated());
        http.formLogin(loginConfig -> loginConfig.permitAll());

        List<String> profiles = List.of(environment.getActiveProfiles());
        if (profiles.contains("keycloak")) {
            OIDCFilterChain(http);
        }
        // TODO: logout does not work with keycloak : https://developers.redhat.com/articles/2022/12/07/how-implement-single-sign-out-keycloak-spring-boot

        return http.build();
    }

    private void OIDCFilterChain(HttpSecurity http) throws Exception {
        http.oauth2Login(a -> {
            a.userInfoEndpoint(e -> e.oidcUserService(userService));

        });

        http.logout(logoutConfigurer -> logoutConfigurer.addLogoutHandler(keyCloakLogoutHandler));
    }
}
