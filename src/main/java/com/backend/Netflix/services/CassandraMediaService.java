package com.backend.Netflix.services;

import com.backend.Netflix.exceptions.DBInsertException;
import com.backend.Netflix.exceptions.DatabaseAccessException;
import com.backend.Netflix.model.MediaRequestMultiform;
import com.backend.Netflix.model.MediaResponseDTO;
import com.backend.Netflix.exceptions.MediaNotFoundException;
import com.backend.Netflix.model.Media;
import com.backend.Netflix.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

@Service
public class CassandraMediaService {
    private static final Logger logger = Logger.getLogger(CassandraMediaService.class.getName());


    @Autowired
    private MediaRepository mediaRepository;


    @Autowired
    private GcpMediaUploadService gcpService;


    public Media insertMedia(MediaRequestMultiform mediaForm, Map<String, String> bucketPaths) throws IOException {

            logger.info("Received file upload request");
            UUID id = UUID.randomUUID();
            String filename = String.format(
                            "%s.%s",
                            mediaForm.titleBody(),
                            Objects.requireNonNull(mediaForm.videoPart().getContentType()).split("/")[1])
                            .replaceAll(" ", "_");

            logger.info("Filename:" + filename);


            LocalDateTime timestamp = LocalDateTime.now();

            Media media = new Media(
                    id,
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
        try {
            return mediaRepository.save(media);
        }
        catch (RuntimeException e) {
            throw new DBInsertException(id);
        }
    }


    public List<Media> getAllMedia() {
        return mediaRepository.findAll();
    }


    public Media getMediaById(UUID id) {
        return mediaRepository.findById(id).orElseThrow(() -> MediaNotFoundException.byId(id));
    }


    public List<Media> getMediaByTitle(String title) {
        return mediaRepository.findByTitleContaining("%" + title + "%").orElseThrow(() -> MediaNotFoundException.byTitle(title));
    }


    public List<Media> getMediaByGenre(String genre) {
        return mediaRepository.findByGenre(genre).orElseThrow(() -> MediaNotFoundException.byGenre(genre));
    }


    public void deleteMediaById(UUID id) {
        if (!mediaRepository.existsById(id)) {
            throw MediaNotFoundException.byId(id);
        }
        mediaRepository.deleteById(id);
    }


    public void deleteMediaByTitle(String title) {
        Optional<List<Media>> mediaList = mediaRepository.findByTitle(title);
        if (mediaList.isEmpty()) throw MediaNotFoundException.byTitle(title);

        try {
            mediaRepository.deleteByTitle(title);
            logger.info("Object " + title + " was deleted from database");
        }
        catch (RuntimeException e) {
            throw new DatabaseAccessException("Error deleting media with title: " + title);
        }
    }


    private MediaResponseDTO convertMediaToDTO(Media media) {
        return new MediaResponseDTO(
                media.getTitle(),
                media.getDescription(),
                media.getGenre(),
                media.getYear(),
                media.getPublisher(),
                media.getDuration()
        );
    }
}