package edu.mizzou.Group14_iPERMITAPP.controller;

import edu.mizzou.Group14_iPERMITAPP.model.EO;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.repository.EORepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;
import edu.mizzou.Group14_iPERMITAPP.service.ReviewSubmittedApplicationsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReviewSubmittedApplicationsForm {

    @Autowired
    private PermitRequestRepository permitRequestRepository;

    @Autowired
    private EORepository eoRepository;

    @Autowired
    private ReviewSubmittedApplicationsService reviewSubmittedApplicationsService;

    @GetMapping("/eo/permits/{requestNo}")
    public String viewPermitDetail(
            @PathVariable String requestNo,
            HttpSession session,
            Model model
    ) {

        // EO-only access check (optional but recommended)
        String userType = (String) session.getAttribute("userType");
        if (userType == null || !userType.equals("EO")) {
            return "redirect:/login";
        }

        // Fetch permit from DB
        PermitRequest permit = permitRequestRepository
                .findById(requestNo)
                .orElse(null);

        if (permit == null) {
            return "redirect:/eo/permits?error=notfound";
        }

        // Send to HTML
        model.addAttribute("permit", permit);

        return "eo/permit-detail";
    }

    @PostMapping("/eo/permits/{requestNo}/decision")
    public String decidePermit(
            @PathVariable String requestNo,
            @RequestParam String decision,
            @RequestParam String reason,
            HttpSession session
    ) {

        String userType = (String) session.getAttribute("userType");
        if (userType == null || !userType.equals("EO")) {
            return "redirect:/login";
        }

        String eoId = (String) session.getAttribute("eo-Id");
        EO eo = eoRepository.findById(eoId).orElse(null);
        PermitRequest permit = permitRequestRepository.findById(requestNo).orElse(null);

        if (eo == null || permit == null) {
            return "redirect:/eo/permits?error=missing_data";
        }
        
        if (decision.equals("APPROVE")) {
            reviewSubmittedApplicationsService.approveRequest(permit, eo, reason);
        }
        else if (decision.equals("REJECT")) {
            reviewSubmittedApplicationsService.rejectRequest(permit, eo, reason);
        }

        return "redirect:/eo/permits";
    }

    public void showPendingApprovalRequest(){

    }

    public void activatePendingApprovalRequest(){

    }

    public void submitApproval(){

    }

    public void submitRejection(){

    }

    public void emailRE(){

    }
}
