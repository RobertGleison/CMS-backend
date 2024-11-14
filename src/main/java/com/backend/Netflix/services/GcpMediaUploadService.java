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
//        bucketPaths.put("360p", uploadVideoLowQuality(title, videoFile));
        bucketPaths.put("1080p", uploadVideoHighQuality(title, videoFile));
        bucketPaths.put("thumbnail", uploadImage(title, thumbnail));
        return bucketPaths;
    }


    public String uploadVideoHighQuality(String fileName, MultipartFile videoFile) throws IOException {
        String bucketFileName = String.format("%s/%s_%s.%s", fileName, "1080p", fileName, Objects.requireNonNull(videoFile.getContentType()).split("/")[1]);
        return streamObjectUpload(bucketFileName, videoFile);
    }


    public String uploadVideoLowQuality(String fileName, MultipartFile videoFile) throws IOException, InterruptedException {
        String bucketFileName = String.format("%s/%s_%s.%s", fileName, "360p", fileName, Objects.requireNonNull(videoFile.getContentType()).split("/")[1]);
        MultipartFile newFile = convertTo360p(videoFile);
        return streamObjectUpload(bucketFileName, newFile);
    }


    public String uploadImage(String fileName, MultipartFile thumbnail) throws IOException {
        String bucketFileName = String.format("%s/%s_%s.%s", fileName, "thumbnail", fileName, Objects.requireNonNull(thumbnail.getContentType()).split("/")[1]);
        return streamObjectUpload(bucketFileName, thumbnail);
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

    public MultipartFile convertTo360p(MultipartFile inputFile) throws IOException, InterruptedException {
        // Extrai os bytes do MultipartFile de entrada
        byte[] inputBytes = inputFile.getBytes();

//        FFmpeg ffmpeg = new FFmpeg("ffmpeg");
//        FFprobe ffprobe = new FFprobe("C:\\ffmpeg\\bin\\ffprobe.exe"); // Caminho para o FFprobe no Windows


        // Comando FFmpeg para redimensionar o vídeo para 360p e enviar para stdout
        String[] command = {
                "C:\\ffmpeg\\bin\\ffmpeg.exe",
                "-i", "pipe:0",                 // Entrada do vídeo via pipe
                "-vf", "scale=640:360",         // Redimensiona para 640x360
                "-f", "mp4",                    // Formato de saída
                "-vcodec", "libx264",           // Codec de vídeo
                "-preset", "fast",              // Preset de compressão rápida
                "-movflags", "frag_keyframe+empty_moov", // Flags para streaming
                "pipe:1"                        // Envia a saída para o stdout
        };

        // Inicia o processo FFmpeg
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Redireciona erros para o stdout
        Process process = processBuilder.start();

        // Envia os bytes de entrada para o processo FFmpeg
        try (InputStream inputStream = new ByteArrayInputStream(inputBytes)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                process.getOutputStream().write(buffer, 0, bytesRead);
            }
            process.getOutputStream().flush();
            process.getOutputStream().close();
        }

        // Captura a saída do FFmpeg em um ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream processInput = process.getInputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = processInput.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        // Espera o processo terminar
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Erro ao processar o vídeo com FFmpeg.");
        }

        // Converte o output do FFmpeg para um MultipartFile
        byte[] outputBytes = outputStream.toByteArray();
        return new MockMultipartFile(
                "converted.mp4",                   // Nome do arquivo
                "converted.mp4",                   // Nome original
                "video/mp4",                       // Tipo de conteúdo
                outputBytes                        // Conteúdo convertido
        );
    }

//    public MultipartFile convertVideo(String path) throws IOException, InterruptedException {
//        String[] command = {
//                "ffmpeg",
//                "-i", "testeconverter.mp4",       // Arquivo de entrada
//                "-vf", "scale=640:360",           // Redimensiona para 640x360
//                "-f", "mp4",                      // Formato do output
//                "-vcodec", "libx264",             // Codec de vídeo
//                "-preset", "fast",                // Preset para compressão rápida
//                "-movflags", "frag_keyframe+empty_moov", // Flags para streaming no stdout
//                "pipe:1"                          // Direciona a saída para o stdout
//        };
//
//        // Inicializa o processo usando ProcessBuilder
//        ProcessBuilder processBuilder = new ProcessBuilder(command);
//        processBuilder.redirectErrorStream(true); // Redireciona o stderr para stdout
//        Process process = processBuilder.start();
//
//        // Captura o output do FFmpeg em um ByteArrayOutputStream
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        InputStream processInput = process.getInputStream();
//
//        byte[] buffer = new byte[4096];
//        int bytesRead;
//        while ((bytesRead = processInput.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//        }
//
//        // Espera o processo finalizar
//        int exitCode = process.waitFor();
//        if (exitCode != 0) {
//            System.err.println("Erro ao processar o vídeo.");
//            return null;
//        }
//
//        // Converte o output para um array de bytes
//        byte[] videoData = outputStream.toByteArray();
//
//        // Cria um MultipartFile usando o MockMultipartFile
//        MultipartFile multipartFile = new MockMultipartFile(
//                "output.mp4",                    // Nome do arquivo
//                "output.mp4",                    // Nome original (pode ser o mesmo)
//                "video/mp4",                     // Tipo de conteúdo
//                videoData                         // Dados do arquivo em byte array
//        );
//
//        return multipartFile;
////            System.out.println("Vídeo processado e armazenado como MultipartFile!");
//
//    }
}

