package edu.mizzou.Group14_iPERMITAPP.controller;

import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import model.Payment;
import model.PermitRequest;
import model.RequestStatus;
import model.EmailArchive;

import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class AcknowledgeEOController {

    public String acceptPayment(PermitRequest permit){
        if (payment.isPaymentApproved()) {

            // Step 1: Update the PermitRequest status to "Submitted"
            // (transitions from "Pending Payment" per the State Chart)
            updateStatus(permitRequest, "Submitted",
                    "Payment approved by OPS-CPP. Application submission complete.");

            // Step 2: Declare to EO that the submission is complete
            notifyEO(permitRequest);

            // Step 3: Send confirmation email to RE and archive it
            String reEmail = permitRequest.getRe().getEmail();
            sendAndArchiveEmail(reEmail, permitRequest, payment);

            return "Payment successful. Your application has been submitted. "
                    + "A confirmation email has been sent to: " + reEmail;

        } else {
            return "Payment was not approved by OPS-CPP. "
                    + "Please try again or contact support.";
        }
    }
    // ----------------------------------------------------------------
// Sends ONE confirmation email to RE upon successful payment
// Per workbook page 11: "send email to RE email account
// confirming their application submission"
// ----------------------------------------------------------------
    private void sendConfirmationEmail(String reEmail, PermitRequest permitRequest) {
        String host        = "smtp.gmail.com";
        String senderEmail = "ipermit.system@gmail.com";
        String senderPass  = "your_password_here";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPass);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(reEmail));
            message.setSubject("iPermit – Application Submission Confirmation");
            message.setText(
                    "Dear " + permitRequest.getRe().getContactPersonName() + ",\n\n"
                            + "Your payment was successfully processed and your permit "
                            + "application has been submitted.\n\n"
                            + "Request No : " + permitRequest.getRequestNo() + "\n"
                            + "Activity   : " + permitRequest.getActivityDescription() + "\n"
                            + "Permit Fee : $" + permitRequest.getPermitFee() + "\n\n"
                            + "The Environmental Officer will review your application shortly.\n\n"
                            + "Regards,\n"
                            + "iPermit System"
            );

            Transport.send(message);
            System.out.println("[Email Sent] Confirmation sent to: " + reEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("[Email Error] Could not send to: " + reEmail);
        }
    }
}
