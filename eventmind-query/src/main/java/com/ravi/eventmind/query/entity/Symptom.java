package com.ravi.eventmind.query.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Symptom {

    @Id
    private String id;
    private String name;
    private Long origin;
    private Integer numberOfOccurance;
}
