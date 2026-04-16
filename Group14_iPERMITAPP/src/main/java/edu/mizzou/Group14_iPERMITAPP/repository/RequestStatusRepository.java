package edu.mizzou.Group14_iPERMITAPP.repository;

import edu.mizzou.Group14_iPERMITAPP.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import edu.mizzou.Group14_iPERMITAPP.model.PermitRequest;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface RequestStatusRepository extends JpaRepository<RequestStatus, Long> {

    @Query("""
    SELECT r.permitRequest
    FROM RequestStatus r
    WHERE r.permitRequestStatus = 'Payment Accepted'
    """)
    List<PermitRequest> findPaidPermitRequests();
}
