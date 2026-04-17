package edu.mizzou.Group14_iPERMITAPP.controller;

// Model imports for EO (Environmental Officer), permits, requests, and status tracking
import edu.mizzou.Group14_iPERMITAPP.model.EO;
import edu.mizzou.Group14_iPERMITAPP.model.EnvironmentalPermit;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.model.RequestStatus;

// Repository imports for database access layer
import edu.mizzou.Group14_iPERMITAPP.repository.EORepository;
import edu.mizzou.Group14_iPERMITAPP.repository.EnvironmentalPermitRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.RequestStatusRepository;

// Service layer imports for business logic
import edu.mizzou.Group14_iPERMITAPP.service.AcknowledgeEOService;
import edu.mizzou.Group14_iPERMITAPP.service.RegisterService;

// HTTP session for login state tracking
import jakarta.servlet.http.HttpSession;

// Spring MVC annotations and model support
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

// Java utilities
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Marks this class as a Spring MVC controller (handles login, EO, and RE navigation)
@Controller
public class RegistrationForm { // aka ministry's website

    // Service handling EO acknowledgment logic after permit actions
    @Autowired
    private AcknowledgeEOService acknowledgeEOService;

    // Repository for managing environmental permit definitions
    @Autowired
    private EnvironmentalPermitRepository permitRepository;

    // Repository for tracking request status history
    @Autowired
    private RequestStatusRepository requestStatusRepository;

    // Repository for permit requests submitted by RE users
    @Autowired
    private PermitRequestRepository permitRequestRepository;

    // Repository for EO user data
    @Autowired
    private EORepository eoRepository;

    // Service handling login and registration logic
    @Autowired
    private RegisterService registerService;

    // Root URL redirects to login page
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    // Displays login page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Handles login form submission
    @PostMapping("/login")
    public String handleLogin(@RequestParam String email,
                              @RequestParam String password,
                              HttpSession session) {

        // Validate credentials using service layer
        boolean success = registerService.login(email, password);

        if (success) {

            // Hardcoded EO login check (admin-style account)
            if (email.equals("environmentalministry158@gmail.com")) {
                session.setAttribute("userType", "EO");
                session.setAttribute("eo-id", "EO-001");
                return "redirect:/eo/dashboard";
            }

            // Otherwise treat as RE user
            session.setAttribute("userEmail", email);
            return "redirect:/re/dashboard";
        }

        // Redirect back to login if authentication fails
        return "redirect:/login?error=true";
    }

    // Logs user out by clearing session
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // Handles new user registration
    @PostMapping("/register")
    public String handleRegister(@RequestParam String contactPersonName,
                                 @RequestParam String organizationName,
                                 @RequestParam String organizationAddress,
                                 @RequestParam String email,
                                 @RequestParam String password,
                                 @RequestParam String siteAddress,
                                 @RequestParam String siteContactPerson) {

        // Call service layer to process registration logic
        String result = registerService.register(contactPersonName,
                organizationName,
                organizationAddress,
                email,
                password,
                siteAddress,
                siteContactPerson);

        // Handle registration outcome states
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

    // EO dashboard page (restricted access)
    @GetMapping("/eo/dashboard")
    public String eoDashboard(HttpSession session) {

        // Ensure only EO users can access
        String userType = (String) session.getAttribute("userType");

        if (userType == null || !userType.equals("EO")) {
            return "redirect:/login";
        }

        return "eo/dashboard";
    }

    // EO view of approved/valid permit requests
    @GetMapping("/eo/permits")
    public String viewPermits(HttpSession session, Model model) {

        // Access control check
        String userType = (String) session.getAttribute("userType");

        if (userType == null || !userType.equals("EO")) {
            return "redirect:/login";
        }

        // Retrieve only valid (paid/approved) permits
        List<PermitRequest> permits = acknowledgeEOService.getValidPermitRequests();

        // Map of latest status per permit request
        Map<String, String> statusMap = acknowledgeEOService.getLatestStatusMap();

        // Add data to view model
        model.addAttribute("permits", permits);
        model.addAttribute("statusMap", statusMap);

        return "eo/permits";
    }

    // EO account page view
    @GetMapping("/eo/account")
    public String accountPage(HttpSession session, Model model) {

        // Ensure EO access only
        String userType = (String) session.getAttribute("userType");
        if (userType == null || !userType.equals("EO")) return "redirect:/login";

        // Retrieve EO account using session ID
        String eoId = (String) session.getAttribute("eo-id");
        EO eo = eoRepository.findById(eoId).orElse(null);

        // Add EO data to model
        model.addAttribute("eo", eo);

        return "eo/account";
    }

    // EO password update handler
    @PostMapping("/eo/account/update-password")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 HttpSession session) {

        // Get EO identity from session
        String eoId = (String) session.getAttribute("eo-id");
        EO eo = eoRepository.findById(eoId).orElse(null);

        // Validate current password before updating
        if (eo == null || !eo.getPassword().equals(currentPassword)) {
            return "redirect:/eo/account?error=password";
        }

        // Update password and save
        eo.setPassword(newPassword);
        eoRepository.save(eo);

        return "redirect:/eo/account?success=true";
    }

    // Page to create new environmental permit (EO only)
    @GetMapping("/eo/permits/create")
    public String createPermitPage(HttpSession session) {

        String userType = (String) session.getAttribute("userType");

        if (userType == null || !userType.equals("EO")) return "redirect:/login";

        return "eo/create-permit";
    }

    // Handles creation of new environmental permit
    @PostMapping("/eo/permits/create")
    public String savePermit(@RequestParam String permitID,
                             @RequestParam String permitName,
                             @RequestParam Double permitFee,
                             @RequestParam String description,
                             HttpSession session) {

        // Ensure EO access only
        String userType = (String) session.getAttribute("userType");

        if (userType == null || !userType.equals("EO")) return "redirect:/login";

        // Create new permit object
        EnvironmentalPermit newPermit = new EnvironmentalPermit();
        newPermit.setPermitID(permitID);
        newPermit.setPermitName(permitName);
        newPermit.setPermitFee(permitFee);
        newPermit.setDescription(description);

        // Save to database
        permitRepository.save(newPermit);

        return "redirect:/eo/dashboard";
    }

    // EO reports page showing all permit requests
    @GetMapping("/eo/reports")
    public String viewAllReports(HttpSession session, Model model) {

        // Access control check
        String userType = (String) session.getAttribute("userType");

        if (userType == null || !userType.equals("EO")) return "redirect:/login";

        // Retrieve all permit requests
        List<PermitRequest> allRequests = permitRequestRepository.findAll();

        // Build status map for all requests
        Map<String, String> statusMap = new HashMap<>();
        List<RequestStatus> allStatuses = requestStatusRepository.findAll();

        for (RequestStatus rs : allStatuses) {
            statusMap.put(rs.getPermitRequest().getRequestNo(),
                    rs.getPermitRequestStatus());
        }

        // Add data to view model
        model.addAttribute("requests", allRequests);
        model.addAttribute("statusMap", statusMap);

        return "eo/reports";
    }

    // Placeholder login method (not implemented)
    public void login(String uname, String passw) {

    }

    // Placeholder validation method (not implemented)
    public void validate(String uname, String passw) {

    }
}
