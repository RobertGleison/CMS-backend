package com.backend.Netflix.configs;

import org.springframework.beans.factory.annotation.Value;  // Changed import
import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;  // Changed import
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class GCPConfig {

    @Value("${gcp.credentials.location}")
    private String credentialsLocation;

    @Value("${cloudProjectId}")  // Add this to properly inject project ID
    private String projectId;

    @Bean
    public CredentialsProvider googleCredentials() throws IOException {
        try {
            // Use ClassPathResource to load from classpath
            Resource resource = new ClassPathResource(credentialsLocation);
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                    resource.getInputStream()
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
                .setProjectId(projectId)  // Use injected project ID
                .build();
        return options.getService();
    }
}