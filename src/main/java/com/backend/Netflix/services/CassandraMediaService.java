package com.backend.Netflix.services;

import com.backend.Netflix.DTO.MediaResponseDTO;
import com.backend.Netflix.model.Media;
import com.backend.Netflix.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CassandraMediaService {


    @Autowired
    private MediaRepository mediaRepository;


    public MediaResponseDTO insertMedia(Media media) {
        return convertMediaToDTO(mediaRepository.save(media));
    }

    private MediaResponseDTO convertMediaToDTO(Media media){
        return new MediaResponseDTO(
                media.getTitle(),
                media.getDescription(),
                media.getGenre(),
                media.getYear(),
                media.getPublisher(),
                media.getDuration()
        );
    }

}
