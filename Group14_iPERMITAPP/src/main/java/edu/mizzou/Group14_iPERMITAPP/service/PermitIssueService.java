package edu.mizzou.Group14_iPERMITAPP.service;

// Controller reference (generally not recommended in service layer, but used here)
import edu.mizzou.Group14_iPERMITAPP.controller.IssuePermitForm;

// Domain models used for permit issuance and email archiving
import edu.mizzou.Group14_iPERMITAPP.model.EO;
import edu.mizzou.Group14_iPERMITAPP.model.EmailArchive;
import edu.mizzou.Group14_iPERMITAPP.model.Permit;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;

// Repository layer for database persistence
import edu.mizzou.Group14_iPERMITAPP.repository.EmailArchiveRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRepository;

// Spring service and dependency injection
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Utility for generating unique IDs
import java.util.UUID;

// Marks this class as a service responsible for issuing permits and managing related email workflows
@Service
public class PermitIssueService {

    // Repository for saving issued permits
    @Autowired
    private PermitRepository pRepository;

    // Repository for storing archived emails
    @Autowired
    private EmailArchiveRepository eaRepository;

    // Reference to form controller (manually instantiated, not typical Spring practice)
    private IssuePermitForm form;

    // Email service used to send notifications
    private EmailService email;

    // Constructor initializes email service and form manually
    public PermitIssueService() {
        this.email = new EmailService();
        this.form = new IssuePermitForm();
    }

    // Generates and saves a permit based on an approved request
    public String printRequest(PermitRequest request, EO eo) {

        // Create new Permit entity
        Permit permit = new Permit();

        // Current system date used as issue date
        java.sql.Date currentDate =
                new java.sql.Date(System.currentTimeMillis());

        // Assign unique permit ID
        permit.setPermitID(UUID.randomUUID().toString());

        // Set issuance date
        permit.setDateOfIssue(currentDate);

        // Set permit duration based on request activity duration
        permit.setDuration(request.getActivityDuration().toString());

        // Copy description from request
        permit.setDescription(request.getActivityDescription());

        // Link permit to original request
        permit.setPermitRequest(request);

        // Link permit to issuing EO
        permit.setEo(eo);

        // Save permit to database
        pRepository.save(permit);

        // Return string representation of created permit (likely for debugging/logging)
        return permit.toString();
    }

    // Sends an email using internal EmailService
    public void sendEmail(String recipient, String emailBody, String subjectLine) {

        // Delegates email sending to EmailService
        email.sendSimpleEmail(recipient, emailBody, subjectLine);
    }

    // Stores a copy of sent email in the archive database table
    public void ArchiveEmail(String recipient, String emailBody, String subjectLine) {

        // Create new email archive record
        EmailArchive archive = new EmailArchive();

        // Set recipient email address
        archive.setRecipient(recipient);

        // Set subject line of email
        archive.setSubjectLine(subjectLine);

        // Set email body content
        archive.setEmailBody(emailBody);

        // Save archived email to database
        eaRepository.save(archive);
    }
}