package com.kobal.FileStorageApp.websecurity.keycloak;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KeyCloakLogoutHandler implements LogoutHandler {

    private static final Logger logger = LoggerFactory.getLogger(KeyCloakLogoutHandler.class);



    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication auth) {
        keycloakLogout((OidcUser) auth.getPrincipal());
    }

    private void keycloakLogout(OidcUser user) {
        RestTemplate restTemplate = new RestTemplate();

        String endSessionEndpoint = user.getIssuer() + "/protocol/openid-connect/logout";
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(endSessionEndpoint)
                .queryParam("id_token_hint", user.getIdToken().getTokenValue());

        ResponseEntity<String> logoutResponse = restTemplate.getForEntity(
                builder.toUriString(), String.class);
        if (logoutResponse.getStatusCode().is2xxSuccessful()) {
            logger.info("Logged out user from Keycloak");
        } else {
            logger.error("Could not propagate logout to Keycloak");
        }
    }
}
