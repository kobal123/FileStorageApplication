package com.kobal.FileStorageApp.user.oidc;

import com.kobal.FileStorageApp.user.model.AppUser;
import com.kobal.FileStorageApp.user.userdetails.UsernameProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;

public class CustomOidcUser extends DefaultOidcUser implements UsernameProvider {

    private final AppUser user;


    public CustomOidcUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken, AppUser user) {
        super(authorities, idToken);
        this.user = user;
    }

    public CustomOidcUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken, String nameAttributeKey, AppUser user) {
        super(authorities, idToken, nameAttributeKey);
        this.user = user;
    }

    public CustomOidcUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken, OidcUserInfo userInfo, AppUser user) {
        super(authorities, idToken, userInfo);
        this.user = user;
    }

    public CustomOidcUser(Collection<? extends GrantedAuthority> authorities, OidcIdToken idToken, OidcUserInfo userInfo, String nameAttributeKey, AppUser user) {
        super(authorities, idToken, userInfo, nameAttributeKey);
        this.user = user;
    }

    @Override
    public String getName() {
        return Long.toString(user.getId());
    }

    @Override
    public String getLoggedInUserName() {
        return user.getName();
    }
}
