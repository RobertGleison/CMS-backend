package com.backend.Netflix.services;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;


import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service
public class GcpMediaUploadService {

    // The ID of your GCP project
    @Value("${cloudProjectId}")
    String projectId;


    // The ID of your GCS bucket
    @Value("${cloudBucketName}")
    String bucketName;

//    @Value("${ffmpegPath}")
//    String ffmpegPath;
//
//    @Value("${ffprobePath}")
//    String ffprobePath;


    public Map<String, String> upload(String title, MultipartFile videoFile, MultipartFile thumbnail) throws IOException, InterruptedException {
        Map<String, String> bucketPaths = new HashMap<>();
        bucketPaths.put("360p", uploadVideoLowQuality(title, videoFile));
        bucketPaths.put("1080p", uploadVideoHighQuality(title, videoFile));
        bucketPaths.put("thumbnail", uploadImage(title, thumbnail));
        return bucketPaths;
    }


    public String uploadVideoHighQuality(String title, MultipartFile videoFile) throws IOException {
        String bucketFileName = String.format("%s/%s_%s.%s", title, "1080p", title, Objects.requireNonNull(videoFile.getContentType()).split("/")[1]);
        return streamObjectUpload(bucketFileName, videoFile);
    }


    public String uploadVideoLowQuality(String title, MultipartFile videoFile) throws IOException, InterruptedException {
        String bucketFileName = String.format("%s/%s_%s.%s", title, "360p", title, Objects.requireNonNull(videoFile.getContentType()).split("/")[1]);
        String newFile = convertTo360p(videoFile, title);
        return uploadObject(bucketFileName, newFile);
    }


    public String uploadImage(String title, MultipartFile thumbnail) throws IOException {
        String bucketFileName = String.format("%s/%s_%s.%s", title, "thumbnail", title, Objects.requireNonNull(thumbnail.getContentType()).split("/")[1]);
        return streamObjectUpload(bucketFileName, thumbnail);
    }


    public String uploadObject(String bucketFileName, String filePath) throws IOException {
        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, bucketFileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        Storage.BlobWriteOption precondition;
        if (storage.get(bucketName, bucketFileName) == null) {
            precondition = Storage.BlobWriteOption.doesNotExist();
        } else {
            precondition =
                    Storage.BlobWriteOption.generationMatch(
                            storage.get(bucketName, bucketFileName).getGeneration());
        }
        storage.createFrom(blobInfo, Paths.get(filePath), precondition);

        System.out.println(
                "File " + filePath + " uploaded to bucket " + bucketName + " as " + bucketFileName);

        deleteMp4File("C:\\Users\\Sophia\\OneDrive\\Documentos\\CC\\Programacao\\DispositivosMoveis\\CMS-backend\\" + filePath);

        return "https://storage.cloud.google.com/netflixplus-library-cc2024/" + bucketFileName;
    }


    public String streamObjectUpload(String objectName, MultipartFile file) throws IOException {
        Storage storage = StorageOptions.newBuilder().setProjectId(this.projectId).build().getService();
        BlobId blobId = BlobId.of(this.bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        try (WriteChannel writer = storage.writer(blobInfo);
             InputStream inputStream = file.getInputStream()) {

            // Use 1MB buffer
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                writer.write(byteBuffer);
            }

            System.out.println(
                    "Wrote " + objectName + " to bucket " + bucketName + " using a WriteChannel.");

            return "https://storage.cloud.google.com/netflixplus-library-cc2024/" + objectName;
        }
    }

    public String convertTo360p(MultipartFile inputFile, String title) throws IOException, InterruptedException {
        String fileName = downloadMultipartAsMp4(inputFile, title, "C:\\Users\\Sophia\\OneDrive\\Documentos\\CC\\Programacao\\DispositivosMoveis\\CMS-backend");
        String convertedVideoPath = convertVideo(fileName, title);
        deleteMp4File("C:\\Users\\Sophia\\OneDrive\\Documentos\\CC\\Programacao\\DispositivosMoveis\\CMS-backend\\" + fileName);
        return convertedVideoPath;
    }

    public String downloadMultipartAsMp4(MultipartFile multipartFile, String title, String targetDirectory) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado está vazio.");
        }

        File directory = new File(targetDirectory);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Não foi possível criar o diretório de destino: " + targetDirectory);
            }
        }

        String fileName = title + "Original.mp4";
        File targetFile = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(multipartFile.getBytes());
        }

        return fileName; // Retorna o nome criado
    }

    public String convertVideo(String fileName, String title) throws IOException, InterruptedException {
        FFmpeg ffmpeg = new FFmpeg("C:\\ffmpeg\\bin\\ffmpeg.exe");
        FFprobe ffprobe = new FFprobe("C:\\ffmpeg\\bin\\ffprobe.exe");

        String outputFile = title + "360p.mp4";
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(fileName)     // Filename, or a FFmpegProbeResult
                .overrideOutputFiles(true) // Override the output if it exists

                .addOutput(outputFile)   // Filename for the destination
                .setFormat("mp4")        // Format is inferred from filename, or can be set
                .setVideoResolution(640, 360) // at 360p

                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        executor.createJob(builder).run();
        return outputFile;

    }

    public boolean deleteMp4File(String filePath) {
        // Cria uma instância de File apontando para o caminho do arquivo
        File file = new File(filePath);

        // Verifica se o arquivo existe antes de tentar deletar
        if (!file.exists()) {
            System.out.println("Arquivo não encontrado: " + filePath);
            return false;
        }

        // Tenta deletar o arquivo
        if (file.delete()) {
            System.out.println("Arquivo deletado com sucesso: " + filePath);
            return true;
        } else {
            System.out.println("Falha ao deletar o arquivo: " + filePath);
            return false;
        }
    }
}

