package com.log_monitoring.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
@NoArgsConstructor
public class TopicUpdateRequest {

    private Long id;
    private String topicDescription;
    private Set<FieldDto> fields;

    @Builder
    public TopicUpdateRequest(Long id, String topicDescription, Set<FieldDto> fields) {
        this.id = id;
        this.topicDescription = topicDescription;
        this.fields = fields;
    }

}
