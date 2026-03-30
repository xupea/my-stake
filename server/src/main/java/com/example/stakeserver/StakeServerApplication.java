package com.example.stakeserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StakeServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StakeServerApplication.class, args);
    }

}
