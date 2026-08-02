package com.ravi.eventmind.observability.logging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationLogRepository  extends JpaRepository<ApplicationLog, Long> {

}