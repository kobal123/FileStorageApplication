package com.kobal.FileStorageApp.user.model;

import com.kobal.FileStorageApp.user.OAuthProvider;
import com.kobal.FileStorageApp.user.role.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "AppUser", indexes = {@Index(columnList = "name")})
public class AppUser {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = true)
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private OAuthProvider oauthProvider;

    public AppUser(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public AppUser(Long id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Role> getRoles() {
        return new HashSet<>(roles);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public AppUser() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }
}
