package com.kobal.FileStorageApp.user;

import jakarta.persistence.*;

@Entity
@Table(name = "AppUser")
public class AppUser {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;

    private String name;
    private String password;


    public AppUser(Long id, String username, String password) {
        this.id = id;
        this.name = username;
        this.password = password;
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
