package edu.mizzou.Group14_iPERMITAPP.controller;

import edu.mizzou.Group14_iPERMITAPP.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

	@Autowired
	private RegisterService registerService;

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

	@PostMapping("/login")
	public String handleLogin(@RequestParam String email, @RequestParam String password) {

		boolean success = registerService.login(email, password);

		if (success) {
			return "redirect:/re/dashboard";
		}

		return "redirect:/login?error=true";
	}

	@PostMapping("/register")
	public String handleRegister(@RequestParam String contactPersonName, @RequestParam String organizationName,
			@RequestParam String organizationAddress, @RequestParam String email, @RequestParam String password) {

		boolean success = registerService.register(contactPersonName, organizationName, organizationAddress, email,
				password);

		if (success) {
			return "redirect:/login?registered=true";
		}

		return "redirect:/login?error=true";
	}
}