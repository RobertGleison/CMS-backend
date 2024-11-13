package com.backend.Netflix.controllers;

import com.backend.Netflix.model.MediaRequestMultiform;
import com.backend.Netflix.model.Media;
import com.backend.Netflix.services.CassandraMediaService;
import com.backend.Netflix.services.GcpMediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {


    @Autowired
    private CassandraMediaService cassandraService;


    @Autowired
    private GcpMediaUploadService gcpService;


    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Media> processMedia(@ModelAttribute @RequestBody MediaRequestMultiform mediaForm) throws IOException {
        Media response = cassandraService.insertMedia(mediaForm);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(uri).body(response);
    }


    @GetMapping
    public ResponseEntity<List<Media>> getAllMedia() {
        return ResponseEntity.ok(cassandraService.getAllMedia());
    }


    @GetMapping("/{id}")
    public ResponseEntity<Media> getMediaById(@PathVariable UUID id) {
        return ResponseEntity.ok(cassandraService.getMediaById(id));
    }


    @GetMapping("/title/{title}")
    public ResponseEntity<List<Media>> getMediaByTitle(@PathVariable String title) {
        return ResponseEntity.ok(cassandraService.getMediaByTitle(title));
    }


    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<Media>> getMediaByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(cassandraService.getMediaByGenre(genre));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        cassandraService.deleteMediaById(id);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/title/{title}")
    public ResponseEntity<Void> deleteByTitle(@PathVariable String title) {
        cassandraService.deleteMediaByTitle(title);
        return ResponseEntity.noContent().build();
    }
}