package edu.mizzou.Group14_iPERMITAPP.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import edu.mizzou.Group14_iPERMITAPP.repository.*;
import edu.mizzou.Group14_iPERMITAPP.model.RequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.mizzou.Group14_iPERMITAPP.model.EnvironmentalPermit;
import edu.mizzou.Group14_iPERMITAPP.model.Payment;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.model.RE;
import edu.mizzou.Group14_iPERMITAPP.model.RESite;
import edu.mizzou.Group14_iPERMITAPP.service.AcknowledgeEOService;
import jakarta.servlet.http.HttpSession;

@Controller
public class REController {
	@Autowired
	private RERepository reRepository;



	@Autowired
	private PermitRequestRepository permitRequestRepository;
	
	@Autowired
	private PaymentRepository paymentRepository;


	@Autowired
	private RequestStatusRepository requestStatusRepository;

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	@GetMapping("/re/dashboard")
	public String dashboard() {
		return "re/dashboard";
	}



	@GetMapping("/re/requests")
	public String myRequests(HttpSession session, Model model) {

		String email = (String) session.getAttribute("userEmail");

		if (email == null) {
			return "redirect:/login";
		}

		RE user = reRepository.findByEmail(email);

		if (user == null) {
			return "redirect:/login";
		}

		// Get this RE's requests
	    List<PermitRequest> requests = permitRequestRepository.findByRe(user);
	    
	    Map<String, Boolean> paidMap = new HashMap<>();
	    Map<String, String> statusMap = new HashMap<>();

	    // Get the latest status for each request
	    List<RequestStatus> latestStatuses = requestStatusRepository.findLatestStatuses();
	    for (RequestStatus rs : latestStatuses) {
	        statusMap.put(rs.getPermitRequest().getRequestNo(), rs.getPermitRequestStatus());
	    }

	    // Build Maps
	    for (PermitRequest r : requests) {
	        // Payment check
	        boolean paid = paymentRepository.findByPermitRequest(r) != null;
	        paidMap.put(r.getRequestNo(), paid);

	        // Status default
	        if (!statusMap.containsKey(r.getRequestNo())) {
	            statusMap.put(r.getRequestNo(), "Pending Payment");
	        }
	    }

		model.addAttribute("requests", requests);
		model.addAttribute("paidMap", paidMap);
		model.addAttribute("statusMap", statusMap);

		return "re/my-requests";
	}

	@GetMapping("/re/account")
	public String accountPage(HttpSession session, Model model) {

		String email = (String) session.getAttribute("userEmail");

		if (email == null) {
			return "redirect:/login";
		}

		RE user = reRepository.findByEmail(email);

		if (user == null) {
			return "redirect:/login?error=nouser";
		}

		model.addAttribute("user", user);

		return "re/account";
	}

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

	    String sessionEmail = (String) session.getAttribute("userEmail");
	    RE user = reRepository.findByEmail(sessionEmail);

	    // Email Format Validation
	    if (!EMAIL_PATTERN.matcher(email).matches()) {
	        return "redirect:/re/account?error=email";
	    }

	    // Check if email change conflicts with another user
	    RE existing = reRepository.findByEmail(email);
	    if (existing != null && !email.equals(sessionEmail)) {
	        return "redirect:/re/account?error=exists";
	    }

	    // Update Basic Profile Info
	    user.setContactPersonName(contactPersonName);
	    user.setOrganizationName(organizationName);
	    user.setOrganizationAddress(organizationAddress);
	    user.setEmail(email);

	    // Update or Create Site Info
	    if (user.getSites() == null || user.getSites().isEmpty()) {
	        // Create new site if list is empty
	        RESite newSite = new RESite();
	        newSite.setSiteAddress(siteAddress);
	        newSite.setSiteContactPerson(siteContactPerson);
	        newSite.setRe(user);
	        
	        List<RESite> sites = new ArrayList<>();
	        sites.add(newSite);
	        user.setSites(sites);
	    } else {
	        // Update the first site in the list (default)
	        RESite existingSite = user.getSites().get(0);
	        existingSite.setSiteAddress(siteAddress);
	        existingSite.setSiteContactPerson(siteContactPerson);
	    }

	    // Password Logic
	    if (newPassword != null && !newPassword.trim().isEmpty()) {
	        if (currentPassword == null || !user.getPassword().equals(currentPassword)) {
	            return "redirect:/re/account?error=password";
	        }
	        user.setPassword(newPassword);
	    }

	    // Save
	    reRepository.save(user);

	    session.setAttribute("userEmail", email);

	    return "redirect:/re/account?success=true";
	}
}