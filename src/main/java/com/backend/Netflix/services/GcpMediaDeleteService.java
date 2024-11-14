package com.backend.Netflix.services;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class GcpMediaDeleteService {

    private static final Logger logger = Logger.getLogger(GcpMediaDeleteService.class.getName());

    @Value("${cloudProjectId}")
    String projectId;

    // The ID of your GCS bucket
    @Value("${cloudBucketName}")
    String bucketName;

    public void deleteMovieFile(String fileName) {
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        Blob blob = storage.get(bucketName, fileName);
        if (blob == null) {
            logger.info("!!!! Object " + fileName + " was deleted from " + bucketName);
//            System.out.println("The object " + fileName + " wasn't found in " + bucketName);
            return;
        }

        // Optional: set a generation-match precondition to avoid potential race
        // conditions and data corruptions. The request to upload returns a 412 error if
        // the object's generation number does not match your precondition.
        Storage.BlobSourceOption precondition =
                Storage.BlobSourceOption.generationMatch(blob.getGeneration());

        storage.delete(bucketName, fileName, precondition);
        logger.info("Object " + blob.getName() + " was deleted from " + bucketName);
//        System.out.println("Object " + fileName + " was deleted from " + bucketName);
    }

    public void deleteMovieFolder(String movieTitle) {
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

        for (Blob blob :
                storage.list(bucketName,
                            Storage.BlobListOption.currentDirectory(),
                            Storage.BlobListOption.prefix(movieTitle + "/"))
                        .iterateAll()) {
//            if (blob==null)
//                continue;
            logger.info("Object " + blob.getName() + " was deleted from " + bucketName);
//            System.out.println("Object " + blob.getName() + " was deleted from " + bucketName);
            blob.delete();
        }
    }
}
