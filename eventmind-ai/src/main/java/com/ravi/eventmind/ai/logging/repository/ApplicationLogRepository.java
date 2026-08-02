package com.ravi.eventmind.ai.logging.repository;

import com.ravi.eventmind.ai.logging.entity.ApplicationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data handle on the log table. Mostly CRUD, maybe a query or two later.
 */
@Repository
public interface ApplicationLogRepository extends JpaRepository<ApplicationLogEntity, Long> {
}
