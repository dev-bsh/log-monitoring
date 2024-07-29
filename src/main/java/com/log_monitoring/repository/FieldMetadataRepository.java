package com.log_monitoring.repository;

import com.log_monitoring.model.FieldMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface FieldMetadataRepository extends JpaRepository<FieldMetadata, Long> {
    Set<FieldMetadata> findAllByTopicMetadataId(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FieldMetadata m WHERE m.topicMetadata.id = :id")
    void deleteByTopicMetadataId(Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FieldMetadata m WHERE m.id IN :ids")
    void deleteAllByIds(Set<Long> ids);
}
