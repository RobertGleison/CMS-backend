package com.backend.Netflix.services;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


/**
 * Service responsible for handling media file uploads to Google Cloud Storage with support for video conversion using FFmpeg.
 */
@Service
public class GcpMediaUploadService {


    private final Storage storage;
    private final String projectId;
    private final String bucketName;

    @Autowired
    public GcpMediaUploadService(
            Storage storage,
            @Value("${cloudProjectId}") String projectId,
            @Value("${cloudBucketName}") String bucketName) {
        this.storage = storage;
        this.projectId = projectId;
        this.bucketName = bucketName;
    }


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
        System.out.println("Enter in upload gcp service");
        title = title.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        Map<String, String> bucketPaths = new HashMap<>();
        bucketPaths.put("LD_default", uploadVideoLowDefinition(title, videoFile));
        bucketPaths.put("HD_default", uploadVideoHighDefinition(title, videoFile));
        bucketPaths.put("thumbnail", uploadImage(title, thumbnail));
        bucketPaths.put("HD_HLS", uploadVideoHighDefinitionHLS(title, videoFile));
        bucketPaths.put("LD_HLS", uploadVideoLowDefinitionHLS(title, videoFile));
        return bucketPaths;
    }


    /**
     * Converts and uploads a video file to HLS format in high definition quality.
     * @param fileName Base name for the file
     * @param videoFile Video file to convert and upload
     * @return Public URL of the uploaded HLS playlist
     * @throws IOException If there's an error during conversion or upload
     */
    public String uploadVideoHighDefinitionHLS(String fileName, MultipartFile videoFile) throws IOException {
        System.out.println("Enter in upload HD HLSvideo");
        Map<String, byte[]> hlsFiles = convertToHLS(videoFile, "HD");
        return uploadHLSFiles(fileName, hlsFiles, "HD_HLS");
    }


    /**
     * Converts and uploads a video file to HLS format in low definition quality.
     * @param fileName Base name for the file
     * @param videoFile Video file to convert and upload
     * @return Public URL of the uploaded HLS playlist
     * @throws IOException If there's an error during conversion or upload
     * @throws InterruptedException If the video conversion process is interrupted
     */
    public String uploadVideoLowDefinitionHLS(String fileName, MultipartFile videoFile) throws IOException, InterruptedException {
        System.out.println("Enter in upload LD HLS video");

        // Create temporary directory
        String tempDir = System.getProperty("java.io.tmpdir");
        String uniqueId = UUID.randomUUID().toString();
        String workingDir = tempDir + File.separator + uniqueId;
        Files.createDirectories(Paths.get(workingDir));

        try {
            // Save input file
            Path inputPath = Paths.get(workingDir, "input.mp4");
            Path ldVideoPath = Paths.get(workingDir, "lowdef.mp4");
            Files.write(inputPath, videoFile.getBytes());

            // Convert to low definition
            String[] command = {
                    "ffmpeg",
                    "-i", inputPath.toString(),
                    "-vf", "scale=640:360",
                    "-c:v", "libx264",
                    "-preset", "ultrafast",
                    "-c:a", "aac",              // Keep audio codec
                    "-ar", "44100",             // Audio sample rate
                    "-b:a", "128k",             // Audio bitrate
                    "-movflags", "+faststart",
                    "-y",
                    ldVideoPath.toString()
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Log FFmpeg output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Error converting to low definition: " + exitCode);
            }

            // Convert to HLS
            MultipartFile ldVideo = new MockMultipartFile(
                    "lowdef.mp4",
                    "lowdef.mp4",
                    "video/mp4",
                    Files.readAllBytes(ldVideoPath)
            );

            Map<String, byte[]> hlsFiles = convertToHLS(ldVideo, "LD");
            return uploadHLSFiles(fileName, hlsFiles, "LD_HLS");

        } finally {
            // Clean up
            deleteDirectory(new File(workingDir));
        }
    }


    /**
     * Uploads HLS files (playlist and segments) to Google Cloud Storage.
     * @param fileName Base name for the files
     * @param hlsFiles Map containing HLS file names and their contents
     * @param hlsType Type of HLS content (HD_HLS_video or LD_HLS_video)
     * @return Public URL of the uploaded playlist file
     * @throws IOException If there's an error during upload
     */
    private String uploadHLSFiles(String fileName, Map<String, byte[]> hlsFiles, String hlsType) throws IOException {
        System.out.println("Enter in upload HLS Files");
        // Remove this line since we'll use the injected storage
        // Storage storage = StorageOptions.newBuilder().setProjectId(this.projectId).build().getService();

        for (Map.Entry<String, byte[]> entry : hlsFiles.entrySet()) {
            String hlsFileName = entry.getKey();
            byte[] fileContent = entry.getValue();

            // Create the full path in the bucket
            String bucketFilePath = String.format("%s/%s/%s", fileName, hlsType, hlsFileName);
            System.out.println("Uploading file: " + bucketFilePath);

            // Upload the file using the injected storage client
            BlobId blobId = BlobId.of(this.bucketName, bucketFilePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(getContentType(hlsFileName))
                    .build();

            this.storage.create(blobInfo, fileContent);
        }

        // Return the playlist URL
        return String.format("https://storage.cloud.google.com/%s/%s/%s/output.m3u8",
                bucketName, fileName, hlsType);
    }


    /**
     * Determines the appropriate content type for HLS files.
     * @param fileName Name of the file
     * @return Content type string
     */
    private String getContentType(String fileName) {
        if (fileName.endsWith(".m3u8")) {
            return "application/vnd.apple.mpegurl";
        } else if (fileName.endsWith(".ts")) {
            return "video/MP2T";
        }
        return "application/octet-stream";
    }


    /**
     * Converts a video file to HLS format with specified quality settings.
     * @param videoFile Video file to convert
     * @param quality Quality level ("HD" or "LD")
     * @return Map containing HLS file names and their contents
     * @throws IOException If there's an error during conversion
     */
    private Map<String, byte[]> convertToHLS(MultipartFile videoFile, String quality) throws IOException {
        System.out.println("Converting to HLS");
        byte[] inputBytes = videoFile.getBytes();
        String tempDir = System.getProperty("java.io.tmpdir");
        String uniqueId = UUID.randomUUID().toString();
        String workingDir = tempDir + File.separator + uniqueId;
        String streamDir = workingDir + File.separator + "stream";  // Define stream directory path
        Map<String, byte[]> hlsFiles = new HashMap<>();

        try {
            // Create both working directory AND stream directory
            Files.createDirectories(Paths.get(workingDir));
            Files.createDirectories(Paths.get(streamDir));  // Create stream directory explicitly

            // Write input file to temp directory
            Path inputPath = Paths.get(workingDir, "input.mp4");
            Files.write(inputPath, inputBytes);

            String[] command = {
                    "ffmpeg",
                    "-i", inputPath.toString(),
                    "-map", "0:v:0",
                    "-map", "0:a:0?",         // Optional audio mapping with ?
                    "-c:v", "libx264",
                    "-b:v", quality.equals("HD") ? "2800k" : "800k",
                    "-maxrate", quality.equals("HD") ? "3000k" : "856k",
                    "-bufsize", quality.equals("HD") ? "6000k" : "1712k",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-preset", "fast",
                    "-keyint_min", "48",
                    "-g", "48",
                    "-sc_threshold", "0",
                    "-hls_time", "4",
                    "-hls_playlist_type", "vod",
                    "-hls_list_size", "0",
                    "-hls_segment_filename", streamDir + "/data%d.ts",  // Use streamDir variable
                    streamDir + "/output.m3u8"  // Use streamDir variable
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read process output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Error processing video with FFmpeg: " + exitCode);
            }

            // Read all generated files
            File streamDirectory = new File(streamDir);  // Convert String path to File object
            File[] files = streamDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    hlsFiles.put(file.getName(), Files.readAllBytes(file.toPath()));
                }
            }

            return hlsFiles;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("HLS conversion interrupted", e);
        } finally {
            // Clean up temp directory
            deleteDirectory(new File(workingDir));
        }
    }


    /**
     * Gets the file extension from a filename.
     * @param filename The filename to extract extension from
     * @return The file extension including the dot, or empty string if none exists
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf(".");
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }


    /**
     * Recursively deletes a directory and all its contents.
     * @param directory The directory to delete
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
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
        System.out.println("Enter in upload HD video");
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
        System.out.println("Enter in upload LD video");
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
        System.out.println("Enter in upload thumbnail");
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
        // Use the injected storage client instead of creating a new one
        BlobId blobId = BlobId.of(bucketName, objectName);
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
    /**
     * Converts a video file to low definition (360p) using FFmpeg.
     * Uses external FFmpeg process to perform the conversion with the following settings:
     * - Resolution: 640x360
     * - Codec: H.264
     * - Preset: ultrafast (for faster processing)
     * - Format: MP4
     * - Thread count: Optimized for current CPU
     * @param inputFile Original video file to convert
     * @return Converted video file as MultipartFile
     * @throws IOException If there's an error handling the files
     * @throws InterruptedException If the FFmpeg process is interrupted
     * @throws RuntimeException If the FFmpeg process fails
     */
    public MultipartFile convertToLowDefinition(MultipartFile inputFile) throws IOException, InterruptedException {
        System.out.println("Converting to Low definition");

        // Create temporary directory and files
        String tempDir = System.getProperty("java.io.tmpdir");
        String uniqueId = UUID.randomUUID().toString();
        Path workingDir = Paths.get(tempDir, uniqueId);
        Files.createDirectories(workingDir);

        Path inputPath = workingDir.resolve("input.mp4");
        Path outputPath = workingDir.resolve("output.mp4");

        try {
            // Write input file
            Files.write(inputPath, inputFile.getBytes());

            String[] command = {
                    "ffmpeg",
                    "-i", inputPath.toString(),
                    "-vf", "scale=640:360",
                    "-c:v", "libx264",
                    "-preset", "ultrafast",
                    "-threads", "0",
                    "-movflags", "+faststart",
                    "-y",
                    outputPath.toString()
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Log FFmpeg output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Error processing video with FFmpeg: Exit code " + exitCode);
            }

            // Read the output file
            byte[] outputBytes = Files.readAllBytes(outputPath);

            return new MockMultipartFile(
                    "converted.mp4",
                    "converted.mp4",
                    "video/mp4",
                    outputBytes
            );

        } finally {
            // Clean up temporary files
            try {
                Files.deleteIfExists(inputPath);
                Files.deleteIfExists(outputPath);
                Files.deleteIfExists(workingDir);
            } catch (IOException e) {
                System.err.println("Error cleaning up temporary files: " + e.getMessage());
            }
        }
    }
}