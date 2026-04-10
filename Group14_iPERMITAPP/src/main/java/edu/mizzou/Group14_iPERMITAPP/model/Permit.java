package edu.mizzou.Group14_iPERMITAPP.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Data
public class Permit {

    @Id
    private String permitID;

    private Date dateOfIssue;
    private String duration;
    private String description;

    @OneToOne
    @JoinColumn(name = "permit_request_id")
    private PermitRequest permitRequest;

    @ManyToOne
    @JoinColumn(name = "eo_id")
    private EO eo;

    public String toString(){
        return "Permit " + permitID + ":\nIssued on " + dateOfIssue + "\nValid until " + duration + "\n Description: " + description;
    }
}