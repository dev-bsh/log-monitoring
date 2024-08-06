package com.log_monitoring.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class LogDataSearchRequest {

    private Long from;
    private Long to;
    private String topicName;
    private List<ConditionDto> condition;

    public LogDataSearchRequest(Long from, Long to, String topicName, List<ConditionDto> condition) {
        this.from = from;
        this.to = to;
        this.topicName = topicName;
        this.condition = condition;
    }
}
