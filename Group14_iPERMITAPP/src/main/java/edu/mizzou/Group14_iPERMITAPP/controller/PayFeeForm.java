package edu.mizzou.Group14_iPERMITAPP.controller;

import edu.mizzou.Group14_iPERMITAPP.model.Payment;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.repository.PaymentRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;
import edu.mizzou.Group14_iPERMITAPP.service.AcknowledgeEOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.UUID;

@Controller
public class PayFeeForm { //aka PermitPaymentForm in workbook


    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AcknowledgeEOService acknowledgeEOService;

    @Autowired
    private PermitRequestRepository permitRequestRepository;

    @PostMapping("/re/pay/success")
    public String paySuccess() {
        return "redirect:/re/dashboard?paid=true";
    }

    @PostMapping("/re/pay/fail")
    public String payFail() {
        return "redirect:/re/dashboard?paid=false";
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
}
