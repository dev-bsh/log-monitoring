package com.log_monitoring.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class TopicMetadata {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topicName;
    private String topicDescription;
    private int partitions;
    private int replicationFactor;

    @Builder
    public TopicMetadata(String topicName, String topicDescription, int partitions, int replicationFactor) {
        this.topicName = topicName;
        this.topicDescription = topicDescription;
        this.partitions = partitions;
        this.replicationFactor = replicationFactor;
    }

    public void updateDescription(String newDescription) {
        if (newDescription != null) {
            this.topicDescription = newDescription;
        }
    }
}
