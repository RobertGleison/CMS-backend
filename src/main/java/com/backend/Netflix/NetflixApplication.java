package com.backend.Netflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NetflixApplication {
	public static void main(String[] args) {
		// Set the credentials before running the application
		System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "/home/robert/Desktop/mobile/content-management-system-server/src/main/resources/netflixplus-438015-6b10ae45a987.json");
		SpringApplication.run(NetflixApplication.class, args);
	}
}