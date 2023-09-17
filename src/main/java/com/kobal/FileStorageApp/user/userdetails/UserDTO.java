package com.kobal.FileStorageApp.user.userdetails;

import com.kobal.FileStorageApp.user.role.Role;
import java.util.Set;

public record UserDTO(Long id,
                      String name,
                      String email,
                      Set<Role> roles) {}
