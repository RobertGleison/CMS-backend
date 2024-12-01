package com.backend.Netflix.configs;

import org.springframework.beans.factory.annotation.Value;  // Changed import
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;  // Changed import
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GCPConfig {

    @Value("${gcp.credentials.location}")  // Removed default value here
    private String credentialsLocation;

    @Bean
    public CredentialsProvider googleCredentials() throws IOException {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    new FileInputStream(credentialsLocation)
            );
            return () -> credentials;
        } catch (IOException e) {
            throw new IOException("Failed to load GCP credentials from " + credentialsLocation, e);
        }
    }

    @Bean
    public Storage googleCloudStorage(CredentialsProvider credentialsProvider) throws IOException {
        StorageOptions options = StorageOptions.newBuilder()
                .setCredentials(credentialsProvider.getCredentials())
                .setProjectId("${cloudProjectId}")  // Add project ID
                .build();
        return options.getService();  // Removed casting
    }
}