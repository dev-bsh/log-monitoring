package com.log_monitoring.dto;

import com.log_monitoring.model.AggSetting;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class AggSettingResponse {

    private Long id;
    private String topicName;
    private String settingName;
    private List<ConditionDto> condition;

    @Builder
    public AggSettingResponse(Long id, String topicName, String settingName, List<ConditionDto> condition) {
        this.id = id;
        this.topicName = topicName;
        this.settingName = settingName;
        this.condition = condition;
    }

    public static AggSettingResponse fromEntity(AggSetting entity, List<ConditionDto> condition) {
        return AggSettingResponse.builder()
                .id(entity.getId())
                .topicName(entity.getTopicName())
                .settingName(entity.getSettingName())
                .condition(condition)
                .build();
    }

}
