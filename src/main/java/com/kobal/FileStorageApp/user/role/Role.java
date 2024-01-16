package com.kobal.FileStorageApp.user.role;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;


@Entity
@Table(name = "APP_USER_ROLE")
public class Role implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    @Enumerated(value = EnumType.STRING)
    @Column(length = 20)
    RoleName name;

    public Role(RoleName name) {
        this.name = name;
    }
    public Role() {}
    public Long getId() {
        return id;
    }


    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return name.toString();
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name=" + name +
                '}';
    }
}
