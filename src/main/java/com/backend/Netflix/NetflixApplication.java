package com.backend.Netflix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class NetflixApplication {

	//TODO: tirar o application.properties hardcoded, like "gcp.credentials.path=${GCP_CREDENTIALS_PATH}"
	public static void main(String[] args) {
		SpringApplication.run(NetflixApplication.class, args);
	}

	@Bean  // This annotation is crucial - it tells Spring to create a RestTemplate bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
