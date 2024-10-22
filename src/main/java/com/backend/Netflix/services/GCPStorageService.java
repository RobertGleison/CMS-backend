package com.backend.Netflix.services;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.nio.channels.Channels;

@Service
public class GCPStorageService {

    @Autowired
    private Storage storage;

    @Value("${gcp.bucket.name}")
    private String bucketName;


    public String uploadFile(String objectName, InputStream inputStream, String contentType) {
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        try {
            Blob blob = storage.create(blobInfo, inputStream.readAllBytes());
            return blob.getMediaLink();
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to GCP", e);
        }
    }
}