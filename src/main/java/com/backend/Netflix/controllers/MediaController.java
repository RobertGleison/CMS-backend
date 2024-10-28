package com.backend.Netflix.controllers;

import com.backend.Netflix.model.VideoMetadata;
import com.backend.Netflix.services.VideoProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media")
public class MediaController {

    private static final Logger logger = LoggerFactory.getLogger(MediaController.class);

    @Autowired
    private VideoProcessingService videoService;



    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> processVideo(
            @RequestPart(value = "videoFile", required = true) MultipartFile videoFile,
            @RequestPart(value = "thumbnail", required = true) MultipartFile thumbnail,
            @RequestPart(value = "title") String movieTitle,
            @RequestPart(value = "description") String movieDescription,
            @RequestPart(value = "genre") String movieGenre,
            @RequestPart(value = "publisher") String movieCreator,
            @RequestPart(value = "duration") String movieDuration
    ) {

        try {
            if (!videoFile.getContentType().startsWith("video/")) {
                return ResponseEntity.badRequest()
                        .body("Invalid video file format. Only video files are allowed.");
            }

            if (!thumbnail.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body("Invalid thumbnail format. Only image files are allowed.");
            }

            // Validate file sizes
            long maxVideoSize = 1024 * 1024 * 500; // 500MB
            long maxThumbnailSize = 1024 * 1024 * 10; // 10MB

            if (videoFile.getSize() > maxVideoSize) {
                return ResponseEntity.badRequest()
                        .body("Video file size exceeds maximum limit of 500MB");
            }

            if (thumbnail.getSize() > maxThumbnailSize) {
                return ResponseEntity.badRequest()
                        .body("Thumbnail size exceeds maximum limit of 5MB");
            }
            VideoMetadata metadata = new VideoMetadata(movieTitle, movieDescription, movieGenre, movieCreator, movieDuration);
            // Process the upload using service layer
//            VideoMetadata savedVideo = videoService.processVideoUpload(videoFile, thumbnail, metadata);
            System.out.println(metadata);

//            logger.info("Successfully processed video upload: {}", metadata.getMovieTitle());
            return ResponseEntity.ok().body(metadata);

        } catch (Exception e) {
            logger.error("Error processing video upload: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Error processing upload: " + e.getMessage());
        }
    }
}