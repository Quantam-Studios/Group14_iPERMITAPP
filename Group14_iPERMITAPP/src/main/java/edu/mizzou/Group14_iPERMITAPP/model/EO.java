package edu.mizzou.Group14_iPERMITAPP.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class EO {

    @Id
    private String id;

    private String name;
    private String password;
}