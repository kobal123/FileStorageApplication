package com.kobal.FileStorageApp.user;

import com.kobal.FileStorageApp.user.role.Role;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "AppUser")
public class AppUser implements UserDetails {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;
    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new ArrayList<>(roles);
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return String.valueOf(id);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
