package com.backend.Netflix.repository;

import com.backend.Netflix.model.Media;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaRepository extends CassandraRepository<Media, UUID> {
    Optional<Media> findByTitle(String title);
//    Optional<Media> findByBucketPath(String bucketPath);
}
