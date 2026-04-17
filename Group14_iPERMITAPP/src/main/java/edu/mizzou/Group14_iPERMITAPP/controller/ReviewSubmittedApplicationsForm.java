package edu.mizzou.Group14_iPERMITAPP.controller;

// Model imports for EO users and permit request data
import edu.mizzou.Group14_iPERMITAPP.model.EO;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;

// Repository imports for database access
import edu.mizzou.Group14_iPERMITAPP.repository.EORepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.RequestStatusRepository;

// Service layer handling approval/rejection business logic
import edu.mizzou.Group14_iPERMITAPP.service.ReviewSubmittedApplicationsService;

// HTTP session for authentication and role checking
import jakarta.servlet.http.HttpSession;

// Spring MVC imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Marks this class as a Spring MVC controller for EO permit review workflow
@Controller
public class ReviewSubmittedApplicationsForm {

    // Repository for accessing permit request data
    @Autowired
    private PermitRequestRepository permitRequestRepository;

    // Repository for EO user data
    @Autowired
    private EORepository eoRepository;

    // Service handling approval/rejection logic for permit applications
    @Autowired
    private ReviewSubmittedApplicationsService reviewSubmittedApplicationsService;

    // Repository for tracking request status history (not directly used here but part of workflow)
    @Autowired
    private RequestStatusRepository requestStatusRepository;

    // Displays detailed view of a specific permit request (EO only)
    @GetMapping("/eo/permits/{requestNo}")
    public String viewPermitDetail(
            @PathVariable String requestNo,
            HttpSession session,
            Model model
    ) {

        // Ensure only EO users can access this endpoint
        String userType = (String) session.getAttribute("userType");
        if (userType == null || !userType.equals("EO")) {
            return "redirect:/login";
        }

        // Retrieve permit request from database
        PermitRequest permit = permitRequestRepository
                .findById(requestNo)
                .orElse(null);

        // Redirect if permit not found
        if (permit == null) {
            return "redirect:/eo/permits?error=notfound";
        }

        // Add permit data to model for display in view
        model.addAttribute("permit", permit);

        // Store current request in service (used for later processing/decision workflow)
        reviewSubmittedApplicationsService.setCurrentRequest(permit);

        return "eo/permit-detail";
    }

    // Handles EO decision (approve or reject) for a permit request
    @PostMapping("/eo/permits/{requestNo}/decision")
    public String decidePermit(
            @PathVariable String requestNo,
            @RequestParam String decision,
            @RequestParam(required = false, defaultValue = "") String reason,
            HttpSession session
    ) {

        // Ensure EO-only access
        String userType = (String) session.getAttribute("userType");
        if (userType == null || !userType.equals("EO")) {
            return "redirect:/login";
        }

        // Retrieve EO identity from session
        String eoId = (String) session.getAttribute("eo-id");

        // Fetch EO user and permit request from database
        EO eo = (eoId != null) ? eoRepository.findById(eoId).orElse(null) : null;
        PermitRequest permit = permitRequestRepository.findById(requestNo).orElse(null);

        // Validate required data exists before processing
        if (eo == null || permit == null) {
            return "redirect:/eo/permits?error=missing_data";
        }

        try {

            // Process approval decision
            if ("APPROVE".equals(decision)) {
                reviewSubmittedApplicationsService.approveRequest(permit, eo, reason);

                // Process rejection decision
            } else if ("REJECT".equals(decision)) {
                reviewSubmittedApplicationsService.rejectRequest(permit, eo, reason);
            }

        } catch (Exception e) {
            // Log error for debugging (basic exception handling)
            e.printStackTrace();
            return "redirect:/eo/permits?error=service_failed";
        }

        // Redirect back to EO permits list after decision is processed
        return "redirect:/eo/permits";
    }

}
