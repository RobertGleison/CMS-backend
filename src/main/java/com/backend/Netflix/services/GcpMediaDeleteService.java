package com.backend.Netflix.services;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

/**
 * Service responsible for deleting media files and folders from Google Cloud Storage.
 */
@Service
public class GcpMediaDeleteService {
    private static final Logger logger = Logger.getLogger(GcpMediaDeleteService.class.getName());


    @Value("${cloudProjectId}")
    String projectId;


    @Value("${cloudBucketName}")
    String bucketName;


    /**
     * Deletes a specific file from Google Cloud Storage.
     * Uses generation matching precondition to prevent race conditions.
     * Logs the deletion operation status.
     * @param fileName The complete path and name of the file to delete within the bucket
     */
    public void deleteMovieFile(String fileName) {
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        Blob blob = storage.get(bucketName, fileName);
        if (blob == null) {
            logger.info("!!!! Object " + fileName + " was deleted from " + bucketName);
            return;
        }

        // Generation-match precondition to prevent race conditions and data corruptions
        Storage.BlobSourceOption precondition = Storage.BlobSourceOption.generationMatch(blob.getGeneration());

        storage.delete(bucketName, fileName, precondition);
        logger.info("Object " + blob.getName() + " was deleted from " + bucketName);
    }


    /**
     * Deletes an entire movie folder and all its contents from Google Cloud Storage.
     * This includes all quality versions of the video and the thumbnail.
     * Iterates through all objects with the specified movie title prefix.
     * @param movieTitle The title of the movie, which is used as the folder name in storage
     *                  Expected folder structure: {movieTitle}/[HD_video.mp4, LD_video.mp4, thumbnail.jpg]
     */
    public void deleteMovieFolder(String movieTitle) {
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

        for (Blob blob :
                storage.list(bucketName,
                            Storage.BlobListOption.currentDirectory(),
                            Storage.BlobListOption.prefix(movieTitle + "/"))
                            .iterateAll()) {
            logger.info("Object " + blob.getName() + " was deleted from " + bucketName);
            blob.delete();
        }
    }
}