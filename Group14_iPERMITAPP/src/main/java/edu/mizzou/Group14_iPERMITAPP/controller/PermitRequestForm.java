package edu.mizzou.Group14_iPERMITAPP.controller;


import edu.mizzou.Group14_iPERMITAPP.model.*;
import edu.mizzou.Group14_iPERMITAPP.repository.*;
import edu.mizzou.Group14_iPERMITAPP.service.AcknowledgeEOService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.UUID;

@Controller
public class PermitRequestForm {

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

    @GetMapping("/re/submit")
    public String submitPermitPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("userEmail");
        RE user = reRepository.findByEmail(email);

        model.addAttribute("permits", environmentalPermitRepository.findAll());
        model.addAttribute("sites", user.getSites());

        return "re/submit-permit";
    }

    @PostMapping("/re/submit")
    public String submitPermit(@RequestParam String activityDescription, @RequestParam String activityStartDate,
                               @RequestParam String activityDuration, @RequestParam String permitId, @RequestParam Long siteId, HttpSession session) {

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

        RESite site = user.getSites().stream()
                .filter(s -> s.getId().equals(siteId))
                .findFirst()
                .orElse(null);

        if (site == null) return "redirect:/re/submit?error=site";

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
        request.setSite(site);

        request.setEnvironmentalPermit(permit);

        permitRequestRepository.save(request);

        return "redirect:/re/pay?requestNo=" + request.getRequestNo();
    }

}
