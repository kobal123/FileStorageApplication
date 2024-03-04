package com.kobal.FileStorageApp.user.model;

import com.kobal.FileStorageApp.user.OAuthIssuer;
import com.kobal.FileStorageApp.user.role.Role;
import com.kobal.FileStorageApp.user.storage.UserStorageInfo;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "AppUser", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"issuer", "subject"})
})
public class AppUser {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;

    @Column(nullable = false)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();
    private String subject;

    @OneToOne(fetch = FetchType.LAZY)
    private UserStorageInfo userStorageInfo;

    @Enumerated(EnumType.STRING)
    private OAuthIssuer issuer;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public OAuthIssuer getIssuer() {
        return issuer;
    }

    public void setIssuer(OAuthIssuer issuer) {
        this.issuer = issuer;
    }

    public AppUser() {}
    public AppUser(Long id, String email, OAuthIssuer issuer, String sub) {
        this.id = id;
        this.email = email;
        this.issuer = issuer;
        this.subject = sub;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
