package com.log_monitoring.dto;


import com.log_monitoring.model.TopicMetadata;
import lombok.*;

import java.util.Set;

@Getter @Setter
@NoArgsConstructor
@ToString
public class TopicSaveRequest {

    private String topicName;
    private String topicDescription;
    private int partitions;
    private int replicationFactor;
    private Set<FieldDto> fields;

    @Builder
    public TopicSaveRequest(String topicName, String topicDescription, int partitions, int replicationFactor, Set<FieldDto> fields) {
        this.topicName = topicName;
        this.topicDescription = topicDescription;
        this.partitions = partitions;
        this.replicationFactor = replicationFactor;
        this.fields = fields;
    }

    public TopicMetadata toEntity() {
        return TopicMetadata.builder()
                .topicName(topicName)
                .topicDescription(topicDescription)
                .partitions(partitions)
                .replicationFactor(replicationFactor)
                .build();
    }
}