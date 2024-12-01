package com.backend.Netflix.controllers;

import com.backend.Netflix.model.MediaRequestDTO;
import com.backend.Netflix.model.MediaResponseDTO;
import com.backend.Netflix.services.CassandraMediaService;
import com.backend.Netflix.services.GcpMediaDeleteService;
import com.backend.Netflix.services.GcpMediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for handling media-related operations in the Netflix backend.
 */
@RestController
@RequestMapping("/media")
public class MediaController {


    @Autowired
    private CassandraMediaService cassandraService;


    @Autowired
    private GcpMediaUploadService gcpService;


    @Autowired
    private GcpMediaDeleteService gcpDelete;


    /**
     * Processes and uploads new media content to backend.
     * Handles both media file upload to GCP and metadata storage in Cassandra.
     * @param mediaForm DTO containing media details including title, video file, and thumbnail
     * @return ResponseEntity containing the created media's details
     * @throws IOException if there's an error processing the media files
     * @throws InterruptedException if the upload process is interrupted
     */
    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<MediaResponseDTO> processMedia(@ModelAttribute @RequestBody MediaRequestDTO mediaForm) throws IOException, InterruptedException {
        Map<String, String> bucketPaths = new HashMap<>();
        bucketPaths.put("high_quality", "testeeeee");
        bucketPaths.put("low_quality", "testeeeee");

        MediaResponseDTO response = cassandraService.insertMedia(mediaForm, bucketPaths);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(uri).body(response);
    }


    /**
     * Retrieves all media entries from the database.
     * @return ResponseEntity containing a list of all media items
     */
    @GetMapping
    public ResponseEntity<List<MediaResponseDTO>> getAllMedia() {
        return ResponseEntity.ok(cassandraService.getAllMedia());
    }


    /**
     * Retrieves a specific media entry by its UUID.
     * @param id UUID of the media item to retrieve
     * @return ResponseEntity containing the requested media item's details
     */
    @GetMapping("/{id}")
    public ResponseEntity<MediaResponseDTO> getMediaById(@PathVariable UUID id) {
        return ResponseEntity.ok(cassandraService.getMediaById(id));
    }


    /**
     * Searches for media entries by title.
     * @param title Title of the media to search for
     * @return ResponseEntity containing a list of matching media items
     */
    @GetMapping("/title/{title}")
    public ResponseEntity<List<MediaResponseDTO>> getMediaByTitle(@PathVariable String title) {
        return ResponseEntity.ok(cassandraService.getMediaByTitle(title));
    }


    /**
     * Retrieves all media entries of a specific genre.
     * @param genre Genre to filter media by
     * @return ResponseEntity containing a list of media items in the specified genre
     */
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MediaResponseDTO>> getMediaByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(cassandraService.getMediaByGenre(genre));
    }


    /**
     * Deletes a media entry by its UUID.
     * Removes both the database entry and associated files from storage.
     * @param id UUID of the media item to delete
     * @return ResponseEntity with no content indicating successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        cassandraService.deleteMediaById(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Deletes all media entries with a specific title.
     * Removes both database entries and associated files from storage.
     * @param title Title of the media items to delete
     * @return ResponseEntity with no content indicating successful deletion
     */
    @DeleteMapping("/title/{title}")
    public ResponseEntity<Void> deleteByTitle(@PathVariable String title) {
        cassandraService.deleteMediaByTitle(title);
        return ResponseEntity.noContent().build();
    }
}