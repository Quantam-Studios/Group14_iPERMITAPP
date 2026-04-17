package edu.mizzou.Group14_iPERMITAPP.controller;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;
import edu.mizzou.Group14_iPERMITAPP.service.AcknowledgeEOService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;

@Controller
public class RegistrationForm { // aka ministry's website

    @Autowired
    private AcknowledgeEOService acknowledgeEOService;

    @GetMapping("/eo/dashboard")
    public String eoDashboard(HttpSession session) {

        String userType = (String) session.getAttribute("userType");

        // optional security check
        if (userType == null || !userType.equals("EO")) {
            return "redirect:/login";
        }

        return "eo/dashboard"; // this is your HTML file name (without .html)
    }

    @GetMapping("/eo/permits")
    public String viewPermits(HttpSession session, Model model) {

        // Optional: ensure only EO can access this page
        String userType = (String) session.getAttribute("userType");

        if (userType == null || !userType.equals("EO")) {
            return "redirect:/login";
        }

        // Get only PAID permits from your service
        List<PermitRequest> permits = acknowledgeEOService.getValidPermitRequests();
        model.addAttribute("permits", permits);

        return "eo/permits";
    }

    public void login(String uname, String passw){

    }

    public void validate(String uname, String passw){

    }
}
