package com.backend.Netflix.repository;

import com.backend.Netflix.model.Media;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.UUID;

public interface MediaRepository extends CassandraRepository<Media, UUID> {
}
