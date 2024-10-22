package com.backend.Netflix.controllers;

import com.backend.Netflix.model.VideoMetadata;
import com.backend.Netflix.services.VideoProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoProcessingService videoProcessingService;


    @PostMapping("/process/{identifier}")
    public VideoMetadata processVideo(@PathVariable String identifier) throws IOException {
        return videoProcessingService.processVideo(identifier);
    }
}