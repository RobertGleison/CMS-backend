package com.backend.Netflix.services;

import com.backend.Netflix.model.VideoMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

@Service
public class VideoProcessingService {

    private final InternetArchiveService archiveService;
    private final GCPStorageService gcpService;

    public VideoProcessingService(InternetArchiveService archiveService, GCPStorageService gcpService) {
        this.archiveService = archiveService;
        this.gcpService = gcpService;
    }

    public VideoMetadata processVideo(String identifier) throws IOException {
        // Get metadata
        VideoMetadata metadata = archiveService.getMetadata(identifier);

        // Download and upload thumbnail
        URL thumbnailUrl = archiveService.getThumbnailUrl(identifier);
        String gcpThumbnailPath = String.format("thumbnails/%s_thumb.jpg", identifier);
        try (InputStream thumbnailStream = thumbnailUrl.openStream()) {
            String thumbnailGcpUrl = gcpService.uploadFile(
                    gcpThumbnailPath,
                    thumbnailStream,
                    "image/jpeg"
            );
            metadata.setThumbnailUrl(thumbnailGcpUrl);
        }

        // Download and upload video
        URL videoUrl = archiveService.getVideoDownloadUrl(identifier);
        String gcpVideoPath = String.format("videos/%s.mp4", identifier);
        URLConnection conn = videoUrl.openConnection();
        try (InputStream videoStream = conn.getInputStream()) {
            String videoGcpUrl = gcpService.uploadFile(
                    gcpVideoPath,
                    videoStream,
                    "video/mp4"
            );
            metadata.setVideoUrl(videoGcpUrl);
        }

        // Upload metadata
        String metadataJson = new ObjectMapper().writeValueAsString(metadata);
        String gcpMetadataPath = String.format("metadata/%s.json", identifier);
        try (InputStream metadataStream = new ByteArrayInputStream(metadataJson.getBytes())) {
            gcpService.uploadFile(
                    gcpMetadataPath,
                    metadataStream,
                    "application/json"
            );
        }

        return metadata;
    }
}