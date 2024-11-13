package com.backend.Netflix.services;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service
public class GcpMediaUploadService {

    // The ID of your GCP project
    @Value("${cloudProjectId}")
    String projectId;

    // The ID of your GCS bucket
    @Value("${cloudBucketName}")
    String bucketName;

    public Map<String, String> upload(String title, MultipartFile videoFile, MultipartFile thumbnail) throws IOException {
        Map<String, String> bucketPaths = new HashMap<>();
        bucketPaths.put("360p", uploadVideoLowQuality(title, videoFile));
        bucketPaths.put("1080p", uploadVideoHighQuality(title, videoFile));
        bucketPaths.put("thumbnail", uploadImage(title, thumbnail));
        return bucketPaths;

    }

    public String uploadVideoHighQuality(String fileName, MultipartFile videoFile) throws IOException {
        String bucketFileName = String.format("%s/%s_%s.%s", fileName, "1080p", fileName, Objects.requireNonNull(videoFile.getContentType()).split("/")[1]);
        String bucketPath = streamObjectUpload(bucketFileName, videoFile);
        return bucketPath;
    }


    public String uploadVideoLowQuality(String fileName, MultipartFile videoFile) {
        String bucketFileName = String.format("%s/%s_%s.%s", fileName, "360p", fileName, Objects.requireNonNull(videoFile.getContentType()).split("/")[1]);
        // converter
        // upload e retornar link
        return "s3://bucket/popeye_e_os_40_abacates360.mp4";
    }

    public String uploadImage(String fileName, MultipartFile thumbnail) throws IOException {
        String bucketFileName = String.format("%s/%s_%s.%s", fileName, "thumbnail", fileName, Objects.requireNonNull(thumbnail.getContentType()).split("/")[1]);
        String bucketPath = streamObjectUpload(bucketFileName, thumbnail);
        return bucketPath;
    }

    public String streamObjectUpload(String objectName, MultipartFile file) throws IOException {
        Storage storage = StorageOptions.newBuilder().setProjectId(this.projectId).build().getService();
        BlobId blobId = BlobId.of(this.bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        try (WriteChannel writer = storage.writer(blobInfo);
             InputStream inputStream = file.getInputStream()) {

            // Use 1MB buffer
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                writer.write(byteBuffer);
            }

            System.out.println(
                    "Wrote " + objectName + " to bucket " + bucketName + " using a WriteChannel.");

            return "https://storage.cloud.google.com/netflixplus-library-cc2024/" + objectName;
        }
    }
}

