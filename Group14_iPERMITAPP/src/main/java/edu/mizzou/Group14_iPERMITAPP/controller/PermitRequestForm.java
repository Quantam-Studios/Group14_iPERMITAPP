package edu.mizzou.Group14_iPERMITAPP.controller;

// Import domain models used in permit submission workflow
import edu.mizzou.Group14_iPERMITAPP.model.*;

// Import repositories for database access
import edu.mizzou.Group14_iPERMITAPP.repository.*;

// Service used for post-processing actions (e.g., EO acknowledgment)
import edu.mizzou.Group14_iPERMITAPP.service.AcknowledgeEOService;

// HTTP session handling for logged-in user tracking
import jakarta.servlet.http.HttpSession;

// Spring MVC and dependency injection imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.UUID;

// Marks this class as a Spring MVC controller handling permit request forms
@Controller
public class PermitRequestForm {

    // Repository for Resident Engineer (RE) user data
    @Autowired
    private RERepository reRepository;

    // Repository for available environmental permits
    @Autowired
    private EnvironmentalPermitRepository environmentalPermitRepository;

    // Repository for storing and retrieving permit requests
    @Autowired
    private PermitRequestRepository permitRequestRepository;

    // Repository for payment records (not directly used here but available for workflow continuity)
    @Autowired
    private PaymentRepository paymentRepository;

    // Service to trigger EO acknowledgment logic after workflow steps
    @Autowired
    private AcknowledgeEOService acknowledgeEOService;

    // Displays the permit submission page
    @GetMapping("/re/submit")
    public String submitPermitPage(HttpSession session, Model model) {

        // Retrieve logged-in user's email from session
        String email = (String) session.getAttribute("userEmail");

        // Fetch user from database
        RE user = reRepository.findByEmail(email);

        // Add all available permits to the model for dropdown selection
        model.addAttribute("permits", environmentalPermitRepository.findAll());

        // Add user's sites to model for site selection
        model.addAttribute("sites", user.getSites());

        return "re/submit-permit"; // returns the HTML view
    }

    // Handles submission of a new permit request
    @PostMapping("/re/submit")
    public String submitPermit(@RequestParam String activityDescription,
                               @RequestParam String activityStartDate,
                               @RequestParam String activityDuration,
                               @RequestParam String permitId,
                               @RequestParam Long siteId,
                               HttpSession session) {

        // Ensure user is logged in
        if (session.getAttribute("userEmail") == null) {
            return "redirect:/login";
        }

        // Basic validation: ensure end date is not before start date
        if (activityStartDate != null && activityDuration != null) {
            if (java.sql.Date.valueOf(activityDuration)
                    .before(java.sql.Date.valueOf(activityStartDate))) {
                return "redirect:/re/submit?error=date";
            }
        }

        // Retrieve logged-in user
        String email = (String) session.getAttribute("userEmail");
        RE user = reRepository.findByEmail(email);

        // Redirect if user not found
        if (user == null) {
            return "redirect:/login?error=nouser";
        }

        // Retrieve selected environmental permit
        EnvironmentalPermit permit =
                environmentalPermitRepository.findById(permitId).orElse(null);

        // Redirect if permit does not exist
        if (permit == null) {
            return "redirect:/re/submit?error=permit";
        }

        // Find the site belonging to the user matching selected siteId
        RESite site = user.getSites().stream()
                .filter(s -> s.getId().equals(siteId))
                .findFirst()
                .orElse(null);

        // Redirect if site is invalid or not owned by user
        if (site == null) return "redirect:/re/submit?error=site";

        // Create a new permit request object
        PermitRequest request = new PermitRequest();

        // Generate unique request number (system-generated as required)
        request.setRequestNo(UUID.randomUUID().toString());

        // Set request creation date
        request.setDateOfRequest(new Date());

        // Store activity details from form input
        request.setActivityDescription(activityDescription);

        // Convert and assign start and duration dates
        try {
            request.setActivityStartDate(java.sql.Date.valueOf(activityStartDate));
            request.setActivityDuration(java.sql.Date.valueOf(activityDuration));
        } catch (Exception e) {
            // Handle invalid date format input
            return "redirect:/re/submit?error=date";
        }

        // Set permit fee based on selected permit
        request.setPermitFee(permit.getPermitFee());

        // Link request to RE user
        request.setRe(user);

        // Link request to selected site
        request.setSite(site);

        // Link request to selected environmental permit
        request.setEnvironmentalPermit(permit);

        // Save permit request to database
        permitRequestRepository.save(request);

        // Redirect user to payment page for this request
        return "redirect:/re/pay?requestNo=" + request.getRequestNo();
    }

}