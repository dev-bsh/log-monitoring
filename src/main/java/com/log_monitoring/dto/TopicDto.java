package com.log_monitoring.dto;

import com.log_monitoring.model.FieldMetadata;
import com.log_monitoring.model.TopicMetadata;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor
public class TopicDto {

    private Long id;
    private String topicName;
    private String topicDescription;
    private Set<FieldDto> fields;

    @Builder
    public TopicDto(Long id, String topicName, String topicDescription, Set<FieldDto> fields) {
        this.id = id;
        this.topicName = topicName;
        this.topicDescription = topicDescription;
        this.fields = fields;
    }

    public static TopicDto fromEntity(TopicMetadata topicMetadata, Set<FieldMetadata> fields) {
        return TopicDto.builder()
                .id(topicMetadata.getId())
                .topicName(topicMetadata.getTopicName())
                .topicDescription(topicMetadata.getTopicDescription())
                .fields(fields.stream().map(FieldDto::fromEntity).collect(Collectors.toSet()))
                .build();
    }

}
