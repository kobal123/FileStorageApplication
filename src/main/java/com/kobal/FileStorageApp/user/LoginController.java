package com.kobal.FileStorageApp.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class LoginController {
    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/register")
    String register() {
        return "register";
    }
}
