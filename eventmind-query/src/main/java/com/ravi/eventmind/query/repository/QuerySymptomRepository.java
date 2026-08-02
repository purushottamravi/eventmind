package com.ravi.eventmind.query.repository;


import com.ravi.eventmind.query.entity.Symptom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuerySymptomRepository extends JpaRepository<Symptom, String> {

    Page<Symptom> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
