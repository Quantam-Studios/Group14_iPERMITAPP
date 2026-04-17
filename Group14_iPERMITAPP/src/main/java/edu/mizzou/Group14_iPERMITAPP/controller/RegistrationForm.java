package edu.mizzou.Group14_iPERMITAPP.controller;
import edu.mizzou.Group14_iPERMITAPP.model.EO;
import edu.mizzou.Group14_iPERMITAPP.model.EnvironmentalPermit;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.model.RequestStatus;
import edu.mizzou.Group14_iPERMITAPP.repository.EORepository;
import edu.mizzou.Group14_iPERMITAPP.repository.EnvironmentalPermitRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.RequestStatusRepository;
import edu.mizzou.Group14_iPERMITAPP.service.AcknowledgeEOService;
import edu.mizzou.Group14_iPERMITAPP.service.RegisterService;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class RegistrationForm { // aka ministry's website

    @Autowired
    private AcknowledgeEOService acknowledgeEOService;
    
    @Autowired
    private EnvironmentalPermitRepository permitRepository;
    
    @Autowired
    private RequestStatusRepository requestStatusRepository;

    @Autowired
    private PermitRequestRepository permitRequestRepository;
    
    @Autowired
    private EORepository eoRepository;

    @Autowired
    private RegisterService registerService;

    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String email, @RequestParam String password, HttpSession session) {
        boolean success = registerService.login(email, password);

        if (success) {

            if (email.equals("environmentalministry158@gmail.com")) {
                session.setAttribute("userType", "EO");
                session.setAttribute("eo-id", "EO-001");
                return "redirect:/eo/dashboard";
            }

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
                                 @RequestParam String organizationAddress, @RequestParam String email, @RequestParam String password,
                                 @RequestParam String siteAddress, @RequestParam String siteContactPerson) {

        String result = registerService.register(contactPersonName, organizationName, organizationAddress, email,
                password, siteAddress, siteContactPerson);

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


    @GetMapping("/eo/dashboard")
    public String eoDashboard(HttpSession session) {

        String userType = (String) session.getAttribute("userType");

        // optional security check
        if (userType == null || !userType.equals("EO")) {
            return "redirect:/login";
        }

        return "eo/dashboard";
    }

    @GetMapping("/eo/permits")
    public String viewPermits(HttpSession session, Model model) {

        String userType = (String) session.getAttribute("userType");

        if (userType == null || !userType.equals("EO")) {
            return "redirect:/login";
        }

        // Get only PAID permits from your service
        List<PermitRequest> permits = acknowledgeEOService.getValidPermitRequests();
        Map<String, String> statusMap = acknowledgeEOService.getLatestStatusMap();

        model.addAttribute("permits", permits);
        model.addAttribute("statusMap", statusMap);
        return "eo/permits";
    }

    @GetMapping("/eo/account")
    public String accountPage(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        if (userType == null || !userType.equals("EO")) return "redirect:/login";

        String eoId = (String) session.getAttribute("eo-id");
        EO eo = eoRepository.findById(eoId).orElse(null);
        model.addAttribute("eo", eo);

        return "eo/account";
    }

    @PostMapping("/eo/account/update-password")
    public String updatePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            HttpSession session) {
        
        String eoId = (String) session.getAttribute("eo-id");
        EO eo = eoRepository.findById(eoId).orElse(null);

        if (eo == null || !eo.getPassword().equals(currentPassword)) {
            return "redirect:/eo/account?error=password";
        }

        eo.setPassword(newPassword);
        eoRepository.save(eo);

        return "redirect:/eo/account?success=true";
    }
    
    @GetMapping("/eo/permits/create")
    public String createPermitPage(HttpSession session) {
        String userType = (String) session.getAttribute("userType");
        if (userType == null || !userType.equals("EO")) return "redirect:/login";
        return "eo/create-permit";
    }

    @PostMapping("/eo/permits/create")
    public String savePermit(
            @RequestParam String permitID,
            @RequestParam String permitName,
            @RequestParam Double permitFee,
            @RequestParam String description,
            HttpSession session) {
                
        String userType = (String) session.getAttribute("userType");
        if (userType == null || !userType.equals("EO")) return "redirect:/login";

        EnvironmentalPermit newPermit = new EnvironmentalPermit();
        newPermit.setPermitID(permitID);
        newPermit.setPermitName(permitName);
        newPermit.setPermitFee(permitFee);
        newPermit.setDescription(description);

        permitRepository.save(newPermit);

        return "redirect:/eo/dashboard";
    }  
    
    @GetMapping("/eo/reports")
    public String viewAllReports(HttpSession session, Model model) {
        String userType = (String) session.getAttribute("userType");
        if (userType == null || !userType.equals("EO")) return "redirect:/login";

        List<PermitRequest> allRequests = permitRequestRepository.findAll();
        
        // Create a status map for all requests
        Map<String, String> statusMap = new HashMap<>();
        List<RequestStatus> allStatuses = requestStatusRepository.findAll();
        for (RequestStatus rs : allStatuses) {
            statusMap.put(rs.getPermitRequest().getRequestNo(), rs.getPermitRequestStatus());
        }

        model.addAttribute("requests", allRequests);
        model.addAttribute("statusMap", statusMap);
        
        return "eo/reports";
    }

    public void login(String uname, String passw){

    }

    public void validate(String uname, String passw){

    }
}
