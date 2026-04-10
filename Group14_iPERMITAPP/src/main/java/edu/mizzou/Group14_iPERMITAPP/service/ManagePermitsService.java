package edu.mizzou.Group14_iPERMITAPP.service;
import edu.mizzou.Group14_iPERMITAPP.model.EnvironmentalPermit;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.model.RE;
import edu.mizzou.Group14_iPERMITAPP.model.RequestStatus;
import edu.mizzou.Group14_iPERMITAPP.repository.EnvironmentalPermitRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;
import edu.mizzou.Group14_iPERMITAPP.controller.PermitRequestForm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;

@Service
public class ManagePermitsService {

    @Autowired
    private EnvironmentalPermitRepository epRepository;
    @Autowired
    private PermitRequestRepository prRepository;

    private PermitRequestForm form;

    private AcknowledgeEOService acknowledgeEO;

    public ManagePermitsService(AcknowledgeEOService ackEO){
        this.acknowledgeEO = ackEO;
        this.form = new PermitRequestForm();
    }

    public Double getPermitFee(String permitId){//takes permitId and finds permitfee for that EP
        //finds EP if it exists
        EnvironmentalPermit permit = epRepository.findById(permitId)
                                                .orElse(null);

        //gets permit fee if it exists
        if (permit != null){
            return permit.getPermitFee();
        }
        return null;
    }

    public void setApplication(EnvironmentalPermit ePermit, RE re, String activityDesc, Date activityStart, Date activityDuration){
        PermitRequest request = new PermitRequest();
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());

        request.setRequestNo(UUID.randomUUID().toString());
        request.setDateOfRequest(currentDate);
        request.setActivityDescription(activityDesc);
        request.setActivityStartDate(activityStart);
        request.setActivityDuration(activityDuration);
        request.setPermitFee(getPermitFee(ePermit.getPermitID()));
        request.setRe(re);
        request.setEnvironmentalPermit(ePermit);

        prRepository.save(request);

        RequestStatus status = new RequestStatus();

        status.setPermitRequestStatus("Pending Payment");
        status.setPermitRequest(request);
        status.setDate(currentDate);

        form.showAcknowledgement("Pending Payment");

        acknowledgeEO.acceptPayment(request);
    }
}
