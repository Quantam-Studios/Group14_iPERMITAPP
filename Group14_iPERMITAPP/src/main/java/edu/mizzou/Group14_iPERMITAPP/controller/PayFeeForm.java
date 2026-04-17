package edu.mizzou.Group14_iPERMITAPP.controller;

// Import model classes used for payment and permit request data
import edu.mizzou.Group14_iPERMITAPP.model.Payment;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;

// Import repositories for database access
import edu.mizzou.Group14_iPERMITAPP.repository.PaymentRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;

// Import service layer used for business logic after payment
import edu.mizzou.Group14_iPERMITAPP.service.AcknowledgeEOService;

// Spring framework imports for dependency injection and web handling
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.UUID;

// Marks this class as a Spring MVC controller handling web requests
@Controller
public class PayFeeForm { // aka PermitPaymentForm in workbook


    // Repository used to save and retrieve Payment records from database
    @Autowired
    private PaymentRepository paymentRepository;

    // Service responsible for handling acknowledgment logic after payment
    @Autowired
    private AcknowledgeEOService acknowledgeEOService;

    // Repository used to retrieve PermitRequest data from database
    @Autowired
    private PermitRequestRepository permitRequestRepository;

    // Redirect endpoint after successful payment submission
    @PostMapping("/re/pay/success")
    public String paySuccess() {
        return "redirect:/re/dashboard?paid=true";
    }

    // Redirect endpoint after failed payment submission
    @PostMapping("/re/pay/fail")
    public String payFail() {
        return "redirect:/re/dashboard?paid=false";
    }

    // Displays the payment page for a given permit request
    @GetMapping("/re/pay")
    public String payPage(@RequestParam String requestNo, Model model) {

        // Retrieve the permit request using request number
        PermitRequest request = permitRequestRepository.findById(requestNo)
                .orElse(null);

        // If request does not exist, redirect with error
        if (request == null) {
            return "redirect:/re/dashboard?error=notfound";
        }

        // Add request object to view model for rendering in UI
        model.addAttribute("request", request);

        // Add permit fee to model for display on payment page
        model.addAttribute("fee", request.getEnvironmentalPermit().getPermitFee());

        return "re/pay"; // returns the payment HTML page
    }

    // Handles submission of payment form
    @PostMapping("/re/pay")
    public String handlePayment(@RequestParam String requestNo,
                                @RequestParam String action) {

        // Retrieve permit request again from database
        PermitRequest request = permitRequestRepository.findById(requestNo)
                .orElse(null);

        // If request not found, redirect with error
        if (request == null) {
            return "redirect:/re/dashboard?error=notfound";
        }

        // If user chose to pay
        if (action.equals("pay")) {

            // Create new Payment record
            Payment payment = new Payment();

            // Assign unique ID to payment
            payment.setPaymentID(UUID.randomUUID().toString());

            // Set current date as payment date
            payment.setPaymentDate(new Date());

            // Hardcoded payment method (placeholder)
            payment.setPaymentMethod("CARD");

            // Placeholder card details (not secure, likely for testing only)
            payment.setLast4DigitsofCard(1234); // placeholder
            payment.setCardHolderName("Test User"); // placeholder

            // Mark payment as approved
            payment.setPaymentApproved(true);

            // Link payment to permit request
            payment.setPermitRequest(request);

            // Save payment to database
            paymentRepository.save(payment);

            // Trigger EO acknowledgment logic after successful payment
            acknowledgeEOService.acceptPayment(request);

            // Redirect with success flag
            return "redirect:/re/dashboard?paid=true";

        } else {
            // If action is not "pay", treat as failed/canceled payment
            return "redirect:/re/dashboard?paid=false";
        }
    }
}
