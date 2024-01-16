package com.kobal.FileStorageApp.user.oidc;

import com.kobal.FileStorageApp.user.model.AppUser;
import com.kobal.FileStorageApp.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService extends OidcUserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOidcUserService.class);
    private final UserService userService;
    public CustomOidcUserService( UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {


        OidcUser oidcUser = super.loadUser(userRequest);
        AppUser user = userService.registerOrLoadUserFromOidcUser(oidcUser);
        return new CustomOidcUser(
                oidcUser.getAuthorities(),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                user);
    }
}




