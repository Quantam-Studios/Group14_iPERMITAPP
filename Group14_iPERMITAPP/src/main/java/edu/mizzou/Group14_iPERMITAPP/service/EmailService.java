package edu.mizzou.Group14_iPERMITAPP.service;

// Domain model for permit request data used in email content
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;

// Spring mail support for sending emails
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

// Spring service annotation and dependency injection
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Marks this class as a service component responsible for email notifications
@Service
public class EmailService {

    // Spring mail sender used to send emails (currently not actively used in mocked method)
    @Autowired
    private JavaMailSender mailSender;

    // Sends email notifying RE user that permit status has been updated
    public void sendUpdateEmail(PermitRequest permit, String newStatus) {

        // Recipient email (RE user)
        String to = permit.getRe().getEmail();

        // Email subject line
        String subject = "Your permit status has been changed";

        // Email body message with permit details and new status
        String body = "Hello " + permit.getRe().getContactPersonName() +
                ",\n\nThis email is to inform you that the status on one of your permits has been changed. " +
                "the status of permit " + permit.getRequestNo() + " is now " + newStatus +
                ".\n\n-Environmental Ministry";

        // Delegate actual sending to helper method
        sendSimpleEmail(to, subject, body);
    }

    // Sends confirmation email after permit payment is successfully submitted
    public void sendConfirmationEmail(PermitRequest permit) {

        // Recipient email (RE user)
        String to = permit.getRe().getEmail();

        // Email subject
        String subject = "Confirmation of permit submission";

        // Email body confirming payment and next steps
        String body = "Hello " + permit.getRe().getContactPersonName() +
                ",\n\nThis email is to confirm your payment of " + permit.getPermitFee() +
                " for your " + permit.getEnvironmentalPermit().getPermitName() +
                ". you will receive an email once your permit is under review.\n\n-Environmental Ministry";

        // Send constructed email
        sendSimpleEmail(to, subject, body);
    }

    // Sends decision email after EO approves or rejects a permit request
    public void sendDecisionEmail(PermitRequest permit, String decision, String description) {

        // Handle empty rejection/approval reason
        if (description.equals("")) {
            description = "no reason given";
        }

        // Recipient email (RE user)
        String to = permit.getRe().getEmail();

        // Email subject reflecting decision outcome
        String subject = "Your permit has been " + decision;

        // Email body containing decision details and reason
        String body = "Hello " + permit.getRe().getContactPersonName() +
                ",\n\nThis email is to inform you that a decision has been made on one of your permits. " +
                "permit " + permit.getRequestNo() + " has been " + decision +
                ". the reason for this decision is as follows:\n\n" + description +
                ".\n\n-Environmental Ministry";

        // Send constructed email
        sendSimpleEmail(to, subject, body);
    }

    // Core email sending method (currently mocked for development/testing)
    public void sendSimpleEmail(String to, String subject, String text) {

        // Real email sending logic is commented out (disabled for testing)
        /*
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        */

        // Console output used as placeholder instead of real email delivery
        System.out.println("EMAIL WOULD BE SENT TO: " + to +
                " | WITH SUBJECT: " + subject +
                " | WITH CONTENT: " + text);
    }
}