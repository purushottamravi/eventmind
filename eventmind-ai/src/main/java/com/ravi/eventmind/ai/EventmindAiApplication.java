package com.ravi.eventmind.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The doorbell of the whole module. Running this main class starts the
 * Spring Boot app and everything else follows.
 */
@SpringBootApplication
public class EventmindAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventmindAiApplication.class, args);
    }
}
