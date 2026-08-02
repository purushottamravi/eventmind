package com.ravi.eventmind.query.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * A row in the read-model database that comes to life for a moment when we load it.
 * Holds whatever the symptom events tell us about a symptom.
 */
@Data
@Entity
public class Symptom {

    @Id
    private String id;
    private String name;
    private Long origin;
    private Integer numberOfOccurance;
}
