package com.ravi.eventmind.observability.logging.repository;

import com.ravi.eventmind.observability.logging.ApplicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data takes care of the boring SQL for us. We just declare the method names and get a working repository.
 */
@Repository
public interface ApplicationLogRepository  extends JpaRepository<ApplicationLog, Long> {

}