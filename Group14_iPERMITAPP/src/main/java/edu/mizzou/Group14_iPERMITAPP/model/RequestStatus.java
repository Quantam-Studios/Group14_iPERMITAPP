package edu.mizzou.Group14_iPERMITAPP.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
public class RequestStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String permitRequestStatus;
    private Date date;
    private String description;

    @ManyToOne
    @JoinColumn(name = "permit_request_id")
    private PermitRequest permitRequest;
}