package com.backend.Netflix.repository;

import com.backend.Netflix.model.MediaResponseDTO;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaRepository extends CassandraRepository<MediaResponseDTO, UUID> {

    @Query("DELETE FROM media WHERE title = ?0")
    void deleteByTitle(String title);

    @AllowFiltering
    Optional<List<MediaResponseDTO>> findByGenre(String genre);

    @AllowFiltering
    Optional<List<MediaResponseDTO>> findByTitle(String title);

    Optional<List<MediaResponseDTO>> findByTitleContaining(String title);

}
