package com.backend.Netflix.repository;

import com.backend.Netflix.model.Media;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaRepository extends CassandraRepository<Media, UUID> {

    @Query("SELECT * FROM media WHERE title = ?0 ALLOW FILTERING")
    void deleteByTitle(String title);

    @AllowFiltering
    Optional<List<Media>> findByGenre(String genre);

    @AllowFiltering
    Optional<List<Media>> findByTitle(String title);

    Optional<List<Media>> findByTitleContaining(String title);
}
