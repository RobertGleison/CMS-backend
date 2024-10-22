package com.backend.Netflix.services;

import com.backend.Netflix.model.VideoMetadata;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

@Service
public class InternetArchiveService {

    private final RestTemplate restTemplate = new RestTemplate();

    public VideoMetadata getMetadata(String identifier) throws IOException {
        String metadataUrl = String.format("https://archive.org/metadata/%s", identifier);
        JSONObject response = new JSONObject(restTemplate.getForObject(metadataUrl, String.class));
        JSONObject metadata = response.getJSONObject("metadata");

        VideoMetadata videoMetadata = new VideoMetadata();
        videoMetadata.setTitle(metadata.optString("title", ""));
        videoMetadata.setDescription(metadata.optString("description", ""));
        videoMetadata.setYear(metadata.optString("year", ""));
        videoMetadata.setCreator(metadata.optString("creator", ""));
        videoMetadata.setRuntime(metadata.optString("runtime", ""));

        // Handle genre/subject as array
        if (metadata.has("subject")) {
            ArrayList<String> genres = new ArrayList<>();
            metadata.getJSONArray("subject").forEach(item -> genres.add(item.toString()));
            videoMetadata.setGenre(genres);
        }

        return videoMetadata;
    }

    public URL getVideoDownloadUrl(String identifier) throws IOException {
        String metadataUrl = String.format("https://archive.org/metadata/%s", identifier);
        JSONObject response = new JSONObject(restTemplate.getForObject(metadataUrl, String.class));
        JSONObject files = response.getJSONObject("files");

        // Find MP4 file
        String fileName = files.keySet().stream()
                .filter(key -> key.endsWith(".mp4"))
                .findFirst()
                .orElseThrow(() -> new IOException("No MP4 file found"));

        return new URL(String.format("https://archive.org/download/%s/%s", identifier, fileName));
    }

    public URL getThumbnailUrl(String identifier) throws MalformedURLException {
        return new URL(String.format("https://archive.org/download/%s/%s_thumb.jpg", identifier, identifier));
    }
}