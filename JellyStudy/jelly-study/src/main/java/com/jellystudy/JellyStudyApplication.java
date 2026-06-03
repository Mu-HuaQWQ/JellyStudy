package com.jellystudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JellyStudyApplication {
    public static void main(String[] args) {
        SpringApplication.run(JellyStudyApplication.class, args);
    }
}
