package com.jaasielsilva.labsystem;

import com.jaasielsilva.labsystem.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class LabsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(LabsystemApplication.class, args);
	}

}
