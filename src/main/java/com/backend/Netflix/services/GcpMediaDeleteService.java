package com.backend.Netflix.services;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.logging.Logger;

@Service
public class GcpMediaDeleteService {
    private static final Logger logger = Logger.getLogger(GcpMediaDeleteService.class.getName());

    private final Storage storage;
    private final String bucketName;

    @Autowired
    public GcpMediaDeleteService(
            Storage storage,
            @Value("${cloudBucketName}") String bucketName) {
        this.storage = storage;
        this.bucketName = bucketName;
    }

    /**
     * Deletes all files in a movie folder based on the movie title.
     * Folder structure example: "Avatar/video.mp4", "Avatar/thumbnail.jpg"
     * @param movieTitle The movie title which is used as the folder name
     */
    public void deleteMediaByTitle(String movieTitle) {
        String folderPrefix = movieTitle.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        String folderPath = folderPrefix + "/";

        logger.info("Starting deletion of movie folder: " + folderPath);

        // First delete all files in the folder
        for (Blob blob : storage.list(
                        bucketName,
                        Storage.BlobListOption.prefix(folderPath))
                .iterateAll()) {
            try {
                blob.delete();
                logger.info("Deleted file: " + blob.getName());
            } catch (Exception e) {
                logger.warning("Failed to delete file: " + blob.getName() + ". Error: " + e.getMessage());
            }
        }

        // Now delete the empty folder itself
        Blob folderBlob = storage.get(bucketName, folderPath);
        if (folderBlob != null) {
            try {
                folderBlob.delete();
                logger.info("Deleted empty folder: " + folderPath);
            } catch (Exception e) {
                logger.warning("Failed to delete empty folder: " + folderPath + ". Error: " + e.getMessage());
            }
        }

        logger.info("Completed deletion of movie folder and its contents: " + folderPath);
    }
}