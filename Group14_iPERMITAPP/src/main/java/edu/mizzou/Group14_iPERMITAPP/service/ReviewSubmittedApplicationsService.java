package edu.mizzou.Group14_iPERMITAPP.service;

// Controller imports (generally not recommended in service layer, but used here)
import edu.mizzou.Group14_iPERMITAPP.controller.IssuePermitForm;
import edu.mizzou.Group14_iPERMITAPP.controller.ReviewSubmittedApplicationsForm;

// Domain models used for permit review workflow
import edu.mizzou.Group14_iPERMITAPP.model.*;

// Repository layer for persistence of decisions, status updates, and email logs
import edu.mizzou.Group14_iPERMITAPP.repository.DecisionRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.EmailArchiveRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.RequestStatusRepository;

// Spring service annotation and dependency injection
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Marks this class as the service handling EO review workflow (approve/reject permits)
@Service
public class ReviewSubmittedApplicationsService {

    // Repository for storing archived emails
    @Autowired
    private EmailArchiveRepository eaRepository;

    // Repository for tracking request status changes
    @Autowired
    private RequestStatusRepository rsRepository;

    // Repository for storing final EO decisions
    @Autowired
    private DecisionRepository dRepository;

    // Email service for sending notifications
    @Autowired
    private EmailService emailService;

    // Manually instantiated email service (redundant alongside @Autowired version)
    private EmailService email;

    // Direct reference to controller (not typical in Spring architecture)
    private ReviewSubmittedApplicationsForm form;

    // Constructor initializes helper objects manually
    public ReviewSubmittedApplicationsService() {
        this.email = new EmailService();
        this.form = new ReviewSubmittedApplicationsForm();
    }

    // Marks a permit request as "Being Reviewed" when EO opens it
    public void setCurrentRequest(PermitRequest permit) {

        // Create timestamp for status update
        java.sql.Date currentDate =
                new java.sql.Date(System.currentTimeMillis());

        // Create new status entry
        RequestStatus status = new RequestStatus();

        // Set review status
        status.setPermitRequestStatus("Being Reviewed");

        // Link status to permit request
        status.setPermitRequest(permit);

        // Set status update date
        status.setDate(currentDate);

        // Send email notification to RE user
        emailService.sendUpdateEmail(permit, "Being Reviewed");

        // Save status to database
        rsRepository.save(status);
    }

    // Approves a permit request and records decision
    public void approveRequest(PermitRequest permit, EO eo, String description) {

        // Create decision record
        Decision decision = new Decision();

        // Timestamp for decision
        java.sql.Date currentDate =
                new java.sql.Date(System.currentTimeMillis());

        // Create status update object
        RequestStatus status = new RequestStatus();

        // Set decision fields
        decision.setId(permit.getRequestNo());
        decision.setDateOfDecision(currentDate);
        decision.setFinalDecision("Approve");
        decision.setDescription(description);
        decision.setEo(eo);
        decision.setPermitRequest(permit);

        // Set request status as approved
        status.setPermitRequestStatus("Approved");
        status.setPermitRequest(permit);
        status.setDate(currentDate);

        // Save status update
        rsRepository.save(status);

        // Send approval email to applicant
        emailService.sendDecisionEmail(permit, "Approved", description);
    }

    // Rejects a permit request and records decision
    public void rejectRequest(PermitRequest permit, EO eo, String description) {

        // Create decision record
        Decision decision = new Decision();

        // Timestamp for decision
        java.sql.Date currentDate =
                new java.sql.Date(System.currentTimeMillis());

        // Create status update object
        RequestStatus status = new RequestStatus();

        // Set decision fields
        decision.setId(permit.getRequestNo());
        decision.setDateOfDecision(currentDate);
        decision.setFinalDecision("Reject");
        decision.setDescription(description);
        decision.setEo(eo);
        decision.setPermitRequest(permit);

        // Set request status as rejected
        status.setPermitRequestStatus("Rejected");
        status.setPermitRequest(permit);
        status.setDate(currentDate);

        // Save status update
        rsRepository.save(status);

        // Send rejection email to applicant
        emailService.sendDecisionEmail(permit, "Rejected", description);
    }

    // Sends a simple email via internal email service
    public void sendEmail(String recipient, String emailBody, String subjectLine) {
        email.sendSimpleEmail(recipient, emailBody, subjectLine);
    }

    // Stores email in archive database table
    public void ArchiveEmail(String recipient, String emailBody, String subjectLine) {

        // Create archive record
        EmailArchive archive = new EmailArchive();

        // Set email metadata
        archive.setRecipient(recipient);
        archive.setSubjectLine(subjectLine);
        archive.setEmailBody(emailBody);

        // Save archived email
        eaRepository.save(archive);
    }
}