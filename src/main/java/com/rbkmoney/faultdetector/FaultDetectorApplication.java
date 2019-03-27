package com.rbkmoney.faultdetector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ServletComponentScan
@SpringBootApplication
//@Profile("prod")
public class FaultDetectorApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(FaultDetectorApplication.class);
    }

}
