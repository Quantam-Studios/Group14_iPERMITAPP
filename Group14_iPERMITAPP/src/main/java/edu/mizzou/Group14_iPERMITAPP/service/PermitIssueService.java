package edu.mizzou.Group14_iPERMITAPP.service;

import edu.mizzou.Group14_iPERMITAPP.controller.IssuePermitForm;
import edu.mizzou.Group14_iPERMITAPP.model.EO;
import edu.mizzou.Group14_iPERMITAPP.model.EmailArchive;
import edu.mizzou.Group14_iPERMITAPP.model.Permit;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;

import edu.mizzou.Group14_iPERMITAPP.repository.EmailArchiveRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PermitIssueService {

    @Autowired
    private PermitRepository pRepository;
    @Autowired
    private EmailArchiveRepository eaRepository;

    private IssuePermitForm form;
    private EmailService email;

    public PermitIssueService(){
        this.email = new EmailService();
        this.form = new IssuePermitForm();
    }

    public String printRequest(PermitRequest request, EO eo){
        Permit permit = new Permit();
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());

        permit.setPermitID(UUID.randomUUID().toString());
        permit.setDateOfIssue(currentDate);
        permit.setDuration(request.getActivityDuration().toString());
        permit.setDescription(request.getActivityDescription());
        permit.setPermitRequest(request);
        permit.setEo(eo);

        pRepository.save(permit);

        return permit.toString();
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
