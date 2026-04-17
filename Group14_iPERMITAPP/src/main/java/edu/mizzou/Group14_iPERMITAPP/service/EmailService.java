package edu.mizzou.Group14_iPERMITAPP.service;



import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.model.RE;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.RERepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;


    public void sendUpdateEmail(PermitRequest permit, String newStatus) {
        String to = permit.getRe().getEmail();
        String subject = "Your permit status has been changed";
        String body = "Hello " + permit.getRe().getContactPersonName() + ",\n\nThis email is to inform you that the status on one of your permits has been changed. " +
                "the status of permit " + permit.getRequestNo() + " is now " + newStatus + ".\n\n-Environmental Ministry";

        sendSimpleEmail(to, subject, body);
    }

    public void sendConfirmationEmail(PermitRequest permit) {
        String to = permit.getRe().getEmail();
        String subject = "Confirmation of permit submission";
        String body = "Hello " + permit.getRe().getContactPersonName() + ",\n\nThis email is to confirm your payment of " + permit.getPermitFee() + " for your " + permit.getEnvironmentalPermit().getPermitName() +
                ". you will receive an email once your permit is under review.\n\n-Environmental Ministry";

        sendSimpleEmail(to, subject, body);
    }

    public void sendDecisionEmail(PermitRequest permit, String decision, String description) {
        if (description.equals("")){
            description = "no reason given";
        }
        String to = permit.getRe().getEmail();
        String subject = "Your permit has been " + decision;
        String body = "Hello " + permit.getRe().getContactPersonName() + ",\n\nThis email is to inform you that a decision has been made on one of your permits. " +
                "permit " + permit.getRequestNo() + " has been " + decision + ". the reason for this decision is as follows:\n\n" + description + ".\n\n-Environmental Ministry";

        sendSimpleEmail(to, subject, body);
    }

    public void sendSimpleEmail(String to, String subject, String text) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(text);
//        mailSender.send(message);
    	
    	System.out.println("EMAIL WOULD BE SENT TO: " + to + " | WITH SUBJECT: " + subject + " | WITH CONTENT: " + text);
    }
}
