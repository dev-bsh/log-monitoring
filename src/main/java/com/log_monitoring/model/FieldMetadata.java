package com.log_monitoring.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class FieldMetadata {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fieldName;
    private String fieldType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topic_metadata_id", nullable = false)
    private TopicMetadata topicMetadata;

    @Builder
    public FieldMetadata(String fieldName, String fieldType, TopicMetadata topicMetadata) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.topicMetadata = topicMetadata;
    }

    public void update(String fieldType) {
        if (fieldType != null) {
            this.fieldType = fieldType;
        }
    }
}
