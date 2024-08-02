package com.log_monitoring.dto;

import com.log_monitoring.model.AggSetting;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class AggSettingSaveRequest {

    private String topicName;
    private String fieldName;
    private String keyword;

    @Builder
    public AggSettingSaveRequest(String topicName, String fieldName, String keyword) {
        this.topicName = topicName;
        this.fieldName = fieldName;
        this.keyword = keyword;
    }

    public AggSetting toEntity() {
        return AggSetting.builder()
                .topicName(topicName)
                .fieldName(fieldName)
                .keyword(keyword)
                .build();
    }

}
