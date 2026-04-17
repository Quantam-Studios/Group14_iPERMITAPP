package edu.mizzou.Group14_iPERMITAPP.service;

// Domain models for permit requests and status tracking
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import edu.mizzou.Group14_iPERMITAPP.model.RequestStatus;

// Repository for persisting and retrieving request status data
import edu.mizzou.Group14_iPERMITAPP.repository.RequestStatusRepository;

// Spring service annotation and dependency injection
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Java utilities for collections
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Marks this class as a service layer component in Spring
@Service
public class AcknowledgeEOService { // still needs work

    // Repository for accessing request status records in database
    @Autowired
    private RequestStatusRepository rsRepository;

    // Service used to send email notifications to users
    @Autowired
    private EmailService emailService;

    // Builds a map of latest status per permit request
    public Map<String, String> getLatestStatusMap() {

        // Retrieve most recent status entries for all requests
        List<RequestStatus> latest = rsRepository.findLatestStatuses();

        // Map to store requestNo -> status string
        Map<String, String> map = new HashMap<>();

        // Populate map with latest status per request
        for (RequestStatus rs : latest) {
            map.put(rs.getPermitRequest().getRequestNo(),
                    rs.getPermitRequestStatus());
        }

        return map;
    }

    // Handles logic when a payment is accepted for a permit request
    public void acceptPayment(PermitRequest permit) {

        // NOTE: Placeholder comment indicating future integration with boundary classes

        // Create current system date for status record
        java.sql.Date currentDate =
                new java.sql.Date(System.currentTimeMillis());

        // Create new status record
        RequestStatus status = new RequestStatus();

        // Set status to indicate successful payment processing
        status.setPermitRequestStatus("Payment Accepted");

        // Link status to permit request
        status.setPermitRequest(permit);

        // Set timestamp of status update
        status.setDate(currentDate);

        // Save status update to database
        rsRepository.save(status);

        // Send confirmation email after successful payment
        emailService.sendConfirmationEmail(permit);
    }

    // Retrieves all permit requests that have been marked as paid/valid
    public List<PermitRequest> getValidPermitRequests() {
        return rsRepository.findPaidPermitRequests();
    }
}