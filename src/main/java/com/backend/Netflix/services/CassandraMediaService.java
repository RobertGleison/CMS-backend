package com.backend.Netflix.services;

import com.backend.Netflix.model.MediaRequestDTO;
import com.backend.Netflix.exceptions.MediaNotFoundException;
import com.backend.Netflix.model.MediaResponseDTO;
import com.backend.Netflix.repository.MediaRepository;
import com.google.firebase.database.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * Service class to handle media-related operations with Cassandra database.
 * Integrates with GCP for media file storage and Cassandra for metadata persistence.
 */
@Service
public class CassandraMediaService {
    private static final Logger logger = Logger.getLogger(CassandraMediaService.class.getName());

    @Value("${cloudBucketName}")
    String bucketName;

    @Autowired
    private MediaRepository mediaRepository;


    @Autowired
    private GcpMediaUploadService gcpService;


    /**
     * Processes the uploaded media file and stores the metadata as a new entry in Cassandra.
     * @param mediaForm DTO containing the media details and files
     * @param bucketPaths Map containing paths to stored media files in GCP buckets
     * @return MediaResponseDTO containing the created media entry details
     * @throws IOException if there's an error processing the media files
     * @throws DatabaseException if there's an error saving to the database
     */
    public MediaResponseDTO insertMedia(MediaRequestDTO mediaForm, Map<String, String> bucketPaths) throws IOException {
        logger.info("Received file upload request");

        UUID id = UUID.randomUUID();
        String prefix = String.format("https://storage.googleapis.com/%s/%s", bucketName, mediaForm.getTitle());

        logger.info("prefix:" + prefix);
        LocalDateTime timestamp = LocalDateTime.now();

        MediaResponseDTO media = new MediaResponseDTO(
                        id,
                        mediaForm.getTitle(),
                        mediaForm.getDescription(),
                        mediaForm.getGenre(),
                        mediaForm.getYear(),
                        mediaForm.getPublisher(),
                        mediaForm.getDuration(),
                        prefix,
                        bucketPaths,
                        timestamp
                     );
        try {
            return mediaRepository.save(media);
        } catch (RuntimeException e) {
            throw new DatabaseException(e.getMessage());
        }
    }


    /**
     * Retrieves all media entries from the database.
     * @return List of all media entries
     */
    public List<MediaResponseDTO> getAllMedia() {
        return mediaRepository.findAll();
    }


    public boolean ifMediaExists(String movieTitle) {
        Optional<List<MediaResponseDTO>> result = mediaRepository.findByTitle(movieTitle);
        return result.isPresent() && !result.get().isEmpty();
    }


    /**
     * Retrieves a specific media entry by its UUID.
     * @param id UUID of the media to retrieve
     * @return MediaResponseDTO containing the media details
     * @throws MediaNotFoundException if no media is found with the given ID
     */
    public MediaResponseDTO getMediaById(UUID id) {
        return mediaRepository.findById(id).orElseThrow(() -> MediaNotFoundException.byId(id));
    }


    /**
     * Searches for media entries containing the given title string.
     * @param title Title string to search for
     * @return List of media entries matching the title search
     * @throws MediaNotFoundException if no media is found with the given title
     */
    public List<MediaResponseDTO> getMediaByTitle(String title) {
        return mediaRepository.findByTitleContaining("%" + title + "%").orElseThrow(() -> MediaNotFoundException.byTitle(title));
    }


    /**
     * Retrieves all media entries of a specific genre.
     * @param genre Genre to filter by
     * @return List of media entries in the specified genre
     * @throws MediaNotFoundException if no media is found in the given genre
     */
    public List<MediaResponseDTO> getMediaByGenre(String genre) {
        return mediaRepository.findByGenre(genre).orElseThrow(() -> MediaNotFoundException.byGenre(genre));
    }


    /**
     * Deletes a media entry by its UUID.
     * @param id UUID of the media to delete
     * @throws MediaNotFoundException if no media is found with the given ID
     */
    public void deleteMediaById(UUID id) {
        if (!mediaRepository.existsById(id)) {
            throw MediaNotFoundException.byId(id);
        }
        mediaRepository.deleteById(id);
    }


    public void deleteAllMedia(){
        mediaRepository.deleteAll();
    }

    /**
     * Deletes all media entries with the specified title.
     * @param title Title of the media entries to delete
     * @throws MediaNotFoundException if no media is found with the given title
     * @throws DatabaseException if there's an error during the deletion process
     */
    public void deleteMediaByTitle(String title) {
        Optional<List<MediaResponseDTO>> mediaList = mediaRepository.findByTitle(title);
        if (mediaList.isEmpty()) throw MediaNotFoundException.byTitle(title);

        try {
            for (MediaResponseDTO media : mediaList.get()) {
                mediaRepository.deleteById(media.getId());
            }
        } catch (RuntimeException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}