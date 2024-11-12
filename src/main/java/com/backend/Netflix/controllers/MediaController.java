package com.backend.Netflix.controllers;

import com.backend.Netflix.DTO.MediaRequestMultiform;
import com.backend.Netflix.DTO.MediaResponseDTO;
import com.backend.Netflix.model.Media;
import com.backend.Netflix.repository.MediaRepository;
import com.backend.Netflix.services.CassandraMediaService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import com.backend.Netflix.services.GcpMediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {

    private static final Logger logger = Logger.getLogger(MediaController.class.getName());
    @Autowired
    private CassandraMediaService cassandraService;
    @Autowired
    private GcpMediaUploadService gcpService;




    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> processVideo(@ModelAttribute MediaRequestMultiform mediaForm) {
        try {
            logger.info("Received file upload request");
            String filename = String.format("%s_%s", mediaForm.titleBody(), Objects.requireNonNull(mediaForm.videoPart().getContentType()).split("/")[1]);
//          Map<String, String> bucketPath = gcpService.uploadFile();

            Map<String, String> bucketPaths = new HashMap<>();
            bucketPaths.put("360p", "caminho_path_360p");
            bucketPaths.put("1080p", "caminho_path_1080p");

            LocalDateTime timestamp = LocalDateTime.now();

            Media media = new Media(
                    UUID.randomUUID(),
                    mediaForm.titleBody(),
                    mediaForm.descriptionBody(),
                    mediaForm.genreBody(),
                    mediaForm.yearBody(),
                    mediaForm.publisherBody(),
                    mediaForm.durationBody(),
                    filename,
                    bucketPaths,
                    timestamp
            );

            MediaResponseDTO response = cassandraService.insertMedia(media);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Error processing upload: " + e);
            return ResponseEntity.badRequest().body("Error processing upload: " + e.getMessage());
        }
    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<UserResponseDtoV1> getUserById(@PathVariable Integer id) {
//        return ResponseEntity.ok().body(service.findById(id));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<UserResponseDtoV1>> getAllUsers() {
//        logger.info("I'm in the getAllUsers");
//        return ResponseEntity.ok().body(service.findAll());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id) {
//        service.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<UserResponseDtoV1> updateUser(@RequestBody UserRequestDtoV1 userRequestDtoV1, @PathVariable Integer id) {
//        UserResponseDtoV1 userResponseDtoV1 = service.update(userRequestDtoV1, id);
//        return ResponseEntity.ok(userResponseDtoV1);
//    }
//
//
//    @PostMapping
//    public ResponseEntity<UserResponseDtoV1> insertUser(@RequestBody UserRequestDtoV1 userDto) {
//        UserResponseDtoV1 userDtoResponse = service.insert(userDto);
//        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
//                .path("{/id}")
//                .buildAndExpand(userDtoResponse.id()).toUri();
//        return ResponseEntity.created(uri).body(userDtoResponse);
//    }
}
