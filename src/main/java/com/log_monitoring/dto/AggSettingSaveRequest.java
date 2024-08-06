package com.log_monitoring.dto;

import com.log_monitoring.model.AggSetting;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class AggSettingSaveRequest {

    private String topicName;
    private String settingName;
    private List<ConditionDto> condition;

    @Builder
    public AggSettingSaveRequest(String topicName, String settingName, List<ConditionDto> condition) {
        this.topicName = topicName;
        this.settingName = settingName;
        this.condition = condition;
    }

    public AggSetting toEntity() {
        return AggSetting.builder()
                .topicName(topicName)
                .settingName(settingName)
                .build();
    }

}
