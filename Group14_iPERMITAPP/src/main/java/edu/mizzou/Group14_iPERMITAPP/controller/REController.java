package edu.mizzou.Group14_iPERMITAPP.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

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
import edu.mizzou.Group14_iPERMITAPP.repository.EnvironmentalPermitRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PaymentRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.RERepository;
import edu.mizzou.Group14_iPERMITAPP.service.AcknowledgeEOService;
import jakarta.servlet.http.HttpSession;

@Controller
public class REController {
	@Autowired
	private RERepository reRepository;

	@Autowired
	private EnvironmentalPermitRepository environmentalPermitRepository;

	@Autowired
	private PermitRequestRepository permitRequestRepository;
	
	@Autowired
	private PaymentRepository paymentRepository;
	
    @Autowired
    private AcknowledgeEOService acknowledgeEOService;

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	@GetMapping("/re/dashboard")
	public String dashboard() {
		return "re/dashboard";
	}

	@PostMapping("/re/pay/success")
	public String paySuccess() {
		return "redirect:/re/dashboard?paid=true";
	}

	@PostMapping("/re/pay/fail")
	public String payFail() {
		return "redirect:/re/dashboard?paid=false";
	}

	@GetMapping("/re/submit")
	public String submitPermitPage(Model model) {
	    // Fetch all permits from the database
	    List<EnvironmentalPermit> permits = environmentalPermitRepository.findAll();
	    
	    // Add them to the model
	    model.addAttribute("permits", permits);
	    
	    return "re/submit-permit";
	}

	@GetMapping("/re/pay")
	public String payPage(@RequestParam String requestNo, Model model) {

	    PermitRequest request = permitRequestRepository.findById(requestNo)
	            .orElse(null);

	    if (request == null) {
	        return "redirect:/re/dashboard?error=notfound";
	    }

	    model.addAttribute("request", request);
	    model.addAttribute("fee", request.getEnvironmentalPermit().getPermitFee());

	    return "re/pay";
	}

	@PostMapping("/re/pay")
	public String handlePayment(@RequestParam String requestNo,
	                            @RequestParam String action) {

	    PermitRequest request = permitRequestRepository.findById(requestNo)
	            .orElse(null);

	    if (request == null) {
	        return "redirect:/re/dashboard?error=notfound";
	    }

	    if (action.equals("pay")) {

	        Payment payment = new Payment();
	        payment.setPaymentID(UUID.randomUUID().toString());
	        payment.setPaymentDate(new Date());
	        payment.setPaymentMethod("CARD");
	        payment.setLast4DigitsofCard(1234); // placeholder
	        payment.setCardHolderName("Test User"); // placeholder
	        payment.setPaymentApproved(true);
	        payment.setPermitRequest(request);

	        paymentRepository.save(payment);
	        
	        acknowledgeEOService.acceptPayment(request); 

	        return "redirect:/re/dashboard?paid=true";

	    } else {
	        return "redirect:/re/dashboard?paid=false";
	    }
	}

	@PostMapping("/re/submit")
	public String submitPermit(@RequestParam String activityDescription, @RequestParam String activityStartDate,
			@RequestParam String activityDuration, @RequestParam String permitId, HttpSession session) {

		if (session.getAttribute("userEmail") == null) {
			return "redirect:/login";
		}

		if (activityStartDate != null && activityDuration != null) {
			if (java.sql.Date.valueOf(activityDuration).before(java.sql.Date.valueOf(activityStartDate))) {
				return "redirect:/re/submit?error=date";
			}
		}

		String email = (String) session.getAttribute("userEmail");
		RE user = reRepository.findByEmail(email);

		if (user == null) {
			return "redirect:/login?error=nouser";
		}

		EnvironmentalPermit permit =
		        environmentalPermitRepository.findById(permitId).orElse(null);

		if (permit == null) {
		    return "redirect:/re/submit?error=permit";
		}

		PermitRequest request = new PermitRequest();

		// system-generated sequence (workbook requirement)
		request.setRequestNo(UUID.randomUUID().toString());
		request.setDateOfRequest(new Date());
		request.setActivityDescription(activityDescription);
		try {
		    request.setActivityStartDate(java.sql.Date.valueOf(activityStartDate));
		    request.setActivityDuration(java.sql.Date.valueOf(activityDuration));
		} catch (Exception e) {
		    return "redirect:/re/submit?error=date";
		}

		request.setPermitFee(permit.getPermitFee());

		request.setRe(user);

		request.setEnvironmentalPermit(permit);

		permitRequestRepository.save(request);

		return "redirect:/re/pay?requestNo=" + request.getRequestNo();
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

	    List<PermitRequest> requests = permitRequestRepository.findByRe(user);

	    Map<String, Boolean> paidMap = new HashMap<>();

	    for (PermitRequest r : requests) {
	        boolean paid = paymentRepository.findByPermitRequest(r) != null;
	        paidMap.put(r.getRequestNo(), paid);
	    }

	    model.addAttribute("requests", requests);
	    model.addAttribute("paidMap", paidMap);

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