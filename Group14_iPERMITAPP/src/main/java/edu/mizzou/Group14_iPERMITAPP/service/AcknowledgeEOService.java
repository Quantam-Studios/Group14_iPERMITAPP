package edu.mizzou.Group14_iPERMITAPP.service;

import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.model.RequestStatus;
import edu.mizzou.Group14_iPERMITAPP.repository.RequestStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AcknowledgeEOService { //still needs work

    @Autowired
    private RequestStatusRepository rsRepository;

    public void acceptPayment(PermitRequest permit){
        // this should do something before updating the status, however that is going to interact with one of the
        // boundary classes, and I'm not confident enough to touch them yet

        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());
        RequestStatus status = new RequestStatus();

        status.setPermitRequestStatus("Payment Accepted");
        status.setPermitRequest(permit);
        status.setDate(currentDate);

        rsRepository.save(status);
    }

    public List<PermitRequest> getValidPermitRequests() {
        return rsRepository.findPaidPermitRequests();
    }
}
