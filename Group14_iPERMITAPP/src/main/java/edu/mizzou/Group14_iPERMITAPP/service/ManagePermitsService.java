package edu.mizzou.Group14_iPERMITAPP.service;

// Domain models used for permit creation and tracking workflow
import edu.mizzou.Group14_iPERMITAPP.model.EnvironmentalPermit;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.model.RE;
import edu.mizzou.Group14_iPERMITAPP.model.RequestStatus;

// Repository layer for database access
import edu.mizzou.Group14_iPERMITAPP.repository.EnvironmentalPermitRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.PermitRequestRepository;
import edu.mizzou.Group14_iPERMITAPP.repository.RequestStatusRepository;

// Controller reference (not typically recommended in service layer, but included in current design)
import edu.mizzou.Group14_iPERMITAPP.controller.PermitRequestForm;

// Spring service and dependency injection
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Java utilities
import java.util.Date;
import java.util.UUID;

// Marks this class as a service responsible for managing permit creation workflow
@Service
public class ManagePermitsService { // probably done at this point

    // Repository for environmental permit definitions
    @Autowired
    private EnvironmentalPermitRepository epRepository;

    // Repository for permit requests submitted by users
    @Autowired
    private PermitRequestRepository prRepository;

    // Repository for request status tracking
    @Autowired
    private RequestStatusRepository rsRepository;

    // Reference to form controller (instantiated manually, unusual in Spring design)
    private PermitRequestForm form;

    // Service used to trigger EO acknowledgment logic after request creation
    private AcknowledgeEOService acknowledgeEO;

    // Constructor-based injection for acknowledgment service
    public ManagePermitsService(AcknowledgeEOService ackEO) {
        this.acknowledgeEO = ackEO;

        // Manual instantiation of controller (not typical Spring practice)
        this.form = new PermitRequestForm();
    }

    // Retrieves permit fee for a given environmental permit ID
    public Double getPermitFee(String permitId) {

        // Fetch permit from database
        EnvironmentalPermit permit = epRepository.findById(permitId)
                .orElse(null);

        // Return fee if permit exists, otherwise return null
        if (permit != null) {
            return permit.getPermitFee();
        }

        return null;
    }

    // Creates and initializes a new permit application
    public void setApplication(EnvironmentalPermit ePermit,
                               RE re,
                               String activityDesc,
                               Date activityStart,
                               Date activityDuration) {

        // Create new permit request object
        PermitRequest request = new PermitRequest();

        // Current system date used for request timestamp
        java.sql.Date currentDate =
                new java.sql.Date(System.currentTimeMillis());

        // Assign system-generated unique request number
        request.setRequestNo(UUID.randomUUID().toString());

        // Set request creation date
        request.setDateOfRequest(currentDate);

        // Set activity details provided by user
        request.setActivityDescription(activityDesc);
        request.setActivityStartDate(activityStart);
        request.setActivityDuration(activityDuration);

        // Assign permit fee based on selected environmental permit
        request.setPermitFee(getPermitFee(ePermit.getPermitID()));

        // Link request to RE user
        request.setRe(re);

        // Link request to selected environmental permit
        request.setEnvironmentalPermit(ePermit);

        // Save permit request to database
        prRepository.save(request);

        // Create initial status entry for the request
        RequestStatus status = new RequestStatus();

        // Set default workflow status
        status.setPermitRequestStatus("Pending Payment");

        // Link status to permit request
        status.setPermitRequest(request);

        // Set timestamp for status creation
        status.setDate(currentDate);

        // Save status entry
        rsRepository.save(status);

        // Trigger EO-side processing (currently calls acceptPayment, may be design placeholder)
        acknowledgeEO.acceptPayment(request);
    }
}