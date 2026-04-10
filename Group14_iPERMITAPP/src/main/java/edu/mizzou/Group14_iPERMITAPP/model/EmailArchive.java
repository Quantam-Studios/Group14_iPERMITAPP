package edu.mizzou.Group14_iPERMITAPP.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EmailArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipient;
    private String subjectLine;
    private String emailBody;


}
