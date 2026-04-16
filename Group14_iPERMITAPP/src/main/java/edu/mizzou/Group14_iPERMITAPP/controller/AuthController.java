package edu.mizzou.Group14_iPERMITAPP.controller;

import edu.mizzou.Group14_iPERMITAPP.service.RegisterService;
import jakarta.servlet.http.HttpSession;

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
	public String handleLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {

		if (email.equals("environmentalministry158@gmail.com") && password.equals("Ipermit123")) {
			session.setAttribute("userType", "EO");
			return "redirect:/eo/dashboard";
		}

		boolean success = registerService.login(email, password);

		if (success) {
			session.setAttribute("userEmail", email);
			return "redirect:/re/dashboard";
		}

		return "redirect:/login?error=true";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login";
	}

	@PostMapping("/register")
	public String handleRegister(@RequestParam String contactPersonName, @RequestParam String organizationName,
			@RequestParam String organizationAddress, @RequestParam String email, @RequestParam String password) {

		String result = registerService.register(contactPersonName, organizationName, organizationAddress, email,
				password);

		switch (result) {
		case "SUCCESS":
			return "redirect:/login?registered=true";

		case "EMAIL_EXISTS":
			return "redirect:/login?error=exists&mode=register";

		case "INVALID_EMAIL":
			return "redirect:/login?error=email&mode=register";

		case "EMPTY_FIELDS":
			return "redirect:/login?error=empty&mode=register";

		default:
			return "redirect:/login?error=true";
		}
	}
}