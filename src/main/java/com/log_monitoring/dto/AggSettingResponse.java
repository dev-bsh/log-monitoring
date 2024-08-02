package com.log_monitoring.dto;

import com.log_monitoring.model.AggSetting;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class AggSettingResponse {

    private Long id;
    private String topicName;
    private String fieldName;
    private String keyword;

    @Builder
    public AggSettingResponse(Long id, String topicName, String fieldName, String keyword) {
        this.id = id;
        this.topicName = topicName;
        this.fieldName = fieldName;
        this.keyword = keyword;
    }

    public static AggSettingResponse fromEntity(AggSetting entity) {
        return AggSettingResponse.builder()
                .id(entity.getId())
                .topicName(entity.getTopicName())
                .fieldName(entity.getFieldName())
                .keyword(entity.getKeyword())
                .build();
    }

}
