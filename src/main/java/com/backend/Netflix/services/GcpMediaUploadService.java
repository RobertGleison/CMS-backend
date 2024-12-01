package com.backend.Netflix.services;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Service responsible for handling media file uploads to Google Cloud Storage with support for video conversion using FFmpeg.
 */
@Service
public class GcpMediaUploadService {


    @Value("${cloudProjectId}")
    String projectId;


    @Value("${cloudBucketName}")
    String bucketName;


    /**
     * Uploads a video file and its thumbnail to Google Cloud Storage.
     * Creates three versions: high definition, low definition, and thumbnail.
     * @param title Title of the media, used for file naming
     * @param videoFile Original video file to be uploaded
     * @param thumbnail Thumbnail image for the video
     * @return Map containing URLs for all uploaded versions (high_quality, low_quality, thumbnail)
     * @throws IOException If there's an error handling the files
     * @throws InterruptedException If the video conversion process is interrupted
     */
    public Map<String, String> upload(String title, MultipartFile videoFile, MultipartFile thumbnail) throws IOException, InterruptedException {
        Map<String, String> bucketPaths = new HashMap<>();
        bucketPaths.put("low_quality", uploadVideoLowDefinition(title, videoFile));
        bucketPaths.put("high_quality", uploadVideoHighDefinition(title, videoFile));
        bucketPaths.put("thumbnail", uploadImage(title, thumbnail));
        return bucketPaths;
    }


    /**
     * Uploads the high definition version of the video.
     * Creates a file path in format: {fileName}/HD_video.{extension}
     * @param fileName Base name for the file
     * @param videoFile Video file to upload
     * @return Public URL of the uploaded file
     * @throws IOException If there's an error during upload
     */
    public String uploadVideoHighDefinition(String fileName, MultipartFile videoFile) throws IOException {
        String bucketFileName = String.format("%s/%s_%s.%s", fileName, "HD", "video", Objects.requireNonNull(videoFile.getContentType()).split("/")[1]);
        return streamObjectUpload(bucketFileName, videoFile);
    }


    /**
     * Converts and uploads the low definition version of the video.
     * Creates a file path in format: {fileName}/LD_video.{extension}
     * @param fileName Base name for the file
     * @param videoFile Video file to convert and upload
     * @return Public URL of the uploaded file
     * @throws IOException If there's an error during conversion or upload
     * @throws InterruptedException If the conversion process is interrupted
     */
    public String uploadVideoLowDefinition(String fileName, MultipartFile videoFile) throws IOException, InterruptedException {
        String bucketFileName = String.format("%s/%s_%s.%s", fileName, "LD", "video", Objects.requireNonNull(videoFile.getContentType()).split("/")[1]);
        MultipartFile newFile = convertToLowDefinition(videoFile);
        return streamObjectUpload(bucketFileName, newFile);
    }


    /**
     * Uploads the thumbnail image.
     * Creates a file path in format: {fileName}/thumbnail.{extension}
     * @param fileName Base name for the file
     * @param thumbnail Thumbnail image to upload
     * @return Public URL of the uploaded thumbnail
     * @throws IOException If there's an error during upload
     */
    public String uploadImage(String fileName, MultipartFile thumbnail) throws IOException {
        String bucketFileName = String.format("%s/%s_.%s", fileName, "thumbnail", Objects.requireNonNull(thumbnail.getContentType()).split("/")[1]);
        return streamObjectUpload(bucketFileName, thumbnail);
    }


    /**
     * Handles the actual streaming upload of files to Google Cloud Storage.
     * Uses a 1MB buffer for efficient streaming of large files.
     * @param objectName Full path/name of the file in the bucket
     * @param file File to upload
     * @return Public URL of the uploaded file
     * @throws IOException If there's an error during upload
     */
    public String streamObjectUpload(String objectName, MultipartFile file) throws IOException {
        Storage storage = StorageOptions.newBuilder().setProjectId(this.projectId).build().getService();
        BlobId blobId = BlobId.of(this.bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        try (WriteChannel writer = storage.writer(blobInfo);
             InputStream inputStream = file.getInputStream()) {

            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                writer.write(byteBuffer);
            }

            System.out.println("Wrote " + objectName + " to bucket " + bucketName + " using a WriteChannel.");
            return String.format("https://storage.cloud.google.com/%s/%s", bucketName, objectName);
        }
    }


    /**
     * Converts a video file to low definition (360p) using FFmpeg.
     * Uses external FFmpeg process to perform the conversion with the following settings:
     * - Resolution: 640x360
     * - Codec: H.264
     * - Preset: fast
     * - Format: MP4
     * @param inputFile Original video file to convert
     * @return Converted video file as MultipartFile
     * @throws IOException If there's an error handling the files
     * @throws InterruptedException If the FFmpeg process is interrupted
     * @throws RuntimeException If the FFmpeg process fails
     */
    public MultipartFile convertToLowDefinition(MultipartFile inputFile) throws IOException, InterruptedException {
        byte[] inputBytes = inputFile.getBytes();

        String[] command = {
                "C:\\ffmpeg\\bin\\ffmpeg.exe",
                "-i", "pipe:0",
                "-vf", "scale=640:360",
                "-f", "mp4",
                "-vcodec", "libx264",
                "-preset", "fast",
                "-movflags", "frag_keyframe+empty_moov",
                "pipe:1"
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (InputStream inputStream = new ByteArrayInputStream(inputBytes)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                process.getOutputStream().write(buffer, 0, bytesRead);
            }
            process.getOutputStream().flush();
            process.getOutputStream().close();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream processInput = process.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = processInput.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Erro ao processar o vídeo com FFmpeg.");
        }

        byte[] outputBytes = outputStream.toByteArray();
        return new MockMultipartFile(
                "converted.mp4",
                "converted.mp4",
                "video/mp4",
                outputBytes
        );
    }
}