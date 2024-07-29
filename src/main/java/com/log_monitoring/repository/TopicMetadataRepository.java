package com.log_monitoring.repository;

import com.log_monitoring.model.TopicMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicMetadataRepository extends JpaRepository<TopicMetadata, Long> {
    Optional<TopicMetadata> findByTopicName(String topicName);
}
