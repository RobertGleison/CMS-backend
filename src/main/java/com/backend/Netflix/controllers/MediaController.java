package com.backend.Netflix.controllers;


import com.backend.Netflix.model.Media;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {

    @PostMapping(value = "/upload",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> processVideo(
            @RequestParam("videoPart") MultipartFile videoFile,
            @RequestParam("thumbnailPart") MultipartFile thumbnail,
            @RequestParam("titleBody") String title,
            @RequestParam("descriptionBody") String description,
            @RequestParam("genreBody") String genre,
            @RequestParam("yearBody") Integer year,
            @RequestParam("publisherBody") String publisher,
            @RequestParam("durationBody") Integer duration
    ) {
        try {
            // Log incoming request details
            System.out.println("Received file upload request");
            System.out.println("Video file name: " + videoFile.getOriginalFilename());
            System.out.println("Video content type: " + videoFile.getContentType());

            Media media = new Media(
                    UUID.randomUUID(),
                    title,
                    description,
                    genre,
                    year,
                    publisher,
                    duration,
                    "popeye_e_os_40_abacates.jpg",
                    "s3://bucket/popeye_e_os_40_abacates.mp4",
                    LocalDateTime.of(2001, 3, 23, 0, 0
                    ));

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(media);

        } catch (Exception e) {
            System.out.println("Error processing upload: " + e);
            return ResponseEntity.badRequest()
                    .body("Error processing upload: " + e.getMessage());
        }
    }

    @GetMapping(value = "/teste")
    public ResponseEntity<String> teste(){
        System.out.println("Teste");
        return ResponseEntity.ok().body("Teste");
    }

}
