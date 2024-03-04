package com.kobal.FileStorageApp.user;

import java.util.Arrays;

public enum OAuthIssuer {
    KEYCLOAK("http://127.0.0.1:8080/realms/Filestore"),
    GOOGLE("https://accounts.google.com");

    private String url;
    OAuthIssuer(String url) {
        this.url = url;
    }

    public static OAuthIssuer fromIssuer(String issuer) {
        return Arrays.stream(OAuthIssuer.values())
                .filter(oAuthProvider -> oAuthProvider.url.equals(issuer))
                .findFirst()
                .orElseThrow();
    }
}
