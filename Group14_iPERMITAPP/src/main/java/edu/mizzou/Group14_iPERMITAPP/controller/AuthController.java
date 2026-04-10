package edu.mizzou.Group14_iPERMITAPP.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // loads login.html
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String email,
                              @RequestParam String password) {

        // TODO: replace with real authentication
        if (email.equals("test@test.com") && password.equals("1234")) {
            return "redirect:/home";
        }

        return "redirect:/login?error=true";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam String email,
                                 @RequestParam String password) {

        // TODO: save user to database
        System.out.println("Registering: " + email);

        return "redirect:/login?registered=true";
    }
}