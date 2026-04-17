package edu.mizzou.Group14_iPERMITAPP.controller;

// Standard Java utilities for collections, dates, regex, and UUID generation
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

// Repository imports for database access
import edu.mizzou.Group14_iPERMITAPP.repository.*;

// Model import for request status tracking
import edu.mizzou.Group14_iPERMITAPP.model.RequestStatus;

// Spring framework imports for MVC and dependency injection
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Domain model imports used throughout controller
import edu.mizzou.Group14_iPERMITAPP.model.EnvironmentalPermit;
import edu.mizzou.Group14_iPERMITAPP.model.Payment;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.model.RE;
import edu.mizzou.Group14_iPERMITAPP.model.RESite;

// Service layer import (declared but not used in this class)
import edu.mizzou.Group14_iPERMITAPP.service.AcknowledgeEOService;

// HTTP session for tracking logged-in user state
import jakarta.servlet.http.HttpSession;

// Marks this class as a Spring MVC controller
@Controller
public class REController {

	// Repository for RE (user) data
	@Autowired
	private RERepository reRepository;

	// Repository for permit request records
	@Autowired
	private PermitRequestRepository permitRequestRepository;

	// Repository for payment records
	@Autowired
	private PaymentRepository paymentRepository;

	// Repository for tracking request status history
	@Autowired
	private RequestStatusRepository requestStatusRepository;

	// Regex pattern used to validate email format
	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	// Loads RE dashboard page
	@GetMapping("/re/dashboard")
	public String dashboard() {
		return "re/dashboard";
	}

	// Displays list of permit requests for logged-in RE user
	@GetMapping("/re/requests")
	public String myRequests(HttpSession session, Model model) {

		// Retrieve logged-in user's email from session
		String email = (String) session.getAttribute("userEmail");

		// Redirect to login if session is missing
		if (email == null) {
			return "redirect:/login";
		}

		// Fetch user from database
		RE user = reRepository.findByEmail(email);

		// Redirect if user does not exist
		if (user == null) {
			return "redirect:/login";
		}

		// Get all permit requests belonging to this user
		List<PermitRequest> requests = permitRequestRepository.findByRe(user);

		// Map to track whether each request has been paid
		Map<String, Boolean> paidMap = new HashMap<>();

		// Map to store latest status of each request
		Map<String, String> statusMap = new HashMap<>();

		// Retrieve latest status entries from database
		List<RequestStatus> latestStatuses = requestStatusRepository.findLatestStatuses();

		// Populate status map using request number as key
		for (RequestStatus rs : latestStatuses) {
			statusMap.put(rs.getPermitRequest().getRequestNo(),
					rs.getPermitRequestStatus());
		}

		// Build payment and default status maps for each request
		for (PermitRequest r : requests) {

			// Check if a payment exists for this request
			boolean paid = paymentRepository.findByPermitRequest(r) != null;
			paidMap.put(r.getRequestNo(), paid);

			// If no status exists, default to "Pending Payment"
			if (!statusMap.containsKey(r.getRequestNo())) {
				statusMap.put(r.getRequestNo(), "Pending Payment");
			}
		}

		// Add data to model for rendering in view
		model.addAttribute("requests", requests);
		model.addAttribute("paidMap", paidMap);
		model.addAttribute("statusMap", statusMap);

		return "re/my-requests";
	}

	// Displays account page for logged-in user
	@GetMapping("/re/account")
	public String accountPage(HttpSession session, Model model) {

		// Get email from session
		String email = (String) session.getAttribute("userEmail");

		// Redirect if not logged in
		if (email == null) {
			return "redirect:/login";
		}

		// Fetch user from database
		RE user = reRepository.findByEmail(email);

		// Redirect if user not found
		if (user == null) {
			return "redirect:/login?error=nouser";
		}

		// Add user object to model for display
		model.addAttribute("user", user);

		return "re/account";
	}

	// Handles account update form submission
	@PostMapping("/re/account/update")
	public String updateAccount(
			@RequestParam String contactPersonName,
			@RequestParam String organizationName,
			@RequestParam String organizationAddress,
			@RequestParam String email,
			@RequestParam(required = false) String currentPassword,
			@RequestParam(required = false) String newPassword,
			@RequestParam String siteAddress,
			@RequestParam String siteContactPerson,
			HttpSession session) {

		// Get current logged-in user email
		String sessionEmail = (String) session.getAttribute("userEmail");
		RE user = reRepository.findByEmail(sessionEmail);

		// Validate email format
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			return "redirect:/re/account?error=email";
		}

		// Check if new email is already used by another account
		RE existing = reRepository.findByEmail(email);
		if (existing != null && !email.equals(sessionEmail)) {
			return "redirect:/re/account?error=exists";
		}

		// Update basic user profile fields
		user.setContactPersonName(contactPersonName);
		user.setOrganizationName(organizationName);
		user.setOrganizationAddress(organizationAddress);
		user.setEmail(email);

		// Update or create site information
		if (user.getSites() == null || user.getSites().isEmpty()) {

			// Create new site if none exists
			RESite newSite = new RESite();
			newSite.setSiteAddress(siteAddress);
			newSite.setSiteContactPerson(siteContactPerson);
			newSite.setRe(user);

			List<RESite> sites = new ArrayList<>();
			sites.add(newSite);
			user.setSites(sites);

		} else {

			// Update first site (default behavior)
			RESite existingSite = user.getSites().get(0);
			existingSite.setSiteAddress(siteAddress);
			existingSite.setSiteContactPerson(siteContactPerson);
		}

		// Handle password change if provided
		if (newPassword != null && !newPassword.trim().isEmpty()) {

			// Validate current password before updating
			if (currentPassword == null || !user.getPassword().equals(currentPassword)) {
				return "redirect:/re/account?error=password";
			}

			user.setPassword(newPassword);
		}

		// Save updated user to database
		reRepository.save(user);

		// Update session email if changed
		session.setAttribute("userEmail", email);

		return "redirect:/re/account?success=true";
	}
}