package com.kobal.FileStorageApp.user;

import com.kobal.FileStorageApp.FileMetaData;
import jakarta.persistence.*;

import java.io.File;
import java.util.List;

@Entity
@Table(name = "AppUser")
public class AppUser {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    private Long id;

    private String username;
    private String password;

}
