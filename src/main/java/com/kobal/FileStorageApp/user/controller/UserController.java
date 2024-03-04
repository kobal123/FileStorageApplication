package com.kobal.FileStorageApp.user.controller;


import com.kobal.FileStorageApp.file.filecontroller.RestFileController;
import com.kobal.FileStorageApp.user.service.UserService;
import com.kobal.FileStorageApp.user.model.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(RestFileController.class);
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("register")
    public ResponseEntity<Void> register(JwtAuthenticationToken authentication) {
        long start = System.currentTimeMillis();

        Jwt jwt = ((Jwt)authentication.getPrincipal());
        AppUser user = userService.register(jwt);
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        logger.info("register endpoint, user id: %s, time taken %s".formatted(user.getId(), timeElapsed));
        return ResponseEntity.ok().build();
    }
}
