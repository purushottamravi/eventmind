package com.ravi.eventmind.ai.logging.repository;

import com.ravi.eventmind.ai.logging.entity.ApplicationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationLogRepository extends JpaRepository<ApplicationLogEntity, Long> {
}
