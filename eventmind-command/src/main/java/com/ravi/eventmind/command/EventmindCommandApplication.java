package com.ravi.eventmind.command;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The doorbell of the whole module. Running this main class starts the Spring Boot app and everything else follows.
 */
@SpringBootApplication
public class EventmindCommandApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventmindCommandApplication.class, args);
    }
}
