package com.log_monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogMonitoringApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogMonitoringApplication.class, args);
	}

}
