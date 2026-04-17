package edu.mizzou.Group14_iPERMITAPP.service;

import edu.mizzou.Group14_iPERMITAPP.controller.IssuePermitForm;
import edu.mizzou.Group14_iPERMITAPP.controller.ReviewSubmittedApplicationsForm;
import edu.mizzou.Group14_iPERMITAPP.model.*;
import edu.mizzou.Group14_iPERMITAPP.repository.DecisionRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.EmailArchiveRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.RequestStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewSubmittedApplicationsService {

    @Autowired
    private EmailArchiveRepository eaRepository;
    @Autowired
    private RequestStatusRepository rsRepository;
    @Autowired
    private DecisionRepository dRepository;
    @Autowired
    private EmailService emailService;

    private EmailService email;
    private ReviewSubmittedApplicationsForm form;

    public ReviewSubmittedApplicationsService() {
        this.email = new EmailService();
        this.form = new ReviewSubmittedApplicationsForm();
    }

    public void setCurrentRequest(PermitRequest permit) {
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        RequestStatus status = new RequestStatus();

        status.setPermitRequestStatus("Being Reviewed");
        status.setPermitRequest(permit);
        status.setDate(currentDate);
        emailService.sendUpdateEmail(permit, "Being Reviewed");

        rsRepository.save(status);
    }

    public void approveRequest(PermitRequest permit, EO eo, String description) {
        Decision  decision = new Decision();
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        RequestStatus status = new RequestStatus();

        decision.setId(permit.getRequestNo());
        decision.setDateOfDecision(currentDate);
        decision.setFinalDecision("Approve");
        decision.setDescription(description);
        decision.setEo(eo);
        decision.setPermitRequest(permit);

        status.setPermitRequestStatus("Approved");
        status.setPermitRequest(permit);
        status.setDate(currentDate);

        rsRepository.save(status);

        emailService.sendUpdateEmail(permit, "Approved");
    }

    public void rejectRequest(PermitRequest permit, EO eo, String description){
        Decision  decision = new Decision();
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        RequestStatus status = new RequestStatus();

        decision.setId(permit.getRequestNo());
        decision.setDateOfDecision(currentDate);
        decision.setFinalDecision("Reject");
        decision.setDescription(description);
        decision.setEo(eo);
        decision.setPermitRequest(permit);

        status.setPermitRequestStatus("Rejected");
        status.setPermitRequest(permit);
        status.setDate(currentDate);


        rsRepository.save(status);

        emailService.sendUpdateEmail(permit, "Rejected");
    }

    public void sendEmail(String recipient, String emailBody, String subjectLine){
        email.sendSimpleEmail(recipient, emailBody, subjectLine);
    }

    public void ArchiveEmail(String recipient, String emailBody, String subjectLine){
        EmailArchive archive = new EmailArchive();

        archive.setRecipient(recipient);
        archive.setSubjectLine(subjectLine);
        archive.setEmailBody(emailBody);

        eaRepository.save(archive);
    }
}
